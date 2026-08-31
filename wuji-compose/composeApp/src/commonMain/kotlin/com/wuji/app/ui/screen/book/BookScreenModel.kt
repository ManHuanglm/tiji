package com.wuji.app.ui.screen.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.BookItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 书籍列表页状态机。
 */
class BookScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<BookUiState>(BookUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    private var currentPage = 1

    fun loadFirst() {
        currentPage = 1
        uiState = BookUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val items = if (keyword.isBlank()) {
                    sourceEngine.getBookExtensions().mapNotNull { it.execGetRecommendList(1) }
                } else {
                    sourceEngine.getBookExtensions().mapNotNull { it.execSearch(keyword, 1) }
                }.flatMap { it.list }
                if (items.isEmpty()) BookUiState.Empty else BookUiState.Success(items, page = 1, hasMore = true)
            }.getOrElse { e ->
                Napier.w("BookScreenModel loadFirst: ${e.message}")
                BookUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        val current = uiState as? BookUiState.Success ?: return
        if (!current.hasMore || current.loadingMore) return
        uiState = current.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = current.page + 1
            val more = if (keyword.isBlank()) {
                sourceEngine.getBookExtensions().mapNotNull { it.execGetRecommendList(nextPage) }
            } else {
                sourceEngine.getBookExtensions().mapNotNull { it.execSearch(keyword, nextPage) }
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

sealed interface BookUiState {
    data object Loading : BookUiState
    data object Empty : BookUiState
    data class Error(val message: String) : BookUiState
    data class Success(
        val items: List<BookItem>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : BookUiState
}
