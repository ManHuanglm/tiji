package com.wuji.app.download

import org.koin.core.context.GlobalContext.get
import com.wuji.app.source.SourceFetcher
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Desktop Downloader:
 *  - 写入 ~/Downloads/Wuji 目录
 *  - 断点续传:若 .part 文件存在,则通过 Range 头续传
 *  - 进度估算:若远端未给 Content-Length,进度固定显示 50%(直到完成)
 */
class DesktopDownloader : Downloader {

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
        val listener = listeners[taskId] ?: return
        listener.onUpdate(DownloadStatus.Waiting(task))
        scope.launch { runTask(task, resume = true) }
    }

    override fun cancel(taskId: String) { canceled[taskId]?.set(true) }

    override fun remove(taskId: String) {
        listeners.remove(taskId); tasks.remove(taskId)
        canceled.remove(taskId); paused.remove(taskId)
    }

    private suspend fun runTask(task: DownloadTask, resume: Boolean) {
        val listener = listeners[task.id] ?: return
        val dir = File(System.getProperty("user.home"), "Downloads/Wuji").apply { mkdirs() }
        val fileName = task.destination.takeIf { it.isNotBlank() }
            ?: task.url.substringAfterLast('/').ifBlank { task.title }
        val target = File(dir, fileName)
        val tmp = File(target.absolutePath + ".part")

        val existingLen = if (resume && tmp.exists()) tmp.length() else 0L
        val cancelled = canceled[task.id]
        val paus = paused[task.id]
        fun cancelledV() = cancelled?.get() == true
        fun pausedV() = paus?.get() == true

        runCatching {
            val headers = task.headers.orEmpty().toMutableMap()
            if (existingLen > 0) headers["Range"] = "bytes=$existingLen-"
            val input = fetcher.fetchStream(task.url, headers.ifEmpty { null })
                ?: error("fetchStream returned null")
            var downloaded = existingLen
            FileOutputStream(tmp, existingLen > 0).use { out ->
                listener.onUpdate(DownloadStatus.Running(task, 0f))
                val buf = ByteArray(8192)
                var finished = false
                while (!finished) {
                    if (cancelledV()) {
                        listener.onUpdate(DownloadStatus.Canceled(task))
                        runCatching { tmp.delete() }
                        return@runCatching
                    }
                    if (pausedV()) {
                        val lastP = minOf(0.99f, downloaded / (downloaded + 8192f).coerceAtLeast(1f))
                        listener.onUpdate(DownloadStatus.Paused(task, lastP))
                        return@runCatching
                    }
                    val n = input.read(buf)
                    if (n < 0) { finished = true; continue }
                    out.write(buf, 0, n)
                    downloaded += n
                    val p = minOf(0.99f, downloaded / (downloaded + 32768f).coerceAtLeast(1f))
                    listener.onUpdate(DownloadStatus.Running(task, p))
                }
            }
            runCatching { input.close() }
            if (!tmp.renameTo(target)) {
                tmp.copyTo(target, overwrite = true)
                tmp.delete()
            }
            listener.onUpdate(DownloadStatus.Completed(task, target.absolutePath))
        }.onFailure { e ->
            listener.onUpdate(DownloadStatus.Failed(task, e.message ?: "下载失败"))
        }
    }
}
