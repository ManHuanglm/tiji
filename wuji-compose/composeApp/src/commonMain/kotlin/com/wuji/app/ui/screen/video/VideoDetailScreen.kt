package com.wuji.app.ui.screen.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.core.platform.openUrlExternal
import com.wuji.app.source.model.PlayUrl
import com.wuji.app.source.model.VideoItem
import com.wuji.app.ui.components.CoverImage
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.screen.video.VideoPlayerUiState.Ready as VideoReady

/**
 * 视频详情页 - 播放器占位壳 + 多线路切换 + 简介。
 * 暂不内置播放器(需平台 actual),提供「在系统浏览器/默认播放器打开」入口。
 * 后续可接入 VLC MPV 或 Compose 原生 Player。
 */
data class VideoDetailScreen(val item: VideoItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<VideoDetailScreenModel>()
        LaunchedEffect(item) { model.load(item) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(item.title ?: "影视详情") },
                    navigationIcon = {
                        IconButton(onClick = { nav?.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                    },
                )
            },
        ) { pad ->
            when (val s = model.uiState) {
                VideoPlayerUiState.Loading -> LoadingState()
                is VideoPlayerUiState.Error -> ErrorState(s.message) { model.load(item) }
                is VideoReady -> {
                    val d = s.detail
                    LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                        // 播放器壳
                        item {
                            PlayerShell(
                                line = model.selectedLine,
                                onOpenExternal = { line -> line?.let { openUrlExternal(it.url) } },
                            )
                        }
                        // 线路切换
                        item {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("播放线路", style = MaterialTheme.typography.titleSmall)
                                if (d.playUrls.isEmpty()) EmptyState("暂无播放线路")
                                else d.playUrls.forEach { line ->
                                    Row(
                                        Modifier
                                            .fillMaxWidth()
                                            .selectable(
                                                selected = model.selectedLine?.url == line.url,
                                                onClick = { model.selectLine(line) },
                                            )
                                            .padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        RadioButton(
                                            selected = model.selectedLine?.url == line.url,
                                            onClick = { model.selectLine(line) },
                                        )
                                        Spacer(Modifier.padding(8.dp))
                                        Column {
                                            Text(line.name)
                                            Text(
                                                line.url.take(72) + if (line.url.length > 72) "…" else "",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        // 简介
                        item {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                CoverImage(url = d.item.cover, contentDescription = d.item.title)
                                Text(d.item.title ?: "", style = MaterialTheme.typography.titleMedium)
                                AssistChip(
                                    onClick = {},
                                    label = { Text(d.item.status ?: "未知状态") },
                                    leadingIcon = { Icon(Icons.Outlined.PlayArrow, null) },
                                )
                                val meta = buildString {
                                    d.item.director?.let { appendLine("导演: $it") }
                                    d.item.actors?.let { appendLine("演员: $it") }
                                    appendLine(
                                        buildString {
                                            d.item.releaseDate?.let { append("上映: $it  ") }
                                            d.item.country?.let { append("地区: $it  ") }
                                            d.item.duration?.let { append("时长: $it") }
                                        },
                                    )
                                    if (!d.item.tags.isNullOrBlank()) appendLine("标签: ${d.item.tags}")
                                }.trim()
                                Text(meta, color = MaterialTheme.colorScheme.outline)
                                if (!d.intro.isNullOrBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text("剧情简介", style = MaterialTheme.typography.titleSmall)
                                    Text(d.intro!!)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerShell(line: PlayUrl?, onOpenExternal: (PlayUrl?) -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black),
        contentAlignment = Alignment.Center,
    ) {
        if (line == null) {
            Text("选择线路后播放", color = Color.White)
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.PlayArrow, contentDescription = null, tint = Color.White,
                    modifier = Modifier.size(64.dp))
                Text(
                    "点击下方按钮用系统播放器打开",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(8.dp))
                FilledTonalButton(onClick = { onOpenExternal(line) }) {
                    Icon(Icons.Outlined.OpenInBrowser, null)
                    Spacer(Modifier.padding(4.dp))
                    Text("在系统播放器/浏览器打开")
                }
            }
        }
    }
}
