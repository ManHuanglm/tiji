package com.wuji.app.ui.screen.book

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ChapterInfo
import com.wuji.app.ui.components.CoverImage
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.screen.book.BookDetailUiState.Success as BookSuccess
import com.wuji.app.ui.screen.book.ReaderState.Ready as ReaderReady

/**
 * 书籍详情页 - 对齐原项目 book/detail.vue:
 * 左侧封面/简介,右侧章节列表,点击章节进入阅读器(内置页内阅读器)。
 */
data class BookDetailScreen(val item: BookItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<BookDetailScreenModel>()
        LaunchedEffect(item) { model.load(item) }

        val reader = model.readerState
        if (reader != null) {
            ReaderOverlay(
                readerState = reader,
                onClose = model::closeReader,
                onRetry = { model.openChapter(item, reader.chapter) },
            )
            return
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(item.title ?: "书籍详情") },
                    navigationIcon = {
                        IconButton({ nav?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    },
                )
            },
        ) { pad ->
            when (val s = model.uiState) {
                BookDetailUiState.Loading -> LoadingState()
                is BookDetailUiState.Error -> ErrorState(s.message) { model.load(item) }
                is BookSuccess -> {
                    val d = s.detail
                    LazyColumn(
                        Modifier.fillMaxSize().padding(pad),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        // 基本信息
                        item {
                            Column(
                                Modifier.fillMaxWidth().padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                CoverImage(url = d.item.cover, contentDescription = d.item.title)
                                Text(
                                    d.item.title ?: "",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                )
                                Text(
                                    buildString {
                                        d.item.author?.let { append("作者:$it  ") }
                                        if (!d.item.status.isNullOrBlank()) append("状态:${d.item.status}  ")
                                        if (!d.item.tags.isNullOrBlank()) append("标签:${d.item.tags}")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (!d.intro.isNullOrBlank()) Text(
                                    d.intro!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                        // 章节列表
                        item {
                            Text(
                                "目录 (${d.chapters.size})",
                                Modifier.padding(horizontal = 16.dp),
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                        items(d.chapters) { ch ->
                            ChapterRow(ch) { model.openChapter(item, ch) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChapterRow(chapter: ChapterInfo, onClick: () -> Unit) {
    androidx.compose.material3.ListItem(
        headlineContent = { Text(chapter.title) },
        supportingContent = { Text("第 ${chapter.index} 章", color = MaterialTheme.colorScheme.outline) },
        modifier = Modifier.fillMaxWidth().clickableCompat(onClick),
    )
}

@Composable
private fun Modifier.clickableCompat(onClick: () -> Unit): Modifier =
    this.then(androidx.compose.foundation.clickable(onClick = onClick))

/** 页内阅读器浮层(占满窗口,关闭时弹出详情) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderOverlay(
    readerState: ReaderState,
    onClose: () -> Unit,
    onRetry: () -> Unit,
) {
    var fontSizeSp by remember { mutableStateOf(18.sp) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(readerState.chapter.title) },
                navigationIcon = {
                    IconButton(onClose) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "关闭") }
                },
                actions = {
                    IconButton(onClick = { fontSizeSp = (fontSizeSp.value - 1).coerceAtLeast(12f).sp }) {
                        Text("A-")
                    }
                    IconButton(onClick = { fontSizeSp = (fontSizeSp.value + 1).coerceAtMost(32f).sp }) {
                        Text("A+")
                    }
                },
            )
        },
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when (readerState) {
                is ReaderState.Loading -> LoadingState()
                is ReaderState.Error -> ErrorState(readerState.message, onRetry)
                is ReaderReady -> {
                    SelectionContainer {
                        Text(
                            readerState.content,
                            modifier = Modifier.padding(16.dp).fillMaxSize(),
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = fontSizeSp.times(1.7f)),
                            fontSize = fontSizeSp,
                        )
                    }
                }
            }
        }
    }
}
