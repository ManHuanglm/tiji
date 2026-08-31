package com.wuji.app.ui.screen.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.VideoItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 视频列表页状态机。
 */
class VideoScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<VideoUiState>(VideoUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    private var currentPage = 1

    fun loadFirst() {
        currentPage = 1
        uiState = VideoUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val items = if (keyword.isBlank()) {
                    sourceEngine.getVideoExtensions().mapNotNull { it.execGetRecommendList(1) }
                } else {
                    sourceEngine.getVideoExtensions().mapNotNull { it.execSearch(keyword, 1) }
                }.flatMap { it.list }
                if (items.isEmpty()) VideoUiState.Empty else VideoUiState.Success(items, page = 1, hasMore = true)
            }.getOrElse { e ->
                Napier.w("VideoScreenModel loadFirst: ${e.message}")
                VideoUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        val current = uiState as? VideoUiState.Success ?: return
        if (!current.hasMore || current.loadingMore) return
        uiState = current.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = current.page + 1
            val more = if (keyword.isBlank()) {
                sourceEngine.getVideoExtensions().mapNotNull { it.execGetRecommendList(nextPage) }
            } else {
                sourceEngine.getVideoExtensions().mapNotNull { it.execSearch(keyword, nextPage) }
            }.flatMap { it.list }
            uiState = current.copy(
                items = current.items + more,
                page = nextPage,
                hasMore = more.isNotEmpty(),
                loadingMore = false,
            )
        }
    }

    fun onKeywordChange(kw: String) { keyword = kw }
    fun search() { loadFirst() }
    fun refresh() { loadFirst() }
}

sealed interface VideoUiState {
    data object Loading : VideoUiState
    data object Empty : VideoUiState
    data class Error(val message: String) : VideoUiState
    data class Success(
        val items: List<VideoItem>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : VideoUiState
}
