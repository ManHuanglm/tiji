package com.wuji.app.ui.components

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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.wuji.app.ui.screen.ListUiState
import kotlinx.coroutines.launch

/**
 * 资源列表通用骨架 - 复用 Photo/Song/Book/Comic/Video 五资源列表:
 * 顶部搜索栏 + 分页网格 + Loading/Empty/Error 状态。
 *
 * [T] 资源条目类型;[gridAspect] 卡片宽高比(图片 1, 书/漫 0.75, 视频 0.6)。
 */
@Composable
fun <T : Any> ResourceListScaffold(
    state: ListUiState<T>,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    keyFn: (T) -> String,
    coverFn: (T) -> String?,
    titleFn: (T) -> String,
    subtitleFn: (T) -> String? = { null },
    onItemClick: (T) -> Unit,
    gridAspect: Float = 0.75f,
    placeholderHint: String = "搜索资源",
) {
    val gridState = rememberLazyGridState()
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        // 搜索栏
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text(placeholderHint) },
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
        )

        // 列表状态分发
        when (state) {
            ListUiState.Loading -> LoadingState()
            ListUiState.Empty -> EmptyState("暂无内容,请先导入订阅源")
            is ListUiState.Error -> ErrorState(state.message, onRetry)
            is ListUiState.Success<T> -> {
                // 分页监听:接近底部触发 loadMore
                val loadMoreTrigger = remember(state.page, state.loadingMore) {
                    state.items.size - 4
                }
                LaunchedEffect(gridState, state.loadingMore) {
                    androidx.compose.runtime.snapshotFlow {
                        gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
                    }.collect { lastIdx ->
                        if (!state.loadingMore && state.hasMore && lastIdx >= loadMoreTrigger) {
                            scope.launch { onLoadMore() }
                        }
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    state = gridState,
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.items, key = keyFn) { item ->
                        ResourceCard(
                            title = titleFn(item),
                            coverUrl = coverFn(item),
                            subtitle = subtitleFn(item),
                            onClick = { onItemClick(item) },
                            aspect = gridAspect,
                        )
                    }
                    if (state.loadingMore) {
                        item {
                            Box(Modifier.fillMaxWidth().padding(16.dp)) {
                                androidx.compose.material3.CircularProgressIndicator(
                                    Modifier.padding(4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
