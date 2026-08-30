package com.wuji.app.download

/**
 * 下载管理器 - 跨平台 expect/actual。
 * Desktop 写本地磁盘;Android 写 Downloads 公共目录(MediaStore / 存储路径)。
 * 状态通过 [DownloadListener] 回调;实现类内部维护 taskId → task/listener 映射,支持 resume。
 */
interface Downloader {

    /** 启动下载任务。返回 taskId(等于 task.id) */
    fun enqueue(task: DownloadTask, listener: DownloadListener): String

    /** 暂停 */
    fun pause(taskId: String)

    /** 恢复(从上次位置续传) */
    fun resume(taskId: String)

    /** 取消(清理临时文件) */
    fun cancel(taskId: String)

    /** 移除任务记录 */
    fun remove(taskId: String)
}

/** 任务信息 */
data class DownloadTask(
    val id: String = "task_${System.nanoTime()}",
    val title: String,
    val url: String,
    val headers: Map<String, String>? = null,
    val destination: String = title, // 可留文件名或完整路径,实现侧再处理
)

/** 任务状态 */
sealed interface DownloadStatus {
    val task: DownloadTask
    val progress: Float get() = 0f
    data class Waiting(override val task: DownloadTask) : DownloadStatus
    data class Running(override val task: DownloadTask, override val progress: Float) : DownloadStatus
    data class Paused(override val task: DownloadTask, override val progress: Float) : DownloadStatus
    data class Completed(override val task: DownloadTask, val savedPath: String) : DownloadStatus {
        override val progress: Float get() = 1.0f
    }
    data class Failed(override val task: DownloadTask, val error: String) : DownloadStatus
    data class Canceled(override val task: DownloadTask) : DownloadStatus
}

/** 状态监听器 */
interface DownloadListener {
    fun onUpdate(status: DownloadStatus)
}
