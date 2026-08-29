package com.wuji.app.ui.screen.video

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.PlayUrl
import com.wuji.app.source.model.VideoDetail
import com.wuji.app.source.model.VideoItem
import kotlinx.coroutines.launch

/** 视频详情 ScreenModel - 详情 + 多线路 */
class VideoDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<VideoPlayerUiState>(VideoPlayerUiState.Loading)
        private set

    var selectedLine by mutableStateOf<PlayUrl?>(null)
        private set

    fun load(item: VideoItem) {
        uiState = VideoPlayerUiState.Loading
        selectedLine = null
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getVideoExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.execGetVideoDetail(item)
                if (detail == null) VideoPlayerUiState.Error("未找到影视详情")
                else {
                    selectedLine = detail.playUrls.firstOrNull()
                    VideoPlayerUiState.Ready(detail)
                }
            }.getOrElse { VideoPlayerUiState.Error(it.message ?: "加载失败") }
        }
    }

    fun selectLine(line: PlayUrl) { selectedLine = line }
}

sealed interface VideoPlayerUiState {
    data object Loading : VideoPlayerUiState
    data class Error(val message: String) : VideoPlayerUiState
    data class Ready(val detail: VideoDetail) : VideoPlayerUiState
}
