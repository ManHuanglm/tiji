package com.wuji.app.ui.screen.video

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.VideoItem
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.components.ResourceCard

/**
 * 视频列表页 - 对齐原项目 video/index.vue。
 */
object VideoScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<VideoScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(Unit) { model.loadFirst() }
        VideoContent(
            state = model.uiState,
            keyword = model.keyword,
            onKeywordChange = model::onKeywordChange,
            onSearch = model::search,
            onRetry = model::refresh,
            onItemClick = { item -> navigator?.push(VideoDetailScreen(item)) },
        )
    }
}

@Composable
internal fun VideoContent(
    state: VideoUiState,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (VideoItem) -> Unit,
) {
    val gridState = rememberLazyGridState()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("搜索视频 / 演员 / 导演") },
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
        )
        Box(Modifier.weight(1f)) {
            when (state) {
                is VideoUiState.Loading -> LoadingState()
                is VideoUiState.Empty -> EmptyState("暂无视频,请先导入视频订阅源")
                is VideoUiState.Error -> ErrorState(state.message, onRetry)
                is VideoUiState.Success -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 160.dp),
                    state = gridState,
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = { it.id + it.sourceId }) { item ->
                        ResourceCard(
                            title = item.title ?: "未命名",
                            coverUrl = item.cover,
                            subtitle = item.releaseDate ?: item.status,
                            onClick = { onItemClick(item) },
                            aspect = 1.4f, // 16:9 视频封面
                        )
                    }
                }
            }
        }
    }
}
