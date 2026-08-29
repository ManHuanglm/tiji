package com.wuji.app.ui.screen.photo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.PhotoItem
import com.wuji.app.source.model.PagedList
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 图片列表页状态机 - 对齐原项目 photoStore + PhotoList.vue 的状态与加载逻辑。
 * 通过 SourceEngine 聚合多源推荐/搜索结果,支持下拉刷新与分页。
 */
class PhotoScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<PhotoUiState>(PhotoUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    private var currentPage = 1

    fun loadFirst() {
        currentPage = 1
        uiState = PhotoUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val items = if (keyword.isBlank()) {
                    sourceEngine.getPhotoExtensions().mapNotNull { it.execGetRecommendList(1) }
                } else {
                    sourceEngine.getPhotoExtensions().mapNotNull { it.execSearch(keyword, 1) }
                }.flatMap { it.list }
                if (items.isEmpty()) PhotoUiState.Empty else PhotoUiState.Success(items, page = 1, hasMore = true)
            } catch (e: Exception) {
                Napier.w("PhotoScreenModel loadFirst: ${e.message}")
                PhotoUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        val current = uiState as? PhotoUiState.Success ?: return
        if (!current.hasMore || current.loadingMore) return
        uiState = current.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = current.page + 1
            val more = if (keyword.isBlank()) {
                sourceEngine.getPhotoExtensions().mapNotNull { it.execGetRecommendList(nextPage) }
            } else {
                sourceEngine.getPhotoExtensions().mapNotNull { it.execSearch(keyword, nextPage) }
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

sealed interface PhotoUiState {
    data object Loading : PhotoUiState
    data object Empty : PhotoUiState
    data class Error(val message: String) : PhotoUiState
    data class Success(
        val items: List<PhotoItem>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : PhotoUiState
}
