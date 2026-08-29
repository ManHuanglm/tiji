package com.wuji.app.source

import com.wuji.app.source.model.PagedList
import com.wuji.app.source.model.PlaylistInfo
import com.wuji.app.source.model.SongInfo

/**
 * 音乐源扩展 - 对应原项目 SongExtension。
 */
abstract class SongExtension : Extension() {
    /** 推荐歌曲列表 */
    abstract suspend fun getRecommendList(pageNo: Int = 1): PagedList<SongInfo>?

    /** 搜索歌曲 */
    abstract suspend fun search(keyword: String, pageNo: Int = 1): PagedList<SongInfo>?

    /** 搜索歌单 */
    open suspend fun searchPlaylist(keyword: String, pageNo: Int = 1): PagedList<PlaylistInfo>? = null

    /** 推荐歌单 */
    open suspend fun getRecommendPlaylist(pageNo: Int = 1): PagedList<PlaylistInfo>? = null

    /** 歌单详情(歌曲列表) */
    open suspend fun getPlaylistDetail(playlist: PlaylistInfo, pageNo: Int = 1): PagedList<SongInfo>? = null

    /** 获取播放地址 */
    open suspend fun getSongUrl(song: SongInfo): String? = song.url

    /** 获取歌词 */
    open suspend fun getSongLyric(song: SongInfo): String? = song.lyric

    /** 包裹推荐,补全 sourceId */
    suspend fun execGetRecommendList(pageNo: Int = 1): PagedList<SongInfo>? = runCatching {
        getRecommendList(pageNo)?.let { it.copy(list = it.list.map { s -> s.copy(sourceId = id) }) }
    }.getOrNull()

    suspend fun execSearch(keyword: String, pageNo: Int = 1): PagedList<SongInfo>? {
        if (keyword.isBlank()) return execGetRecommendList(pageNo)
        return runCatching {
            search(keyword, pageNo)?.let { it.copy(list = it.list.map { s -> s.copy(sourceId = id) }) }
        }.getOrNull()
    }
}
