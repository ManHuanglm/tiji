package com.wuji.app.ui.screen.video

import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.VideoItem
import com.wuji.app.ui.screen.ListUiState
import com.wuji.app.ui.screen.ResourceListScreenModel

/** 视频列表 ScreenModel */
class VideoScreenModel(sourceEngine: SourceEngine) :
    ResourceListScreenModel<VideoItem>("VideoScreenModel") {

    private val ext = sourceEngine.getVideoExtensions()

    override suspend fun recommend(page: Int): List<VideoItem> =
        ext.mapNotNull { it.execGetRecommendList(page) }.flatMap { it.list }

    override suspend fun search(kw: String, page: Int): List<VideoItem> =
        ext.mapNotNull { it.execSearch(kw, page) }.flatMap { it.list }
}

typealias VideoUiState = ListUiState<VideoItem>
