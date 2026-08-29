package com.wuji.app.ui.screen.song

import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.SongInfo
import com.wuji.app.ui.screen.ListUiState
import com.wuji.app.ui.screen.ResourceListScreenModel

/** 音乐列表 ScreenModel - 聚合全部 SongExtension 的推荐/搜索结果 */
class SongScreenModel(sourceEngine: SourceEngine) :
    ResourceListScreenModel<SongInfo>("SongScreenModel") {

    private val ext = sourceEngine.getSongExtensions()

    override suspend fun recommend(page: Int): List<SongInfo> =
        ext.mapNotNull { it.execGetRecommendList(page) }.flatMap { it.list }

    override suspend fun search(kw: String, page: Int): List<SongInfo> =
        ext.mapNotNull { it.execSearch(kw, page) }.flatMap { it.list }
}

/** 复用 ListUiState 别名,避免每个文件重复声明 */
typealias SongUiState = ListUiState<SongInfo>
