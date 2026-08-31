package com.wuji.app.source

import com.wuji.app.source.model.PagedList
import com.wuji.app.source.model.VideoDetail
import com.wuji.app.source.model.VideoItem

/**
 * 视频源扩展 - 对应原项目 VideoExtension。
 */
abstract class VideoExtension : Extension() {
    abstract suspend fun getRecommendList(pageNo: Int = 1): PagedList<VideoItem>?
    abstract suspend fun search(keyword: String, pageNo: Int = 1): PagedList<VideoItem>?
    abstract suspend fun getVideoDetail(item: VideoItem): VideoDetail?

    suspend fun execGetRecommendList(pageNo: Int = 1): PagedList<VideoItem>? = runCatching {
        getRecommendList(pageNo)?.let { it.copy(list = it.list.map { v -> v.copy(sourceId = id) }) }
    }.getOrNull()

    suspend fun execSearch(keyword: String, pageNo: Int = 1): PagedList<VideoItem>? {
        if (keyword.isBlank()) return execGetRecommendList(pageNo)
        return runCatching {
            search(keyword, pageNo)?.let { it.copy(list = it.list.map { v -> v.copy(sourceId = id) }) }
        }.getOrNull()
    }
}
