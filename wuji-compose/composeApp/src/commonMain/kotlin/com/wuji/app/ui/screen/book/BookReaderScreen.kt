package com.wuji.app.ui.screen.book

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.BookDetail
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 书籍阅读器 - 展示章节正文文本,支持上一章/下一章切换。
 */
data class BookReaderScreen(
    val bookItem: BookItem,
    val chapter: ChapterInfo,
    val bookDetail: BookDetail,
) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = koinScreenModel<BookReaderScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(bookItem, chapter) { model.loadContent(bookItem, chapter) }
        val currentIdx = bookDetail.chapters.indexOfFirst { it.id == chapter.id }.coerceAtLeast(0)
        Scaffold(
            topBar = {
                AppTopBar(
                    title = chapter.title,
                    onBack = { navigator?.pop() },
                    actions = {
                        IconButton(
                            enabled = currentIdx > 0,
                            onClick = {
                                if (currentIdx > 0) {
                                    val prev = bookDetail.chapters[currentIdx - 1]
                                    navigator?.replace(BookReaderScreen(bookItem, prev, bookDetail))
                                }
                            },
                        ) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "上一章") }
                        IconButton(
                            enabled = currentIdx < bookDetail.chapters.lastIndex,
                            onClick = {
                                if (currentIdx < bookDetail.chapters.lastIndex) {
                                    val next = bookDetail.chapters[currentIdx + 1]
                                    navigator?.replace(BookReaderScreen(bookItem, next, bookDetail))
                                }
                            },
                        ) { Icon(Icons.Outlined.ChevronRight, contentDescription = "下一章") }
                    },
                )
            },
        ) { padding ->
            when (val s = model.uiState) {
                ReaderUiState.Loading -> LoadingState(Modifier.padding(padding))
                is ReaderUiState.Error -> ErrorState(s.message, modifier = Modifier.padding(padding))
                is ReaderUiState.Empty -> EmptyState("本章节暂无正文", modifier = Modifier.padding(padding))
                is ReaderUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(padding),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                    ) {
                        item {
                            Text(
                                text = s.content,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    lineHeight = 28.sp,
                                    fontWeight = FontWeight.Normal,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

class BookReaderScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {
    var uiState by mutableStateOf<ReaderUiState>(ReaderUiState.Loading)
        private set

    fun loadContent(bookItem: BookItem, chapter: ChapterInfo) {
        uiState = ReaderUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getBookExtensions().firstOrNull { it.id == bookItem.sourceId }
                val content = ext?.getChapterContent(chapter).orEmpty()
                if (content.isBlank()) ReaderUiState.Empty else ReaderUiState.Success(content)
            }.getOrElse { e ->
                Napier.w("BookReaderScreenModel loadContent: ${e.message}")
                ReaderUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Error(val message: String) : ReaderUiState
    data object Empty : ReaderUiState
    data class Success(val content: String) : ReaderUiState
}
