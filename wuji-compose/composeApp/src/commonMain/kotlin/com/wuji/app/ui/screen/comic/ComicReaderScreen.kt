package com.wuji.app.ui.screen.comic

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.ComicDetail
import com.wuji.app.source.model.ComicItem
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 漫画阅读器 - 纵向滚动浏览章节图片,支持上一章/下一章。
 */
data class ComicReaderScreen(
    val comicItem: ComicItem,
    val chapter: ChapterInfo,
    val comicDetail: ComicDetail,
) : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<ComicReaderScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(comicItem, chapter) { model.loadImages(comicItem, chapter) }
        val currentIdx = comicDetail.chapters.indexOfFirst { it.id == chapter.id }.coerceAtLeast(0)
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
                                    val prev = comicDetail.chapters[currentIdx - 1]
                                    navigator?.replace(ComicReaderScreen(comicItem, prev, comicDetail))
                                }
                            },
                        ) { Icon(Icons.Outlined.ChevronLeft, contentDescription = "上一章") }
                        IconButton(
                            enabled = currentIdx < comicDetail.chapters.lastIndex,
                            onClick = {
                                if (currentIdx < comicDetail.chapters.lastIndex) {
                                    val next = comicDetail.chapters[currentIdx + 1]
                                    navigator?.replace(ComicReaderScreen(comicItem, next, comicDetail))
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
                is ReaderUiState.Empty -> EmptyState("本章节暂无图片", modifier = Modifier.padding(padding))
                is ReaderUiState.Success -> LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    items(s.images, key = { it }) { url ->
                        Box(Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = url,
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}

class ComicReaderScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {
    var uiState by mutableStateOf<ReaderUiState>(ReaderUiState.Loading)
        private set

    fun loadImages(comicItem: ComicItem, chapter: ChapterInfo) {
        uiState = ReaderUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getComicExtensions().firstOrNull { it.id == comicItem.sourceId }
                val images = ext?.getChapterImages(chapter).orEmpty()
                if (images.isEmpty()) ReaderUiState.Empty else ReaderUiState.Success(images)
            }.getOrElse { e ->
                Napier.w("ComicReaderScreenModel loadImages: ${e.message}")
                ReaderUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}

sealed interface ReaderUiState {
    data object Loading : ReaderUiState
    data class Error(val message: String) : ReaderUiState
    data object Empty : ReaderUiState
    data class Success(val images: List<String>) : ReaderUiState
}
