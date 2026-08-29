package com.wuji.app.download

import org.koin.core.context.GlobalContext.get
import com.wuji.app.source.SourceFetcher
import java.io.File
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Desktop 端 Downloader:
 *  写入 ~/Downloads/Wuji 目录;流式分块写入支持断点续传(Range 头)。
 *  任务控制:AtomicBoolean 暂停/取消标志位。
 */
class DesktopDownloader : Downloader {

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
            // 简单策略:从上次已下载长度继续(如果 temp 文件存在则续传)
            val listener = listeners[taskId] ?: return@launch
            val task = (listener as? TaskAwareListener)?.task ?: return@launch
            runTask(task, resume = true)
        }
    }

    override fun cancel(taskId: String) {
        canceled[taskId]?.set(true)
    }

    override fun remove(taskId: String) {
        listeners.remove(taskId); canceled.remove(taskId); paused.remove(taskId)
    }

    private suspend fun runTask(task: DownloadTask, resume: Boolean = false) {
        val listener = listeners[task.id] ?: return
        val dir = File(System.getProperty("user.home"), "Downloads/Wuji").apply { mkdirs() }
        val target = File(dir, task.destination.takeIf { it.isNotBlank() } ?: task.title)
        val tmp = File(target.absolutePath + ".part")

        val existingLen = if (resume && tmp.exists()) tmp.length() else 0L
        runCatching {
            val headers = task.headers.orEmpty().toMutableMap()
            if (existingLen > 0) headers["Range"] = "bytes=$existingLen-"
            val input = fetcher.fetchStream(task.url, headers.ifEmpty { null })
                ?: error("fetchStream failed")
            tmp.outputStream().use { out ->
                if (existingLen > 0) {
                    // 已写入部分直接以 append 打开;但以 outputStream 覆盖会清空,改用 FileOutputStream append
                }
            }
            // 以 append 模式续传
            java.io.FileOutputStream(tmp, existingLen > 0).use { out ->
                listener.onUpdate(DownloadStatus.Running(task, 0f))
                var downloaded = existingLen
                var total = -1L
                val buf = ByteArray(8192)
                while (true) {
                    if (canceled[task.id]?.get() == true) {
                        listener.onUpdate(DownloadStatus.Canceled(task))
                        runCatching { tmp.delete() }
                        return@suspend
                    }
                    if (paused[task.id]?.get() == true) {
                        listener.onUpdate(DownloadStatus.Paused(task, if (total > 0) downloaded.toFloat() / total else 0f))
                        return@suspend
                    }
                    val n = input.read(buf)
                    if (n < 0) break
                    out.write(buf, 0, n)
                    downloaded += n
                    if (total < 0) total = input.available().let { if (it > 0) downloaded + it else -1L }
                    // 粗估进度:无 Content-Length 时用已下载字节数/临时估算
                    val p = if (total > 0) downloaded.toFloat() / total else 0.1f.coerceAtMost(0.99f)
                    listener.onUpdate(DownloadStatus.Running(task, p.coerceIn(0f, 0.99f)))
                }
            }
            input.close()
            tmp.renameTo(target)
            listener.onUpdate(DownloadStatus.Completed(task, target.absolutePath))
        }.onFailure { e ->
            listener.onUpdate(DownloadStatus.Failed(task, e.message ?: "下载失败"))
        }
    }
}

/** 标记型 Listener:resume 需要从 listener 找回 task。 下载界面 ScreenModel 内同时实现本接口。 */
interface TaskAwareListener : DownloadListener { val task: DownloadTask }
