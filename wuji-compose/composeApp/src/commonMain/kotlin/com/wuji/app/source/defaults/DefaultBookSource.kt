package com.wuji.app.source.defaults

import com.wuji.app.source.BookExtension
import com.wuji.app.source.model.BookDetail
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.PagedList
import kotlin.random.Random

/** 内置示例书籍源 - 演示书籍列表/详情/章节内容 */
class DefaultBookSource : BookExtension() {

    override suspend fun getRecommendList(pageNo: Int): PagedList<BookItem> = PagedList(
        page = pageNo, list = mockBooks(pageNo, "推荐"), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun search(keyword: String, pageNo: Int): PagedList<BookItem> = PagedList(
        page = pageNo, list = mockBooks(pageNo, keyword), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun getBookDetail(item: BookItem): BookDetail {
        val chapterCount = Random.nextInt(80, 320)
        return BookDetail(
            item = item.copy(sourceId = this@DefaultBookSource.id),
            intro = """
                ${item.title} 是一部${item.status ?: "连载中"}作品。
                讲述了${item.author ?: "佚名"}笔下主角波澜壮阔的人生,情节跌宕起伏,人物塑造丰满。
            """.trimIndent(),
            chapters = (1..chapterCount).map { i ->
                ChapterInfo(
                    id = "${item.id}_ch_$i",
                    title = "第 ${i}章 章节标题 $i",
                    url = "",
                    index = i,
                )
            },
            sourceId = this@DefaultBookSource.id,
        )
    }

    override suspend fun getChapterContent(chapter: ChapterInfo): String {
        // 模拟章节内容:按章节序号生成固定长度的文本
        val paragraphs = (1..Random.nextInt(25, 60)).joinToString("\n\n") {
            (1..Random.nextInt(6, 14)).joinToString("") {
                MOCK_CHARS.random().toString()
            }
        }
        return "${chapter.title}\n\n$paragraphs"
    }

    private fun mockBooks(page: Int, prefix: String): List<BookItem> {
        val authors = listOf("辰东", "唐家三少", "耳根", "猫腻", "紫金陈", "余华", "刘慈欣", "村上春树")
        val statuses = listOf("连载中", "已完结")
        val tags = listOf("玄幻", "都市", "科幻", "历史", "悬疑", "爱情", "轻小说")
        return (1..PAGE_SIZE).map { i ->
            val idx = (page - 1) * PAGE_SIZE + i
            BookItem(
                id = "book_${prefix}_$idx",
                title = "$prefix 小说第 $idx 册",
                author = authors[idx % authors.size],
                cover = "https://picsum.photos/seed/book$idx/200/280",
                tags = tags[(idx + 2) % tags.size],
                status = statuses[idx % 2],
                latestChapter = "第 ${Random.nextInt(10, 1500)} 章",
                latestUpdate = "2026-08-${10 + (idx % 18)}",
                desc = "$prefix 小说的精彩描述",
                sourceId = this@DefaultBookSource.id,
            )
        }
    }

    companion object {
        private const val PAGE_SIZE = 24
        private val MOCK_CHARS = ('\u4e00'..'\u9fa5').toList() + listOf('，', '。', '！', '？', '：', '；', '“', '”')
    }
}
