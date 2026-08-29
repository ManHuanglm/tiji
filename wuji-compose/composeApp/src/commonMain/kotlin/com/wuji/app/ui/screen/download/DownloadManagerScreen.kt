package com.wuji.app.ui.screen.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.download.DownloadStatus
import com.wuji.app.ui.components.EmptyState

/**
 * 下载管理页 - 队列、进度、暂停/恢复/取消/删除操作。
 * 对齐原项目 download/index.vue。
 */
object DownloadManagerScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val nav = LocalNavigator.current
        val model = koinScreenModel<DownloadManagerScreenModel>()
        var title by remember { mutableStateOf("示例视频 BigBuckBunny") }
        var url by remember { mutableStateOf(
            "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
        ) }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("下载管理") },
                    navigationIcon = {
                        IconButton({ nav?.pop() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "返回")
                        }
                    },
                )
            },
        ) { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // 新增任务条
                Column(
                    Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    OutlinedTextField(
                        value = title, onValueChange = { title = it },
                        label = { Text("任务标题") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = url, onValueChange = { url = it },
                        label = { Text("下载 URL") },
                        modifier = Modifier.fillMaxWidth(),
                    )
                    androidx.compose.material3.FilledTonalButton(
                        onClick = { model.addMockTask(title.ifBlank { "未命名任务" }, url) },
                        enabled = url.isNotBlank(),
                    ) { Text("新建下载任务") }
                }

                if (model.tasks.isEmpty()) {
                    EmptyState("暂无下载任务,先新建一条试试")
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth().weight(1f),
                    ) {
                        items(model.tasks, key = { it.task.id }) { s ->
                            DownloadRow(s,
                                onPause = { model.pause(s.task.id) },
                                onResume = { model.resume(s.task.id) },
                                onCancel = { model.cancel(s.task.id) },
                                onDelete = { model.remove(s.task.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DownloadRow(
    s: DownloadStatus,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(
                    s.task.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                val statusText = when (s) {
                    is DownloadStatus.Waiting -> "等待中"
                    is DownloadStatus.Running -> "下载中 ${(s.progress * 100).toInt()}%"
                    is DownloadStatus.Paused -> "已暂停 ${(s.progress * 100).toInt()}%"
                    is DownloadStatus.Completed -> "已完成:${s.savedPath.takeLast(32)}"
                    is DownloadStatus.Failed -> "失败:${s.error.take(32)}"
                    is DownloadStatus.Canceled -> "已取消"
                }
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
            when (s) {
                is DownloadStatus.Running -> IconButton(onPause) {
                    Icon(Icons.Outlined.Pause, "暂停")
                }
                is DownloadStatus.Paused, is DownloadStatus.Failed, is DownloadStatus.Canceled ->
                    IconButton(onResume) { Icon(Icons.Outlined.PlayArrow, "恢复/重试") }
                is DownloadStatus.Waiting, is DownloadStatus.Completed -> {}
            }
            IconButton(onCancel) { Icon(Icons.Outlined.Cancel, "取消") }
            IconButton(onDelete) { Icon(Icons.Outlined.Delete, "移除") }
        }
        val p = when (s) {
            is DownloadStatus.Running -> s.progress
            is DownloadStatus.Paused -> s.progress
            is DownloadStatus.Completed -> 1f
            else -> 0f
        }
        LinearProgressIndicator(
            progress = { p },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
