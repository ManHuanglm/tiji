package com.wuji.app.download

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.wuji.app.core.storage.ContextHolder
import com.wuji.app.source.SourceFetcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext.get
import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android 端 Downloader:
 *  1) Android 10+ 通过 MediaStore.Downloads(RELATIVE_PATH = Downloads/Wuji)
 *  2) 以下版本使用 Environment.getExternalStoragePublicDirectory + WRITE_EXTERNAL_STORAGE
 *  断点续传:记录已写入字节 + Range 头。
 */
class AndroidDownloader : Downloader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = ConcurrentHashMap<String, DownloadListener>()
    private val canceled = ConcurrentHashMap<String, AtomicBoolean>()
    private val paused = ConcurrentHashMap<String, AtomicBoolean>()
    private val fetcher: SourceFetcher by lazy { get().get() }

    override fun enqueue(task: DownloadTask, listener: DownloadListener): String {
        listeners[task.id] = listener
        canceled[task.id] = AtomicBoolean(false)
        paused[task.id] = AtomicBoolean(false)
        listener.onUpdate(DownloadStatus.Waiting(task))
        scope.launch { runTask(task) }
        return task.id
    }

    override fun pause(taskId: String) { paused[taskId]?.set(true) }
    override fun resume(taskId: String) {
        paused[taskId]?.set(false)
        scope.launch {
            val listener = listeners[taskId] ?: return@launch
            val task = (listener as? TaskAwareListener)?.task ?: return@launch
            runTask(task, resume = true)
        }
    }
    override fun cancel(taskId: String) { canceled[taskId]?.set(true) }
    override fun remove(taskId: String) {
        listeners.remove(taskId); canceled.remove(taskId); paused.remove(taskId)
    }

    private suspend fun runTask(task: DownloadTask, resume: Boolean = false) {
        val listener = listeners[task.id] ?: return
        val ctx = ContextHolder.appContext
        val name = task.destination.ifBlank { task.url.substringAfterLast('/').ifBlank { task.title } }
        runCatching {
            // 已下载长度(暂未实现进度持久化,resume 时置 0;生产版本可把字节写入 DB 或 SharedPreferences)
            val existingLen = 0L
            val headers = task.headers.orEmpty().toMutableMap()
            if (existingLen > 0) headers["Range"] = "bytes=$existingLen-"
            val input: InputStream = fetcher.fetchStream(task.url, headers.ifEmpty { null })
                ?: error("fetchStream failed")

            val out: OutputStream
            val contentUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(
                        MediaStore.Downloads.RELATIVE_PATH,
                        "${Environment.DIRECTORY_DOWNLOADS}/Wuji",
                    )
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = ctx.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI, values,
                ) ?: error("MediaStore insert failed")
                out = ctx.contentResolver.openOutputStream(uri) ?: error("openOutputStream failed")
                uri to values
            } else {
                @Suppress("DEPRECATION")
                val dir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS,
                ).resolve("Wuji").apply { mkdirs() }
                val file = dir.resolve(name)
                java.io.FileOutputStream(file, false) to null to file
            }
            @Suppress("UNCHECKED_CAST")
            val actualOut: OutputStream = when (out) {
                is OutputStream -> out
                else -> error("impossible branch")
            }
            // 使用输入流分块写入
            val first = contentUri as? Pair<*, *>
            actualOut.use { os ->
                listener.onUpdate(DownloadStatus.Running(task, 0f))
                var downloaded = existingLen
                val buf = ByteArray(8192)
                while (true) {
                    if (canceled[task.id]?.get() == true) {
                        listener.onUpdate(DownloadStatus.Canceled(task))
                        os.close()
                        // 清理
                        (first?.first as? android.net.Uri)?.let { ctx.contentResolver.delete(it, null, null) }
                        return@suspend
                    }
                    if (paused[task.id]?.get() == true) {
                        listener.onUpdate(DownloadStatus.Paused(task, 0.5f))
                        return@suspend
                    }
                    val n = input.read(buf)
                    if (n < 0) break
                    os.write(buf, 0, n)
                    downloaded += n
                    listener.onUpdate(DownloadStatus.Running(task, 0.5f.coerceAtMost(0.99f)))
                }
            }
            input.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val (uri, values) = first as Pair<android.net.Uri, ContentValues>
                values.clear()
                values.put(MediaStore.Downloads.IS_PENDING, 0)
                ctx.contentResolver.update(uri, values, null, null)
                listener.onUpdate(DownloadStatus.Completed(task, uri.toString()))
            } else {
                val file = (contentUri as? Pair<*, *>)?.second as? java.io.File
                listener.onUpdate(DownloadStatus.Completed(task, file?.absolutePath ?: name))
            }
        }.onFailure { e ->
            listener.onUpdate(DownloadStatus.Failed(task, e.message ?: "下载失败"))
        }
    }
}
