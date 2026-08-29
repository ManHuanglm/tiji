package com.wuji.app.ui.screen

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 资源列表通用状态机 - 抽象 Photo/Song/Book/Comic/Video 五资源列表的共享逻辑。
 * 子类仅需实现 [recommend] + [search] 两个差异化源调用。
 *
 * 对齐原项目各资源 store 的推荐/搜索/分页状态机。
 */
abstract class ResourceListScreenModel<T>(private val sourceName: String) : ScreenModel {

    var uiState by mutableStateOf<ListUiState<T>>(ListUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    private var currentPage = 1

    /** 分页推荐列表 */
    protected abstract suspend fun recommend(page: Int): List<T>

    /** 分页搜索 */
    protected abstract suspend fun search(kw: String, page: Int): List<T>

    /** 首次加载或刷新 */
    fun loadFirst() {
        currentPage = 1
        uiState = ListUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val items = if (keyword.isBlank()) recommend(1) else search(keyword, 1)
                if (items.isEmpty()) ListUiState.Empty else ListUiState.Success(items, page = 1, hasMore = true)
            } catch (e: Exception) {
                Napier.w("$sourceName loadFirst: ${e.message}")
                ListUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /** 上拉加载更多 */
    fun loadMore() {
        val cur = uiState as? ListUiState.Success<T> ?: return
        if (!cur.hasMore || cur.loadingMore) return
        uiState = cur.copy(loadingMore = true)
        screenModelScope.launch {
            val next = cur.page + 1
            val more = if (keyword.isBlank()) recommend(next) else search(keyword, next)
            uiState = cur.copy(
                items = cur.items + more,
                page = next,
                hasMore = more.isNotEmpty(),
                loadingMore = false,
            )
        }
    }

    fun onKeywordChange(kw: String) { keyword = kw }

    fun search() { loadFirst() }

    fun refresh() { loadFirst() }
}

/** 列表通用状态 */
sealed interface ListUiState<out T> {
    data object Loading : ListUiState<Nothing>
    data object Empty : ListUiState<Nothing>
    data class Error(val message: String) : ListUiState<Nothing>
    data class Success<T>(
        val items: List<T>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : ListUiState<T>
}
