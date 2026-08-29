package com.wuji.app.ui.screen.comic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.ComicDetail
import com.wuji.app.source.model.ComicItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/** 漫画详情 ScreenModel - 详情 + 章节漫画图片流 */
class ComicDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<ComicDetailUiState>(ComicDetailUiState.Loading)
        private set

    var readerState by mutableStateOf<ComicReaderState?>(null)
        private set

    fun load(item: ComicItem) {
        uiState = ComicDetailUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val ext = sourceEngine.getComicExtensions().firstOrNull { it.id == item.sourceId }
                val d = ext?.execGetComicDetail(item)
                if (d == null) ComicDetailUiState.Error("未找到漫画详情")
                else ComicDetailUiState.Success(d)
            } catch (e: Exception) {
                ComicDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun openChapter(item: ComicItem, chapter: ChapterInfo) {
        readerState = ComicReaderState.Loading(chapter)
        screenModelScope.launch {
            readerState = try {
                val ext = sourceEngine.getComicExtensions().firstOrNull { it.id == item.sourceId }
                val imgs = ext?.execGetChapterImages(chapter)
                if (imgs.isNullOrEmpty()) ComicReaderState.Error("章节图片为空", chapter)
                else ComicReaderState.Ready(chapter, imgs)
            } catch (e: Exception) {
                Napier.w("open comic chapter: ${e.message}")
                ComicReaderState.Error(e.message ?: "加载失败", chapter)
            }
        }
    }

    fun closeReader() { readerState = null }
}

sealed interface ComicDetailUiState {
    data object Loading : ComicDetailUiState
    data class Error(val message: String) : ComicDetailUiState
    data class Success(val detail: ComicDetail) : ComicDetailUiState
}

/** 漫画阅读器态 */
sealed interface ComicReaderState {
    val chapter: ChapterInfo
    data class Loading(override val chapter: ChapterInfo) : ComicReaderState
    data class Error(val message: String, override val chapter: ChapterInfo) : ComicReaderState
    data class Ready(override val chapter: ChapterInfo, val images: List<String>) : ComicReaderState
}
