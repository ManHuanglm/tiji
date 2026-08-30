package com.wuji.app.ui.screen.video

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.wuji.app.source.model.PlayUrl
import com.wuji.app.source.model.VideoDetail
import com.wuji.app.source.model.VideoItem
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 视频详情页 - 展示封面、元信息、简介,以及播放源/线路列表。
 * 点击播放地址可调用系统/内置播放器播放。
 */
data class VideoDetailScreen(val item: VideoItem) : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<VideoDetailScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(item) { model.load(item) }
        Scaffold(
            topBar = { AppTopBar(title = item.title ?: "视频详情", onBack = { navigator?.pop() }) },
        ) { padding ->
            when (val s = model.uiState) {
                VideoDetailUiState.Loading -> LoadingState(Modifier.padding(padding))
                is VideoDetailUiState.Error -> ErrorState(s.message, modifier = Modifier.padding(padding))
                is VideoDetailUiState.Success -> {
                    val detail = s.detail
                    LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                        // 封面 + 元信息
                        item {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                AsyncImage(
                                    model = detail.item.cover,
                                    contentDescription = detail.item.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                )
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = detail.item.title ?: "未命名",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Spacer(Modifier.height(6.dp))
                                val meta = listOfNotNull(
                                    detail.item.releaseDate?.let { "上映:$it" },
                                    detail.item.country?.let { "地区:$it" },
                                    detail.item.duration?.let { "时长:$it" },
                                    detail.item.status?.let { "状态:$it" },
                                ).joinToString(" · ")
                                if (meta.isNotBlank()) {
                                    Text(
                                        text = meta,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                detail.item.director?.let {
                                    Text(
                                        text = "导演: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                detail.item.actors?.let {
                                    Text(
                                        text = "演员: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                }
                                if (!detail.item.tags.isNullOrBlank()) {
                                    Text(
                                        text = "标签: ${detail.item.tags}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                        // 简介
                        if (!detail.intro.isNullOrBlank()) {
                            item {
                                Text(
                                    text = "剧情简介",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 16.dp, top = 4.dp),
                                )
                                Text(
                                    text = detail.intro,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                )
                            }
                        }
                        // 播放地址
                        if (detail.playUrls.isNotEmpty()) {
                            item {
                                Text(
                                    text = "播放源 (${detail.playUrls.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp),
                                )
                            }
                            items(detail.playUrls, key = { it.name + it.url }) { playUrl ->
                                PlayUrlRow(playUrl)
                            }
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayUrlRow(playUrl: PlayUrl) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        ) {
            Text(
                text = playUrl.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            // TODO: 接入平台播放器 Intent / VideoView,此处为占位按钮
            OutlinedButton(onClick = { /* 调起播放 */ }) {
                Text("播放")
            }
        }
    }
}

class VideoDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {
    var uiState by mutableStateOf<VideoDetailUiState>(VideoDetailUiState.Loading)
        private set

    fun load(item: VideoItem) {
        uiState = VideoDetailUiState.Loading
        screenModelScope.launch {
            uiState = runCatching {
                val ext = sourceEngine.getVideoExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.getVideoDetail(item)
                if (detail == null) {
                    VideoDetailUiState.Error("未获取到视频详情")
                } else {
                    VideoDetailUiState.Success(detail)
                }
            }.getOrElse { e ->
                Napier.w("VideoDetailScreenModel load: ${e.message}")
                VideoDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }
}

sealed interface VideoDetailUiState {
    data object Loading : VideoDetailUiState
    data class Error(val message: String) : VideoDetailUiState
    data class Success(val detail: VideoDetail) : VideoDetailUiState
}
