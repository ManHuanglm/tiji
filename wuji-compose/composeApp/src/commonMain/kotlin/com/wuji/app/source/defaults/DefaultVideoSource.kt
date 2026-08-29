package com.wuji.app.source.defaults

import com.wuji.app.source.VideoExtension
import com.wuji.app.source.model.PagedList
import com.wuji.app.source.model.PlayUrl
import com.wuji.app.source.model.VideoDetail
import com.wuji.app.source.model.VideoItem
import kotlin.random.Random

/** 内置示例视频源 - 影视列表/详情/多线路播放地址 */
class DefaultVideoSource : VideoExtension() {

    override suspend fun getRecommendList(pageNo: Int): PagedList<VideoItem> = PagedList(
        page = pageNo, list = mockItems(pageNo, "推荐"), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun search(keyword: String, pageNo: Int): PagedList<VideoItem> = PagedList(
        page = pageNo, list = mockItems(pageNo, keyword), pageSize = PAGE_SIZE, totalPage = 5,
    )

    override suspend fun getVideoDetail(item: VideoItem): VideoDetail = VideoDetail(
        item = item.copy(sourceId = this@DefaultVideoSource.id),
        intro = """
            ${item.title}
            上映:${item.releaseDate ?: "2026"}
            地区:${item.country ?: "全球"}
            导演:${item.director ?: "佚名"}
            主演:${item.actors ?: "待定"}
            ${item.intro ?: ""}
        """.trimIndent(),
        playUrls = buildList {
            // 演示多线路播放地址
            add(PlayUrl("线路一", "https://storage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoyrides.mp4"))
            add(PlayUrl("线路二", "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            add(PlayUrl("线路三", "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"))
        },
        sourceId = this@DefaultVideoSource.id,
    )

    private fun mockItems(page: Int, prefix: String): List<VideoItem> {
        val directors = listOf("张艺谋", "诺兰", "宫崎骏", "陈凯歌", "斯皮尔伯格", "昆汀")
        val actors = listOf("周润发, 巩俐, 刘德华", "木村拓哉, 松隆子", "莱昂纳多, 凯特", "沈腾, 贾玲")
        val countries = listOf("中国", "日本", "美国", "韩国", "法国")
        return (1..PAGE_SIZE).map { i ->
            val idx = (page - 1) * PAGE_SIZE + i
            VideoItem(
                id = "video_${prefix}_$idx",
                title = "$prefix 影片第 $idx 号",
                cover = "https://picsum.photos/seed/video$idx/400/560",
                duration = "${Random.nextInt(90, 180)} 分钟",
                releaseDate = "${2018 + (idx % 9)}",
                country = countries[idx % countries.size],
                director = directors[idx % directors.size],
                actors = actors[idx % actors.size],
                tags = listOf("剧情", "动作", "喜剧", "科幻", "动画")[idx % 5],
                status = if (idx % 4 == 0) "VIP" else "HD",
                intro = "$prefix 影片的精彩描述",
                sourceId = this@DefaultVideoSource.id,
            )
        }
    }

    companion object { private const val PAGE_SIZE = 20 }
}
