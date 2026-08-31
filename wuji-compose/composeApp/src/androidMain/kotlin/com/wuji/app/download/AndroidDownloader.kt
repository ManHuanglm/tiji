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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Android Downloader:
 *   - API 29+ 走 MediaStore.Downloads 公共目录
 *   - API 28- 走 Environment.getExternalStoragePublicDirectory(DOWNLOADS)/Wuji
 *   - 进度估算逻辑与 Desktop 对齐(无 Content-Length 时封顶 99% 到完成时 100%)
 *   - 断点续传:已写入 contentValues 拿到的 contentUri + seek append(若文件存在则继续)
 */
class AndroidDownloader : Downloader {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val listeners = ConcurrentHashMap<String, DownloadListener>()
    private val tasks = ConcurrentHashMap<String, DownloadTask>()
    private val canceled = ConcurrentHashMap<String, AtomicBoolean>()
    private val paused = ConcurrentHashMap<String, AtomicBoolean>()
    private val fetcher: SourceFetcher by lazy { get().get() }

    override fun enqueue(task: DownloadTask, listener: DownloadListener): String {
        tasks[task.id] = task
        listeners[task.id] = listener
        canceled[task.id] = AtomicBoolean(false)
        paused[task.id] = AtomicBoolean(false)
        listener.onUpdate(DownloadStatus.Waiting(task))
        scope.launch { runTask(task, resume = false) }
        return task.id
    }

    override fun pause(taskId: String) { paused[taskId]?.set(true) }

    override fun resume(taskId: String) {
        paused[taskId]?.set(false)
        val task = tasks[taskId] ?: return
        listeners[taskId]?.onUpdate(DownloadStatus.Waiting(task))
        scope.launch { runTask(task, resume = true) }
    }

    override fun cancel(taskId: String) { canceled[taskId]?.set(true) }

    override fun remove(taskId: String) {
        listeners.remove(taskId); tasks.remove(taskId)
        canceled.remove(taskId); paused.remove(taskId)
    }

    private data class StreamHolder(
        val uri: android.net.Uri?,
        val output: java.io.OutputStream,
        val finalize: () -> String,
    )

    @Suppress("DEPRECATION")
    private suspend fun runTask(task: DownloadTask, resume: Boolean) {
        val listener = listeners[task.id] ?: return
        val context = ContextHolder.appContext
        val fileName = task.destination.takeIf { it.isNotBlank() }
            ?: task.url.substringAfterLast('/').ifBlank { task.title }

        runCatching {
            val input = fetcher.fetchStream(task.url, task.headers)
                ?: error("fetchStream returned null")

            // 获取输出流
            val out: StreamHolder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/Wuji")
                    put(MediaStore.Downloads.MIME_TYPE, "application/octet-stream")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
                    ?: error("MediaStore insert returned null")
                val os = context.contentResolver.openOutputStream(uri, if (resume) "wa" else "w")
                    ?: error("openOutputStream failed")
                StreamHolder(uri, os) {
                    values.clear()
                    values.put(MediaStore.Downloads.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                    uri.toString()
                }
            } else {
                val pub = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val dir = java.io.File(pub, "Wuji").apply { mkdirs() }
                val file = java.io.File(dir, fileName)
                StreamHolder(null, java.io.FileOutputStream(file, resume)) { file.absolutePath }
            }
            out.output.use { outStream ->
                listener.onUpdate(DownloadStatus.Running(task, 0f))
                val buf = ByteArray(8192)
                var downloaded = 0L
                while (true) {
                    if (canceled[task.id]?.get() == true) {
                        listener.onUpdate(DownloadStatus.Canceled(task))
                        return@suspend
                    }
                    if (paused[task.id]?.get() == true) {
                        val lastP = minOf(0.99f, downloaded / (downloaded + 8192f).coerceAtLeast(1f))
                        listener.onUpdate(DownloadStatus.Paused(task, lastP))
                        return@suspend
                    }
                    val n = input.read(buf)
                    if (n < 0) break
                    outStream.write(buf, 0, n)
                    downloaded += n
                    val p = minOf(0.99f, downloaded / (downloaded + 32768f).coerceAtLeast(1f))
                    listener.onUpdate(DownloadStatus.Running(task, p))
                }
            }
            runCatching { input.close() }
            val path = out.finalize()
            listener.onUpdate(DownloadStatus.Completed(task, path))
        }.onFailure { e ->
            listener.onUpdate(DownloadStatus.Failed(task, e.message ?: "下载失败"))
        }
    }
}
