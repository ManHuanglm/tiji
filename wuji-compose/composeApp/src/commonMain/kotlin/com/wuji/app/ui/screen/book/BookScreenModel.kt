package com.wuji.app.ui.screen.book

import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.BookItem
import com.wuji.app.ui.screen.ListUiState
import com.wuji.app.ui.screen.ResourceListScreenModel

/** 书籍列表 ScreenModel */
class BookScreenModel(sourceEngine: SourceEngine) :
    ResourceListScreenModel<BookItem>("BookScreenModel") {

    private val ext = sourceEngine.getBookExtensions()

    override suspend fun recommend(page: Int): List<BookItem> =
        ext.mapNotNull { it.execGetRecommendList(page) }.flatMap { it.list }

    override suspend fun search(kw: String, page: Int): List<BookItem> =
        ext.mapNotNull { it.execSearch(kw, page) }.flatMap { it.list }
}

typealias BookUiState = ListUiState<BookItem>
