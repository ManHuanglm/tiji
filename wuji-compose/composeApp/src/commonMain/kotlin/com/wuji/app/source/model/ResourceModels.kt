package com.wuji.app.source.model

import kotlinx.serialization.Serializable

/** 分页列表统一容器 */
@Serializable
data class PagedList<T>(
    val list: List<T> = emptyList(),
    val page: Int = 1,
    val pageSize: Int? = null,
    val totalPage: Int? = null,
)

/** 图片项 - 对应 PhotoItem */
@Serializable
data class PhotoItem(
    val id: String = "",
    val title: String? = null,
    val desc: String? = null,
    val cover: List<String> = emptyList(),
    val coverHeaders: Map<String, String>? = null,
    val author: String? = null,
    val datetime: String? = null,
    val hot: String? = null,
    val view: Int? = null,
    val url: String? = null,
    val noDetail: Boolean = false,
    val sourceId: String = "",
)

/** 图片详情 */
@Serializable
data class PhotoDetail(
    val item: PhotoItem,
    val photos: List<String> = emptyList(),
    val photosHeaders: Map<String, String>? = null,
    val page: Int = 1,
    val pageSize: Int? = null,
    val totalPage: Int? = null,
    val sourceId: String = "",
)

/** 图片收藏夹 */
@Serializable
data class PhotoShelf(
    val id: String,
    val name: String,
    val photos: List<PhotoItem> = emptyList(),
    val photosHeaders: Map<String, String>? = null,
    val createTime: Long,
)

/** 歌曲信息 - 对应 SongInfo */
@Serializable
data class SongInfo(
    val id: String = "",
    val title: String? = null,
    val author: String? = null,
    val desc: String? = null,
    val cover: String? = null,
    val coverHeaders: Map<String, String>? = null,
    val url: String? = null,
    val lyric: String? = null,
    val duration: Long? = null,
    val sourceId: String = "",
)

/** 歌单信息 */
@Serializable
data class PlaylistInfo(
    val id: String = "",
    val title: String? = null,
    val cover: String? = null,
    val coverHeaders: Map<String, String>? = null,
    val desc: String? = null,
    val author: String? = null,
    val url: String? = null,
    val sourceId: String = "",
)

/** 书籍项 - 对应 BookItem */
@Serializable
data class BookItem(
    val id: String = "",
    val title: String? = null,
    val desc: String? = null,
    val cover: String? = null,
    val coverHeaders: Map<String, String>? = null,
    val author: String? = null,
    val tags: String? = null,
    val status: String? = null,
    val url: String? = null,
    val latestChapter: String? = null,
    val latestUpdate: String? = null,
    val sourceId: String = "",
)

/** 书籍详情 */
@Serializable
data class BookDetail(
    val item: BookItem,
    val chapters: List<ChapterInfo> = emptyList(),
    val intro: String? = null,
    val sourceId: String = "",
)

/** 章节 */
@Serializable
data class ChapterInfo(
    val id: String,
    val title: String,
    val url: String? = null,
    val index: Int = 0,
)

/** 漫画项 - 对应 ComicItem */
@Serializable
data class ComicItem(
    val id: String = "",
    val title: String? = null,
    val desc: String? = null,
    val cover: String? = null,
    val coverHeaders: Map<String, String>? = null,
    val author: String? = null,
    val tags: String? = null,
    val status: String? = null,
    val url: String? = null,
    val latestChapter: String? = null,
    val latestUpdate: String? = null,
    val sourceId: String = "",
)

/** 漫画详情 */
@Serializable
data class ComicDetail(
    val item: ComicItem,
    val chapters: List<ChapterInfo> = emptyList(),
    val intro: String? = null,
    val sourceId: String = "",
)

/** 视频项 - 对应 VideoItem */
@Serializable
data class VideoItem(
    val id: String = "",
    val title: String? = null,
    val intro: String? = null,
    val cover: String? = null,
    val coverHeaders: Map<String, String>? = null,
    val releaseDate: String? = null,
    val country: String? = null,
    val duration: String? = null,
    val director: String? = null,
    val actors: String? = null,
    val tags: String? = null,
    val status: String? = null,
    val url: String? = null,
    val sourceId: String = "",
)

/** 视频详情 */
@Serializable
data class VideoDetail(
    val item: VideoItem,
    val playUrls: List<PlayUrl> = emptyList(),
    val intro: String? = null,
    val sourceId: String = "",
)

/** 播放地址 */
@Serializable
data class PlayUrl(
    val name: String,
    val url: String,
    val headers: Map<String, String>? = null,
)
