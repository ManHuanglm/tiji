package com.wuji.app.ui.screen.song

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.SongInfo
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 音乐列表页状态机 - 聚合多源歌曲推荐/搜索结果,
 * 同时维护简易播放队列与当前播放项状态。
 */
class SongScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<SongUiState>(SongUiState.Loading)
        private set

    var keyword by mutableStateOf("")
        private set

    /** 当前播放歌曲(为 null 表示未播放) */
    var currentSong by mutableStateOf<SongInfo?>(null)
        private set

    /** 是否正在播放(纯 UI 态,实际播放由平台播放器接管) */
    var isPlaying by mutableStateOf(false)
        private set

    private var currentPage = 1

    fun loadFirst() {
        currentPage = 1
        uiState = SongUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val items = if (keyword.isBlank()) {
                    sourceEngine.getSongExtensions().mapNotNull { it.execGetRecommendList(1) }
                } else {
                    sourceEngine.getSongExtensions().mapNotNull { it.execSearch(keyword, 1) }
                }.flatMap { it.list }
                if (items.isEmpty()) SongUiState.Empty else SongUiState.Success(items, page = 1, hasMore = true)
            }.getOrElse { e ->
                Napier.w("SongScreenModel loadFirst: ${e.message}")
                SongUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun loadMore() {
        val current = uiState as? SongUiState.Success ?: return
        if (!current.hasMore || current.loadingMore) return
        uiState = current.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = current.page + 1
            val more = if (keyword.isBlank()) {
                sourceEngine.getSongExtensions().mapNotNull { it.execGetRecommendList(nextPage) }
            } else {
                sourceEngine.getSongExtensions().mapNotNull { it.execSearch(keyword, nextPage) }
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

    /** 点击播放某首:切歌并标记为播放中 */
    fun playSong(song: SongInfo) {
        currentSong = song
        isPlaying = true
    }

    fun togglePlay() {
        if (currentSong != null) isPlaying = !isPlaying
    }
}

sealed interface SongUiState {
    data object Loading : SongUiState
    data object Empty : SongUiState
    data class Error(val message: String) : SongUiState
    data class Success(
        val items: List<SongInfo>,
        val page: Int,
        val hasMore: Boolean,
        val loadingMore: Boolean = false,
    ) : SongUiState
}
