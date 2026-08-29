package com.wuji.app.ui.screen.comic

import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.ComicItem
import com.wuji.app.ui.screen.ListUiState
import com.wuji.app.ui.screen.ResourceListScreenModel

/** 漫画列表 ScreenModel */
class ComicScreenModel(sourceEngine: SourceEngine) :
    ResourceListScreenModel<ComicItem>("ComicScreenModel") {

    private val ext = sourceEngine.getComicExtensions()

    override suspend fun recommend(page: Int): List<ComicItem> =
        ext.mapNotNull { it.execGetRecommendList(page) }.flatMap { it.list }

    override suspend fun search(kw: String, page: Int): List<ComicItem> =
        ext.mapNotNull { it.execSearch(kw, page) }.flatMap { it.list }
}

typealias ComicUiState = ListUiState<ComicItem>
