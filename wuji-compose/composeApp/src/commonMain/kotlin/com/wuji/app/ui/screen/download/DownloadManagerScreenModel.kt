package com.wuji.app.ui.screen.download

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.download.DownloadListener
import com.wuji.app.download.DownloadStatus
import com.wuji.app.download.DownloadTask
import com.wuji.app.download.Downloader
import com.wuji.app.download.TaskAwareListener

/** 下载管理 ScreenModel - 任务聚合、操作(暂停/恢复/取消/重新下载) */
class DownloadManagerScreenModel(private val downloader: Downloader) : ScreenModel {

    var tasks by mutableStateOf<List<DownloadStatus>>(emptyList())
        private set

    /** 测试用 mock URL 占位,也允许用户手动构造任务。 */
    fun addMockTask(title: String, url: String) {
        val task = DownloadTask(title = title, url = url)
        val listener = AggregateListener(task)
        tasks = tasks + DownloadStatus.Waiting(task)
        downloader.enqueue(task, listener)
    }

    fun pause(taskId: String) = downloader.pause(taskId)
    fun resume(taskId: String) = downloader.resume(taskId)
    fun cancel(taskId: String) = downloader.cancel(taskId)
    fun remove(taskId: String) {
        downloader.remove(taskId)
        tasks = tasks.filterNot { it.task.id == taskId }
    }

    private inner class AggregateListener(override val task: DownloadTask) : TaskAwareListener {
        override fun onUpdate(status: DownloadStatus) {
            screenModelScope.launch {
                tasks = tasks.map { if (it.task.id == task.id) status else it }
            }
        }
    }
}
