package com.wuji.app.source.defaults

import com.wuji.app.core.network.urlEncode
import com.wuji.app.source.SongExtension
import com.wuji.app.source.model.PagedList
import com.wuji.app.source.model.PlaylistInfo
import com.wuji.app.source.model.SongInfo
import kotlin.random.Random

/**
 * 内置示例音乐源 - 无需外部订阅源即可演示列表/搜索。
 * 真实部署后替换为 JS 源桥接或声明式源。
 */
class DefaultSongSource : SongExtension() {

    override suspend fun getRecommendList(pageNo: Int): PagedList<SongInfo> = PagedList(
        page = pageNo,
        list = mockSongs(page = pageNo, prefix = "推荐"),
        pageSize = PAGE_SIZE,
        totalPage = 5,
    )

    override suspend fun search(keyword: String, pageNo: Int): PagedList<SongInfo> = PagedList(
        page = pageNo,
        list = mockSongs(page = pageNo, prefix = keyword),
        pageSize = PAGE_SIZE,
        totalPage = 5,
    )

    override suspend fun getRecommendPlaylist(pageNo: Int): PagedList<PlaylistInfo> = PagedList(
        page = pageNo,
        list = (1..PAGE_SIZE).map {
            val id = "pl_${pageNo}_$it"
            PlaylistInfo(
                id = id,
                title = "歌单 ${Random.nextInt(100)}",
                desc = "包含 ${Random.nextInt(50)} 首歌曲",
                cover = "https://picsum.photos/seed/$id/400/400",
                sourceId = this@DefaultSongSource.id,
            )
        },
    )

    override suspend fun getPlaylistDetail(
        playlist: PlaylistInfo,
        pageNo: Int,
    ): PagedList<SongInfo> = PagedList(
        page = pageNo,
        list = mockSongs(page = pageNo, prefix = playlist.title ?: "歌单"),
    )

    private fun mockSongs(page: Int, prefix: String): List<SongInfo> {
        val authors = listOf("周杰伦", "林俊杰", "邓紫棋", "陈奕迅", "Taylor Swift", "Billie Eilish", "五月天", "毛不易")
        return (1..PAGE_SIZE).map { i ->
            val idx = (page - 1) * PAGE_SIZE + i
            val title = "$prefix - 第 $idx 首"
            SongInfo(
                id = "song_${prefix.urlEncode()}_${idx}",
                title = title,
                author = authors[(idx + Random.nextInt(authors.size)) % authors.size],
                cover = "https://picsum.photos/seed/song${idx}/200/200",
                duration = Random.nextLong(120_000, 360_000),
                sourceId = this@DefaultSongSource.id,
            )
        }
    }

    companion object { private const val PAGE_SIZE = 20 }
}
