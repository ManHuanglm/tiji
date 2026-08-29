package com.wuji.app.source.defaults

import com.wuji.app.source.ComicExtension
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.ComicDetail
import com.wuji.app.source.model.ComicItem
import com.wuji.app.source.model.PagedList
import kotlin.random.Random

/** 内置示例漫画源 - 漫画列表/详情/章节页图片 */
class DefaultComicSource : ComicExtension() {

    override suspend fun getRecommendList(pageNo: Int): PagedList<ComicItem> = PagedList(
        page = pageNo, list = mockItems(pageNo, "推荐"), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun search(keyword: String, pageNo: Int): PagedList<ComicItem> = PagedList(
        page = pageNo, list = mockItems(pageNo, keyword), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun getComicDetail(item: ComicItem): ComicDetail {
        val chapterCount = Random.nextInt(20, 120)
        return ComicDetail(
            item = item.copy(sourceId = this@DefaultComicSource.id),
            intro = """
                ${item.title}——${item.tags}作品。作者:${item.author ?: "佚名"}。
                ${item.status ?: "连载中"},目前更新至第 ${Random.nextInt(10, 999)} 话。
            """.trimIndent(),
            chapters = (1..chapterCount).map { i ->
                ChapterInfo(
                    id = "${item.id}_ch_$i",
                    title = "第 ${i} 话",
                    index = i,
                )
            },
            sourceId = this@DefaultComicSource.id,
        )
    }

    /** 漫画章节页:返回一组图片 URL(占位 picsum) */
    override suspend fun getChapterImages(chapter: ChapterInfo): List<String>? {
        val seed = chapter.id
        val pages = Random(seed.hashCode()).nextInt(8, 30)
        return (1..pages).map { p -> "https://picsum.photos/seed/${seed}_$p/800/1200" }
    }

    private fun mockItems(page: Int, prefix: String): List<ComicItem> {
        val authors = listOf("尾田荣一郎", "久保带人", "岸本齐史", "青山刚昌", "藤本树", "村上真纪")
        val tags = listOf("热血", "恋爱", "日常", "悬疑", "奇幻", "竞技")
        return (1..PAGE_SIZE).map { i ->
            val idx = (page - 1) * PAGE_SIZE + i
            ComicItem(
                id = "comic_${prefix}_$idx",
                title = "$prefix 漫画第 $idx 卷",
                author = authors[idx % authors.size],
                cover = "https://picsum.photos/seed/comic$idx/200/280",
                tags = tags[idx % tags.size],
                status = if (idx % 3 == 0) "已完结" else "连载中",
                latestChapter = "第 ${Random.nextInt(10, 999)} 话",
                desc = "$prefix 漫画精彩描述",
                sourceId = this@DefaultComicSource.id,
            )
        }
    }

    companion object { private const val PAGE_SIZE = 24 }
}
