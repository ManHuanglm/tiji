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
 * 单首歌详情 ScreenModel - 对齐原项目的单曲播放页:
 * - 解析播放 URL(execGetSongUrl)
 * - 加载歌词(execGetSongLyric)
 * 跨平台播放器接入通过后续 expect fun 实现,当前先给出可消费状态。
 */
class SongDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<SongPlayerUiState>(SongPlayerUiState.Loading)
        private set

    var isPlaying by mutableStateOf(false)
        private set

    fun load(song: SongInfo) {
        uiState = SongPlayerUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val ext = sourceEngine.getSongExtensions().firstOrNull { it.id == song.sourceId }
                val url = ext?.execGetSongUrl(song)
                val lyric = ext?.execGetSongLyric(song)
                if (url.isNullOrBlank()) SongPlayerUiState.Error("未获取到播放地址")
                else SongPlayerUiState.Ready(
                    song = song,
                    playUrl = url,
                    lyric = lyric ?: "",
                )
            } catch (e: Exception) {
                Napier.w("load song: ${e.message}")
                SongPlayerUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun togglePlay() { if (uiState is SongPlayerUiState.Ready) isPlaying = !isPlaying }
}

sealed interface SongPlayerUiState {
    data object Loading : SongPlayerUiState
    data class Error(val message: String) : SongPlayerUiState
    data class Ready(
        val song: SongInfo,
        val playUrl: String,
        val lyric: String,
    ) : SongPlayerUiState
}
