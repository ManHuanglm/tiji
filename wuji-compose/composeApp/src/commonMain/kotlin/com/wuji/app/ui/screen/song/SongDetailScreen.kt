package com.wuji.app.ui.screen.song

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material.icons.outlined.Stop
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.SongInfo
import com.wuji.app.ui.components.CoverImage
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.screen.song.SongPlayerUiState.Ready as SongReady

/**
 * 单首歌播放页 - 专辑封面 + 播放控制 + 歌词。
 * 由于未引入跨平台音视频库,播放/停止通过状态切换控制,
 * 并将当前真实播放 URL 暴露(实际平台音频接入走平台 actual: Android MediaPlayer / Desktop JFXClip)。
 */
data class SongDetailScreen(val song: SongInfo) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<SongDetailScreenModel>()
        LaunchedEffect(song) { model.load(song) }
        var lyric by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(song.title ?: "歌曲详情") },
                    navigationIcon = {
                        IconButton(onClick = { nav?.pop() }) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回") }
                    },
                )
            },
        ) { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                when (val s = model.uiState) {
                    SongPlayerUiState.Loading -> CircularProgressIndicator(Modifier.padding(24.dp))
                    is SongPlayerUiState.Error -> ErrorState(message = s.message, onRetry = { model.load(song) })
                    is SongReady -> {
                        LaunchedEffect(s) { lyric = s.lyric }
                        // 封面
                        Box(
                            Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                        ) {
                            CoverImage(url = s.song.cover, contentDescription = s.song.title)
                        }
                        Spacer(Modifier.height(16.dp))
                        Text(
                            s.song.title ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            s.song.author ?: "",
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            s.song.desc?.let { "简介: $it" }.orEmpty(),
                            color = MaterialTheme.colorScheme.outline,
                        )
                        Spacer(Modifier.height(16.dp))

                        // 控制区
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            IconButton(onClick = { /*TODO: 上一首(队列再接入)*/ }) {
                                Icon(Icons.Outlined.SkipPrevious, "上一首")
                            }
                            FilledIconButton(
                                onClick = model::togglePlay,
                                modifier = Modifier.size(64.dp),
                            ) {
                                Icon(
                                    if (model.isPlaying) Icons.Outlined.Stop else Icons.Outlined.PlayArrow,
                                    contentDescription = if (model.isPlaying) "暂停" else "播放",
                                    modifier = Modifier.size(32.dp),
                                )
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Outlined.SkipNext, "下一首")
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            "播放地址:${s.playUrl.take(64)}…",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline,
                        )

                        Spacer(Modifier.height(16.dp))
                        // 歌词区
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                                .verticalScroll(rememberScrollState())
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(16.dp),
                        ) {
                            val lines = lyric.lines().filter { it.isNotBlank() }
                            if (lines.isEmpty()) {
                                Text("暂无歌词", color = MaterialTheme.colorScheme.outline)
                            } else {
                                lines.forEachIndexed { idx, line ->
                                    Text(
                                        line,
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.padding(vertical = 3.dp),
                                        color = if (idx == 6) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
