package com.wuji.app.ui.screen.photo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.PhotoItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

class PhotoDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<PhotoDetailUiState>(PhotoDetailUiState.Loading)
        private set

    fun load(item: PhotoItem) {
        uiState = PhotoDetailUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val ext = sourceEngine.getPhotoExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.execGetPhotoDetail(item)
                val photos = detail?.photos ?: emptyList()
                if (photos.isEmpty()) PhotoDetailUiState.Error("未获取到图片") else PhotoDetailUiState.Success(photos)
            } catch (e: Exception) {
                Napier.w("PhotoDetail load: ${e.message}")
                PhotoDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}
