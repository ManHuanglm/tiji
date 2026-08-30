package com.wuji.app.ui.screen.song

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import coil3.compose.AsyncImage
import com.wuji.app.source.model.SongInfo
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState

/**
 * 音乐列表页 - 对齐原项目 song/index.vue。
 * 含搜索栏、歌曲列表、底部简易播放器栏。
 */
object SongScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SongScreenModel>()
        LaunchedEffect(Unit) { model.loadFirst() }
        SongContent(
            state = model.uiState,
            keyword = model.keyword,
            onKeywordChange = model::onKeywordChange,
            onSearch = model::search,
            onRetry = model::refresh,
            onItemClick = model::playSong,
            currentSong = model.currentSong,
            isPlaying = model.isPlaying,
            onTogglePlay = model::togglePlay,
        )
    }
}

@Composable
internal fun SongContent(
    state: SongUiState,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (SongInfo) -> Unit,
    currentSong: SongInfo?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
) {
    val listState = rememberLazyListState()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("搜索歌曲 / 歌手") },
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
        )
        Box(Modifier.weight(1f)) {
            when (state) {
                is SongUiState.Loading -> LoadingState()
                is SongUiState.Empty -> EmptyState("暂无音乐,请先导入音乐订阅源")
                is SongUiState.Error -> ErrorState(state.message, onRetry)
                is SongUiState.Success -> LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id + it.sourceId }) { item ->
                        SongRow(
                            song = item,
                            onClick = { onItemClick(item) },
                            isCurrent = currentSong?.id == item.id && currentSong?.sourceId == item.sourceId,
                        )
                    }
                }
            }
        }
        // 底部迷你播放器栏
        MiniPlayerBar(currentSong, isPlaying, onTogglePlay)
    }
}

@Composable
private fun SongRow(song: SongInfo, onClick: () -> Unit, isCurrent: Boolean) {
    val bgColor = if (isCurrent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = bgColor),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = song.cover,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp)),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = song.title ?: "未命名歌曲",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = song.author ?: "未知歌手",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = song.duration?.let { formatDuration(it) } ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
    }
}

@Composable
private fun MiniPlayerBar(
    currentSong: SongInfo?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
) {
    if (currentSong == null) return
    Card(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Row(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = currentSong.cover,
                contentDescription = currentSong.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)),
            )
            Column(Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = currentSong.title ?: "未命名",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = currentSong.author ?: "未知",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            IconButton(onClick = onTogglePlay) {
                Icon(
                    imageVector = if (isPlaying) Icons.Outlined.Pause else Icons.Outlined.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

/** 毫秒 → mm:ss */
private fun formatDuration(ms: Long): String {
    val totalSec = ms / 1000
    val m = totalSec / 60
    val s = totalSec % 60
    return "%02d:%02d".format(m, s)
}
