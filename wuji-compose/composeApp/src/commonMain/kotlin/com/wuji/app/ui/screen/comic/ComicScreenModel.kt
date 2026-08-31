package com.wuji.app.ui.screen.comic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.ComicItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 漫画列表页状态机。
 */
class ComicScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<ComicUiState>(ComicUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    private var currentPage = 1

    fun loadFirst() {
        currentPage = 1
        uiState = ComicUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val items = if (keyword.isBlank()) {
                    sourceEngine.getComicExtensions().mapNotNull { it.execGetRecommendList(1) }
                } else {
                    sourceEngine.getComicExtensions().mapNotNull { it.execSearch(keyword, 1) }
                }.flatMap { it.list }
                if (items.isEmpty()) ComicUiState.Empty else ComicUiState.Success(items, page = 1, hasMore = true)
            }.getOrElse { e ->
                Napier.w("ComicScreenModel loadFirst: ${e.message}")
                ComicUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        val current = uiState as? ComicUiState.Success ?: return
        if (!current.hasMore || current.loadingMore) return
        uiState = current.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = current.page + 1
            val more = if (keyword.isBlank()) {
                sourceEngine.getComicExtensions().mapNotNull { it.execGetRecommendList(nextPage) }
            } else {
                sourceEngine.getComicExtensions().mapNotNull { it.execSearch(keyword, nextPage) }
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

sealed interface ComicUiState {
    data object Loading : ComicUiState
    data object Empty : ComicUiState
    data class Error(val message: String) : ComicUiState
    data class Success(
        val items: List<ComicItem>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : ComicUiState
}
