package com.wuji.app.ui.screen.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.data.SubscribeSourceRepository
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.SourceFetcher
import com.wuji.app.source.model.SubscribeDetail
import com.wuji.app.source.model.SubscribeSource
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.screen.SourceMarketScreen
import com.wuji.app.ui.screen.SourceMyScreen
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

/**
 * 订阅源管理页 - 展示已导入源列表,支持:
 * 1. 通过 URL 导入订阅源(远程拉取 JSON 并保存)
 * 2. 启用/禁用开关(写入持久化)
 * 3. 删除订阅源
 * 4. 快捷入口至市场 / 我的源
 */
object SourceManageScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SourceManageScreenModel>()
        val navigator = LocalNavigator.current
        LaunchedEffect(Unit) { model.refresh() }
        var showImport by remember { mutableStateOf(false) }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = "订阅源管理",
                    onBack = { navigator?.pop() },
                    actions = {
                        IconButton(onClick = { navigator?.push(SourceMyScreen) }) {
                            Icon(Icons.Outlined.Add, contentDescription = "我的源")
                        }
                        IconButton(onClick = { navigator?.push(SourceMarketScreen) }) {
                            Icon(Icons.Outlined.Store, contentDescription = "源市场")
                        }
                    },
                )
            },
        ) { p ->
            Column(Modifier.fillMaxSize().padding(p)) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "已导入 ${model.state.value.sources.size} 个订阅源",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { showImport = true }) { Text("导入源 URL") }
                }

                if (model.state.value.sources.isEmpty()) {
                    EmptyState(
                        "暂无订阅源,请点击「导入源 URL」或前往市场添加",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(model.state.value.sources, key = { it.url }) { src ->
                            SourceItemCard(
                                src = src,
                                onToggle = { model.toggle(src) },
                                onDelete = { model.delete(src) },
                            )
                        }
                    }
                }
            }
        }

        if (showImport) {
            ImportSourceDialog(
                initialUrl = model.importUrl,
                onUrlChange = model::onImportUrlChange,
                loading = model.state.value.importLoading,
                error = model.state.value.importError,
                onDismiss = { showImport = false },
                onConfirm = {
                    model.importFromUrl()
                    if (!model.state.value.importLoading && model.state.value.importError == null) {
                        showImport = false
                    }
                },
            )
        }
    }
}

@Composable
private fun SourceItemCard(
    src: SubscribeSource,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (src.disable) {
                MaterialTheme.colorScheme.surfaceVariant
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Row(
            Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f).padding(end = 8.dp)) {
                Text(
                    text = src.detail.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = src.url,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "共 ${src.detail.urls.size} 个子源 · v${src.detail.version}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(checked = !src.disable, onCheckedChange = { onToggle() })
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun ImportSourceDialog(
    initialUrl: String,
    onUrlChange: (String) -> Unit,
    loading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入订阅源", fontWeight = FontWeight.SemiBold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("粘贴订阅源链接,支持返回 JSON 格式的订阅源内容。")
                OutlinedTextField(
                    value = initialUrl,
                    onValueChange = onUrlChange,
                    label = { Text("订阅源 URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (error != null) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = !loading && initialUrl.isNotBlank(),
            ) { Text(if (loading) "导入中..." else "导入") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        },
    )
}

data class SourceManageState(
    val sources: List<SubscribeSource> = emptyList(),
    val importLoading: Boolean = false,
    val importError: String? = null,
)

/**
 * 源管理 ScreenModel - 处理增删改查 + 远程 URL 导入。
 *
 * 导入成功后会自动触发 SourceEngine.loadSubscribeSource 注册到运行时,
 * 确保新导入源在各资源 Tab 立即可见。
 */
class SourceManageScreenModel(
    private val repo: SubscribeSourceRepository,
    private val sourceEngine: SourceEngine,
    private val fetcher: SourceFetcher,
    private val json: Json,
) : StateScreenModel<SourceManageState>(SourceManageState()) {

    var importUrl by mutableStateOf("")
        private set

    fun onImportUrlChange(v: String) { importUrl = v }

    fun refresh() {
        mutableState.value = SourceManageState(sources = repo.loadAll())
    }

    fun toggle(src: SubscribeSource) {
        val updated = src.copy(disable = !src.disable)
        repo.upsert(updated)
        if (updated.disable) {
            updated.detail.urls.forEach { sourceEngine.removeSource(it.id) }
        } else {
            screenModelScope.launch { sourceEngine.loadSubscribeSource(updated) }
        }
        refresh()
    }

    fun delete(src: SubscribeSource) {
        src.detail.urls.forEach { sourceEngine.removeSource(it.id) }
        repo.remove(src.url)
        refresh()
    }

    fun importFromUrl() {
        val url = importUrl.trim()
        if (url.isBlank()) {
            mutableState.value = mutableState.value.copy(importError = "请输入订阅源 URL")
            return
        }
        mutableState.value = mutableState.value.copy(importLoading = true, importError = null)
        screenModelScope.launch {
            val result = runCatching {
                val raw = fetcher.fetchText(url)
                val detail = json.decodeFromString<SubscribeDetail>(raw)
                val source = SubscribeSource(url = url, detail = detail)
                repo.upsert(source)
                if (!source.disable) sourceEngine.loadSubscribeSource(source)
                source
            }
            result.onSuccess {
                importUrl = ""
                mutableState.value = SourceManageState(sources = repo.loadAll())
            }.onFailure { e ->
                Napier.w("importFromUrl failed: ${e.message}")
                mutableState.value = mutableState.value.copy(
                    importLoading = false,
                    importError = e.message ?: "导入失败",
                )
            }
        }
    }
}
