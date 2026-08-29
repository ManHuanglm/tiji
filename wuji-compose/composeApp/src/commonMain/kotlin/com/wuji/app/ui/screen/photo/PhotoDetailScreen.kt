package com.wuji.app.ui.screen.photo

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import coil3.compose.AsyncImage
import com.wuji.app.source.model.PhotoItem
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState

/** 图片详情页 - 对齐原项目 PhotoDetail.vue:展示图集大图 */
data class PhotoDetailScreen(val item: PhotoItem) : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<PhotoDetailScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(item) { model.load(item) }
        Scaffold(
            topBar = { AppTopBar(title = item.title ?: "图片详情", onBack = { navigator?.pop() }) },
        ) { padding ->
            when (val s = model.uiState) {
                PhotoDetailUiState.Loading -> LoadingState(Modifier.padding(padding))
                is PhotoDetailUiState.Error -> ErrorState(s.message, modifier = Modifier.padding(padding))
                is PhotoDetailUiState.Success -> LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    items(s.photos) { url ->
                        AsyncImage(
                            model = url,
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        )
                    }
                }
            }
        }
    }
}

sealed interface PhotoDetailUiState {
    data object Loading : PhotoDetailUiState
    data class Error(val message: String) : PhotoDetailUiState
    data class Success(val photos: List<String>) : PhotoDetailUiState
}
