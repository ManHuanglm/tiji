package com.wuji.app.ui.screen.book

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.BookItem
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.components.ResourceCard

/**
 * 书籍列表页 - 对齐原项目 book/index.vue。
 * 书籍推荐/搜索卡片网格,点击进入书籍详情页(章节列表)。
 */
object BookScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<BookScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(Unit) { model.loadFirst() }
        BookContent(
            state = model.uiState,
            keyword = model.keyword,
            onKeywordChange = model::onKeywordChange,
            onSearch = model::search,
            onRetry = model::refresh,
            onItemClick = { item ->
                navigator?.push(BookDetailScreen(item))
            },
        )
    }
}

@Composable
internal fun BookContent(
    state: BookUiState,
    keyword: String,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onItemClick: (BookItem) -> Unit,
) {
    val gridState = rememberLazyGridState()
    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = keyword,
            onValueChange = onKeywordChange,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            placeholder = { Text("搜索书名 / 作者") },
            trailingIcon = {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Outlined.Search, contentDescription = "搜索")
                }
            },
            singleLine = true,
        )
        Box(Modifier.weight(1f)) {
            when (state) {
                is BookUiState.Loading -> LoadingState()
                is BookUiState.Empty -> EmptyState("暂无书籍,请先导入书籍订阅源")
                is BookUiState.Error -> ErrorState(state.message, onRetry)
                is BookUiState.Success -> LazyVerticalGrid(
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
                            coverUrl = item.cover,
                            subtitle = item.author ?: item.latestChapter,
                            onClick = { onItemClick(item) },
                            aspect = 0.72f,
                        )
                    }
                }
            }
        }
    }
}
