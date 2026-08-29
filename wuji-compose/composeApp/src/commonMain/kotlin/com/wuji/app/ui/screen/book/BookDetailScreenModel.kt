package com.wuji.app.ui.screen.book

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.BookDetail
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ChapterInfo
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/** 书籍详情 ScreenModel - 加载书目、进入指定章节阅读器 */
class BookDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<BookDetailUiState>(BookDetailUiState.Loading)
        private set

    /** 阅读器态 (null 时未打开) */
    var readerState by mutableStateOf<ReaderState?>(null)
        private set

    fun load(item: BookItem) {
        uiState = BookDetailUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val ext = sourceEngine.getBookExtensions().firstOrNull { it.id == item.sourceId }
                val d = ext?.execGetBookDetail(item)
                if (d == null) BookDetailUiState.Error("未找到书籍详情")
                else BookDetailUiState.Success(d)
            } catch (e: Exception) {
                Napier.w("load book detail: ${e.message}")
                BookDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /** 打开阅读器(加载章节内容) */
    fun openChapter(item: BookItem, chapter: ChapterInfo) {
        readerState = ReaderState.Loading(chapter)
        screenModelScope.launch {
            readerState = try {
                val ext = sourceEngine.getBookExtensions().firstOrNull { it.id == item.sourceId }
                val text = ext?.execGetChapterContent(chapter)
                if (text.isNullOrBlank()) ReaderState.Error("章节内容为空", chapter)
                else ReaderState.Ready(chapter, text)
            } catch (e: Exception) {
                ReaderState.Error(e.message ?: "加载失败", chapter)
            }
        }
    }

    fun closeReader() { readerState = null }
}

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState
    data class Error(val message: String) : BookDetailUiState
    data class Success(val detail: BookDetail) : BookDetailUiState
}

/** 阅读器章节内容状态 */
sealed interface ReaderState {
    val chapter: ChapterInfo
    data class Loading(override val chapter: ChapterInfo) : ReaderState
    data class Error(val message: String, override val chapter: ChapterInfo) : ReaderState
    data class Ready(override val chapter: ChapterInfo, val content: String) : ReaderState
}
