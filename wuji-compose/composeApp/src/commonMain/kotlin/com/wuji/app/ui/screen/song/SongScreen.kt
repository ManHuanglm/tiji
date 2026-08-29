package com.wuji.app.ui.screen.song

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.ResourceListScaffold

/** 音乐列表页 - 对齐原项目 song/index.vue */
object SongScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SongScreenModel>()
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
            titleFn = { it.title ?: "未命名歌曲" },
            subtitleFn = { it.author },
            placeholderHint = "搜索歌曲/歌手",
            gridAspect = 1.0f,
            onItemClick = { nav?.push(SongDetailScreen(it)) },
        )
    }
}
