package com.wuji.app.ui.screen.photo

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.components.ResourceCard
import cafe.adriel.voyager.core.screen.Screen

object PhotoScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<PhotoScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(Unit) { model.loadFirst() }
        PhotoContent(
            state = model.uiState,
            keyword = model.keyword,
            onKeywordChange = model::onKeywordChange,
            onSearch = model::search,
            onRetry = model::refresh,
            onItemClick = { item ->
                navigator?.push(PhotoDetailScreen(item))
            },
        )
    }
}

@Composable
internal fun PhotoContent(
    state: PhotoUiState,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (com.wuji.app.source.model.PhotoItem) -> Unit,
) {
    val gridState = rememberLazyGridState()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { androidx.compose.material3.Text("搜索图片") },
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
        )
        when (state) {
            is PhotoUiState.Loading -> LoadingState()
            is PhotoUiState.Empty -> EmptyState("暂无图片,请先导入订阅源")
            is PhotoUiState.Error -> ErrorState(state.message, onRetry)
            is PhotoUiState.Success -> LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                state = gridState,
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                items(state.items, key = { it.id + it.sourceId }) { item ->
                    ResourceCard(
                        title = item.title ?: "未命名",
                        coverUrl = item.cover.firstOrNull(),
                        subtitle = item.author,
                        onClick = { onItemClick(item) },
                    )
                }
            }
        }
    }
}
