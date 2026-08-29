package com.wuji.app.source

import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.ComicDetail
import com.wuji.app.source.model.ComicItem
import com.wuji.app.source.model.PagedList

/**
 * 漫画源扩展 - 对应原项目 ComicExtension。
 */
abstract class ComicExtension : Extension() {
    abstract suspend fun getRecommendList(pageNo: Int = 1): PagedList<ComicItem>?
    abstract suspend fun search(keyword: String, pageNo: Int = 1): PagedList<ComicItem>?
    abstract suspend fun getComicDetail(item: ComicItem): ComicDetail?

    /** 章节图片列表 */
    abstract suspend fun getChapterImages(chapter: ChapterInfo): List<String>?

    suspend fun execGetRecommendList(pageNo: Int = 1): PagedList<ComicItem>? = runCatching {
        getRecommendList(pageNo)?.let { it.copy(list = it.list.map { c -> c.copy(sourceId = id) }) }
    }.getOrNull()

    suspend fun execSearch(keyword: String, pageNo: Int = 1): PagedList<ComicItem>? {
        if (keyword.isBlank()) return execGetRecommendList(pageNo)
        return runCatching {
            search(keyword, pageNo)?.let { it.copy(list = it.list.map { c -> c.copy(sourceId = id) }) }
        }.getOrNull()
    }
}
