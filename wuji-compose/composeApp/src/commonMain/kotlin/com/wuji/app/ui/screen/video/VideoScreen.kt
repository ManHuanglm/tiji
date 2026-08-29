package com.wuji.app.ui.screen.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.ResourceListScaffold

/** 视频列表页 - 对齐原项目 video/index.vue */
object VideoScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<VideoScreenModel>()
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
            titleFn = { it.title ?: "未命名影片" },
            subtitleFn = { buildString {
                it.releaseDate?.let { d -> append(d) }
                if (!it.country.isNullOrBlank()) append(" · ${it.country}")
                if (!it.duration.isNullOrBlank()) append(" · ${it.duration}")
                if (!it.status.isNullOrBlank()) append(" · ${it.status}")
            }.ifBlank { null } },
            placeholderHint = "搜索影片名/演员",
            gridAspect = 0.6f, // 视频海报偏横向
            onItemClick = { nav?.push(VideoDetailScreen(it)) },
        )
    }
}
