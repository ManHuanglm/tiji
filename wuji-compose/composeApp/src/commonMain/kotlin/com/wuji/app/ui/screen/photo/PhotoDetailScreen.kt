package com.wuji.app.ui.screen.photo

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.source.model.PhotoItem
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.components.ErrorState
import com.wuji.app.ui.components.LoadingState
import com.wuji.app.ui.screen.photo.PhotoDetailUiState.Success as DetailSuccess
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import kotlinx.coroutines.flow.collectLatest

/**
 * 图片详情页 - 分页图集 + 保存图片 + 简单全屏预览(点击放大)。
 * 缩放/双指捏合:Desktop/移动端均通过 [FullscreenPhotoPreview] Dialog + 手势位移动画实现。
 * 如需更细粒度的缩放可替换为 zoomable 库。
 */
data class PhotoDetailScreen(val item: PhotoItem) : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<PhotoDetailScreenModel>()
        val snackbar = remember { SnackbarHostState() }
        var previewUrl: String? by remember { mutableStateOf(null) }

        LaunchedEffect(item) { model.load(item) }

        // 保存进度 Snackbar 提示
        LaunchedEffect(model) {
            androidx.compose.runtime.snapshotFlow { model.saveResult }.collectLatest { r ->
                when (r) {
                    is SaveResult.Ok -> {
                        snackbar.showSnackbar("已保存:${r.fileName}")
                        model.clearSaveResult()
                    }
                    is SaveResult.Fail -> {
                        snackbar.showSnackbar("保存失败:${r.message}")
                        model.clearSaveResult()
                    }
                    SaveResult.Saving, null -> {}
                }
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(item.title ?: "图片详情") },
                    navigationIcon = {
                        IconButton({ nav?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { pad ->
            Box(Modifier.padding(pad)) {
                when (val s = model.uiState) {
                    PhotoDetailUiState.Loading -> LoadingState()
                    is PhotoDetailUiState.Error -> ErrorState(s.message) { model.load(item) }
                    is DetailSuccess -> {
                        val listState = rememberLazyListState()
                        val headers = s.headers
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (!item.description.isNullOrBlank()) {
                                item {
                                    Text(
                                        item.description!!,
                                        Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                }
                            }
                            items(s.photos) { url ->
                                PhotoTile(
                                    url = url,
                                    headers = headers,
                                    onTap = { previewUrl = url },
                                    onSave = { model.savePhoto(url, headers) },
                                )
                            }
                            if (s.hasMore) {
                                item {
                                    LaunchedEffect(Unit) { model.loadNext(item) }
                                    Box(
                                        Modifier.fillMaxWidth().padding(24.dp),
                                        contentAlignment = Alignment.Center,
                                    ) { CircularProgressIndicator() }
                                }
                            }
                        }
                    }
                }

                // 全屏预览
                previewUrl?.let { url ->
                    FullscreenPhotoPreview(
                        url = url,
                        headers = (model.uiState as? DetailSuccess)?.headers,
                        onDismiss = { previewUrl = null },
                        onSave = { model.savePhoto(url, (model.uiState as? DetailSuccess)?.headers) },
                    )
                }
            }
        }
    }
}

@Composable
private fun PhotoTile(
    url: String,
    headers: Map<String, String>?,
    onTap: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.04f))
            .clickable(onClick = onTap),
    ) {
        KamelImage(
            resource = asyncPainterResource(
                data = url,
                block = { requestBuilder { headers?.forEach { (k, v) -> addHeader(k, v) } } },
            ),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            onLoading = { CircularProgressIndicator(Modifier.padding(24.dp)) },
            onFailure = { EmptyState("图片加载失败") },
            modifier = Modifier.fillMaxWidth().aspectRatio(1.0f),
        )
        IconButton(
            onClick = onSave,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
        ) {
            Icon(Icons.Outlined.Download, "保存", tint = Color.White)
        }
    }
}

@Composable
private fun FullscreenPhotoPreview(
    url: String,
    headers: Map<String, String>?,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xE6000000))
            .clickable(onClick = onDismiss),
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            KamelImage(
                resource = asyncPainterResource(
                    data = url,
                    block = { requestBuilder { headers?.forEach { (k, v) -> addHeader(k, v) } } },
                ),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                onLoading = { CircularProgressIndicator() },
                onFailure = { Text("加载失败", color = Color.White) },
                modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            )
            IconButton(onClick = onSave, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.Outlined.Download, "保存", tint = Color.White)
            }
        }
    }
}
