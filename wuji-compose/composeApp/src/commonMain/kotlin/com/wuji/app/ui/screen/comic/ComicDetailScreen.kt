package com.wuji.app.ui.screen.comic

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
 * 漫画详情页 - 展示封面 + 简介 + 章节列表。
 */
data class ComicDetailScreen(val item: ComicItem) : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<ComicDetailScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(item) { model.load(item) }
        Scaffold(
            topBar = { AppTopBar(title = item.title ?: "漫画详情", onBack = { navigator?.pop() }) },
        ) { padding ->
            when (val s = model.uiState) {
                ComicDetailUiState.Loading -> LoadingState(Modifier.padding(padding))
                is ComicDetailUiState.Error -> ErrorState(s.message, modifier = Modifier.padding(padding))
                is ComicDetailUiState.Success -> {
                    val detail = s.detail
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
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
                        item {
                            Text(
                                text = "目录 (${detail.chapters.size})",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                            )
                        }
                        items(detail.chapters, key = { it.id }) { chapter ->
                            Card(
                                onClick = { navigator?.push(ComicReaderScreen(item, chapter, detail)) },
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

class ComicDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {
    var uiState by mutableStateOf<ComicDetailUiState>(ComicDetailUiState.Loading)
        private set

    fun load(item: ComicItem) {
        uiState = ComicDetailUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getComicExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.getComicDetail(item)
                if (detail == null || detail.chapters.isEmpty()) {
                    ComicDetailUiState.Error("未获取到漫画信息或章节列表")
                } else {
                    ComicDetailUiState.Success(detail)
                }
            }.getOrElse { e ->
                Napier.w("ComicDetailScreenModel load: ${e.message}")
                ComicDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}

sealed interface ComicDetailUiState {
    data object Loading : ComicDetailUiState
    data class Error(val message: String) : ComicDetailUiState
    data class Success(val detail: ComicDetail) : ComicDetailUiState
}
