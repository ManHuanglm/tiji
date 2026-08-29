package com.wuji.app.ui.screen.comic

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.source.model.ComicItem
import com.wuji.app.ui.components.CoverImage
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.screen.comic.ComicDetailUiState.Success as ComicSuccess
import com.wuji.app.ui.screen.comic.ComicReaderState.Ready as ReaderReady
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/** 漫画详情页 - 封面简介 + 章节列表,点击章节进入图片阅读器 */
data class ComicDetailScreen(val item: ComicItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<ComicDetailScreenModel>()
        LaunchedEffect(item) { model.load(item) }

        val reader = model.readerState
        if (reader != null) {
            ReaderOverlay(reader, onClose = model::closeReader, onRetry = {
                model.openChapter(item, reader.chapter)
            })
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(item.title ?: "漫画详情") },
                    navigationIcon = {
                        IconButton({ nav?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    },
                )
            },
        ) { pad ->
            when (val s = model.uiState) {
                ComicDetailUiState.Loading -> LoadingState()
                is ComicDetailUiState.Error -> ErrorState(s.message) { model.load(item) }
                is ComicSuccess -> {
                    val d = s.detail
                    LazyColumn(
                        Modifier.fillMaxSize().padding(pad),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        item {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CoverImage(url = d.item.cover, contentDescription = d.item.title)
                                Text(d.item.title ?: "", style = MaterialTheme.typography.titleMedium)
                                Text(
                                    buildString {
                                        d.item.author?.let { append("作者:$it  ") }
                                        if (!d.item.status.isNullOrBlank()) append("状态:${d.item.status}  ")
                                        if (!d.item.tags.isNullOrBlank()) append("标签:${d.item.tags}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                                if (!d.intro.isNullOrBlank()) Text(d.intro!!)
                            }
                        }
                        item {
                            Text(
                                "目录 (${d.chapters.size})",
                                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        items(d.chapters) { ch ->
                            androidx.compose.material3.ListItem(
                                headlineContent = { Text(ch.title) },
                                supportingContent = { Text("第 ${ch.index} 话", color = MaterialTheme.colorScheme.outline) },
                                modifier = Modifier.fillMaxWidth()
                                    .then(androidx.compose.foundation.clickable { model.openChapter(item, ch) }),
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderOverlay(
    state: ComicReaderState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.chapter.title) },
                navigationIcon = {
                    IconButton(onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "关闭") }
                },
            )
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().background(Color.Black).padding(pad)) {
            when (state) {
                is ComicReaderState.Loading -> {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                is ComicReaderState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(state.message, color = Color.White)
                            androidx.compose.material3.TextButton(onRetry) { Text("重试", color = Color.White) }
                        }
                    }
                }
                is ReaderReady -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        items(state.images) { url ->
                            KamelImage(
                                resource = asyncPainterResource(url),
                                contentDescription = null,
                                contentScale = ContentScale.FillWidth,
                                onLoading = {
                                    Box(
                                        Modifier.fillMaxWidth().padding(48.dp),
                                        contentAlignment = Alignment.Center,
                                    ) { CircularProgressIndicator(color = Color.White) }
                                },
                                onFailure = { EmptyState("图片加载失败") },
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                    }
                }
            }
        }
    }
}
