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
import kotlinx.coroutines.launch

/**
 * 下载管理 ScreenModel - 任务聚合、操作(暂停/恢复/取消/重新下载)。
 * AggregateListener 既保存 task 信息(便于 resume 逻辑找回 task),也把事件通过状态推送 UI。
 */
class DownloadManagerScreenModel(private val downloader: Downloader) : ScreenModel {

    var tasks by mutableStateOf<List<DownloadStatus>>(emptyList())
        private set

    /** 新建一个下载任务;默认立即执行。 */
    fun addMockTask(title: String, url: String) {
        val task = DownloadTask(title = title, url = url)
        val listener = object : TaskAwareDownloadListener {
            override val task: DownloadTask = task
            override fun onUpdate(status: DownloadStatus) {
                screenModelScope.launch {
                    tasks = tasks.map { if (it.task.id == task.id) status else it } +
                        if (tasks.none { it.task.id == task.id }) listOf(status) else emptyList()
                }
            }
        }
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
}

/** 同时携带 task 引用的 DownloadListener,供 resume 逻辑再查 task */
interface TaskAwareDownloadListener : DownloadListener {
    val task: DownloadTask
}
