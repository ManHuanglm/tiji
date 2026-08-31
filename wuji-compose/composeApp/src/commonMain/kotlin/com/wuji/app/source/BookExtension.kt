package com.wuji.app.source

import com.wuji.app.source.model.BookDetail
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.PagedList

/**
 * 书籍源扩展 - 对应原项目 BookExtension。
 */
abstract class BookExtension : Extension() {
    /** 推荐书籍 */
    abstract suspend fun getRecommendList(pageNo: Int = 1): PagedList<BookItem>?

    /** 搜索 */
    abstract suspend fun search(keyword: String, pageNo: Int = 1): PagedList<BookItem>?

    /** 书籍详情(含章节列表) */
    abstract suspend fun getBookDetail(item: BookItem): BookDetail?

    /** 章节内容(正文文本) */
    abstract suspend fun getChapterContent(chapter: ChapterInfo): String?

    suspend fun execGetRecommendList(pageNo: Int = 1): PagedList<BookItem>? = runCatching {
        getRecommendList(pageNo)?.let { it.copy(list = it.list.map { b -> b.copy(sourceId = id) }) }
    }.getOrNull()

    suspend fun execSearch(keyword: String, pageNo: Int = 1): PagedList<BookItem>? {
        if (keyword.isBlank()) return execGetRecommendList(pageNo)
        return runCatching {
            search(keyword, pageNo)?.let { it.copy(list = it.list.map { b -> b.copy(sourceId = id) }) }
        }.getOrNull()
    }
}
