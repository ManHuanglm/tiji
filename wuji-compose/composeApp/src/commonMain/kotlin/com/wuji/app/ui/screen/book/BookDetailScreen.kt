package com.wuji.app.ui.screen.book

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
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
 * 书籍详情页 - 展示封面、简介、章节列表。
 * 点击章节进入阅读器。
 */
data class BookDetailScreen(val item: BookItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = koinScreenModel<BookDetailScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(item) { model.load(item) }
        Scaffold(
            topBar = { AppTopBar(title = item.title ?: "书籍详情", onBack = { navigator?.pop() }) },
        ) { padding ->
            when (val s = model.uiState) {
                BookDetailUiState.Loading -> LoadingState(Modifier.padding(padding))
                is BookDetailUiState.Error -> ErrorState(s.message, modifier = Modifier.padding(padding))
                is BookDetailUiState.Success -> {
                    val detail = s.detail
                    LazyColumn(
                        Modifier.fillMaxSize().padding(padding),
                    ) {
                        // 头部:封面 + 元信息
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(16.dp),
                                verticalAlignment = Alignment.Top,
                            ) {
                                AsyncImage(
                                    model = detail.item.cover,
                                    contentDescription = detail.item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(144.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Column(Modifier.padding(start = 12.dp).weight(1f)) {
                                    Text(
                                        text = detail.item.title ?: "未命名",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = "作者: ${detail.item.author ?: "未知"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "状态: ${detail.item.status ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Text(
                                        text = "最新: ${detail.item.latestChapter ?: "-"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                            }
                        }
                        // 简介
                        if (!detail.intro.isNullOrBlank()) {
                            item {
                                Text(
                                    text = detail.intro,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }
                        // 章节列表标题
                        item {
                            Text(
                                text = "目录 (${detail.chapters.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            )
                        }
                        // 章节项
                        items(detail.chapters, key = { it.id }) { chapter ->
                            Card(
                                onClick = { navigator?.push(BookReaderScreen(item, chapter, detail)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp),
                            ) {
                                Text(
                                    text = chapter.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(12.dp),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

class BookDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {
    var uiState by mutableStateOf<BookDetailUiState>(BookDetailUiState.Loading)
        private set

    fun load(item: BookItem) {
        uiState = BookDetailUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getBookExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.getBookDetail(item)
                if (detail == null || detail.chapters.isEmpty()) {
                    BookDetailUiState.Error("未获取到书籍信息或章节列表")
                } else {
                    BookDetailUiState.Success(detail)
                }
            }.getOrElse { e ->
                Napier.w("BookDetailScreenModel load: ${e.message}")
                BookDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}

sealed interface BookDetailUiState {
    data object Loading : BookDetailUiState
    data class Error(val message: String) : BookDetailUiState
    data class Success(val detail: BookDetail) : BookDetailUiState
}
