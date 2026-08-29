package com.wuji.app.ui.screen.book

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.ResourceListScaffold

/** 书籍列表页 - 对齐原项目 book/index.vue */
object BookScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<BookScreenModel>()
        val nav = LocalNavigator.current
        LaunchedEffect(Unit) { model.loadFirst() }
        ResourceListScaffold(
            state = model.uiState,
            keyword = model.keyword,
            onKeywordChange = model::onKeywordChange,
            onSearch = model::search,
            onRetry = model::refresh,
            onLoadMore = model::loadMore,
            keyFn = { it.id.ifBlank { it.title ?: System.identityHashCode(it).toString() } },
            coverFn = { it.cover },
            titleFn = { it.title ?: "未命名书籍" },
            subtitleFn = { buildString {
                it.author?.let { a -> append(a) }
                if (!it.tags.isNullOrBlank()) append(" · ${it.tags}")
                if (!it.status.isNullOrBlank()) append(" · ${it.status}")
            }.ifBlank { null } },
            placeholderHint = "搜索书名/作者",
            gridAspect = 0.75f,
            onItemClick = { nav?.push(BookDetailScreen(it)) },
        )
    }
}
