package com.wuji.app.ui.screen.source

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.data.SubscribeSourceRepository
import com.wuji.app.source.SourceType
import com.wuji.app.source.model.SubscribeDetail
import com.wuji.app.source.model.SubscribeSource
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState
import com.wuji.app.ui.screen.SourceMarketScreen
import com.wuji.app.ui.screen.SourceMyScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlin.math.max

/** 订阅源管理页 - 完整 CRUD、启用切换、排序、校验连通 */
object SourceManageScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SourceManageScreenModel>()
        val state by model.state.collectAsState()
        val nav = LocalNavigator.current
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()

        // 导入 Dialog
        var showImport by remember { mutableStateOf(false) }
        var importUrl by remember { mutableStateOf("") }

        // 编辑 Dialog
        var editing: SubscribeSource? by remember { mutableStateOf(null) }
        var editName by remember { mutableStateOf("") }
        var editUrl by remember { mutableStateOf("") }

        LaunchedEffect(model) {
            androidx.compose.runtime.snapshotFlow { model.actionResult }.collect { r ->
                when (r) {
                    is SourceActionResult.Checked -> {
                        val msg = if (r.ok) "${r.url} 连通成功" else "${r.url} 连通失败:${r.msg}"
                        scope.launch { snackbar.showSnackbar(msg) }
                    }
                    is SourceActionResult.Error -> scope.launch { snackbar.showSnackbar(r.msg) }
                    SourceActionResult.Idle -> {}
                }
            }
        }

        Scaffold(
            topBar = {
                AppTopBar(
                    title = "订阅源管理",
                    onBack = { nav?.pop() },
                    actions = {
                        IconButton({ showImport = true }) {
                            Icon(Icons.Outlined.Add, "导入源")
                        }
                        IconButton({ nav?.push(SourceMyScreen) }) {
                            Icon(Icons.Outlined.Add, "我的源")
                        }
                        IconButton({ nav?.push(SourceMarketScreen) }) {
                            Icon(Icons.Outlined.Store, "源市场")
                        }
                    },
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { pad ->
            if (state.sources.isEmpty()) {
                EmptyState(
                    "暂无订阅源,请点击顶部 + 导入链接或前往市场",
                    modifier = Modifier.fillMaxSize().padding(pad),
                )
            } else {
                LazyColumn(Modifier.fillMaxSize().padding(pad)) {
                    items(state.sources, key = { it.url }) { src ->
                        SourceRow(
                            src = src,
                            onToggle = { model.toggleEnabled(src.url) },
                            onEdit = {
                                editing = src
                                editName = src.detail.name
                                editUrl = src.url
                            },
                            onCheck = { model.checkConnectivity(src.url) },
                            onMoveUp = { model.move(src.url, -1) },
                            onMoveDown = { model.move(src.url, +1) },
                            onDelete = { model.remove(src.url) },
                        )
                    }
                }
            }
        }

        if (showImport) {
            ImportDialog(
                url = importUrl,
                onChange = { importUrl = it },
                onDismiss = { showImport = false; importUrl = "" },
                onConfirm = {
                    model.importFromUrl(importUrl)
                    showImport = false; importUrl = ""
                },
                checking = state.checkingUrl,
            )
        }

        val ed = editing
        if (ed != null) {
            EditDialog(
                name = editName, url = editUrl,
                onNameChange = { editName = it }, onUrlChange = { editUrl = it },
                onDismiss = { editing = null },
                onConfirm = {
                    model.updateUrl(ed.copy(
                        url = editUrl,
                        detail = ed.detail.copy(name = editName, id = editUrl),
                    ))
                    editing = null
                },
            )
        }
    }
}

@Composable
private fun SourceRow(
    src: SubscribeSource,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onCheck: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    ListItem(
        headlineContent = { Text(src.detail.name) },
        supportingContent = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(src.url, style = MaterialTheme.typography.labelSmall)
                val types = src.detail.urls.groupBy { it.type }.keys.joinToString("/") { it.name }
                Text("类型:${types}  子源数:${src.detail.urls.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = !src.disable, onCheckedChange = { onToggle() })
                IconButton(onClick = onMoveUp) { Icon(Icons.Outlined.KeyboardArrowUp, "上移") }
                IconButton(onClick = onMoveDown) { Icon(Icons.Outlined.KeyboardArrowDown, "下移") }
                IconButton(onClick = onCheck) { Icon(Icons.Outlined.Check, "校验") }
                IconButton(onClick = onEdit) { Icon(Icons.Outlined.Edit, "编辑") }
                IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, "删除") }
            }
        },
    )
}

@Composable
private fun ImportDialog(
    url: String, onChange: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit,
    checking: Boolean,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入订阅源") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("输入订阅源链接(支持 http/https URL 或直接 JSON 粘贴)")
                OutlinedTextField(
                    value = url, onValueChange = onChange,
                    placeholder = { Text("https://... 或 { ... }") },
                    modifier = Modifier.fillMaxWidth(),
                )
                if (checking) Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.padding(end = 8.dp))
                    Text("解析中…", style = MaterialTheme.typography.labelSmall)
                }
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm, enabled = url.isNotBlank() && !checking) {
                Text("确认导入")
            }
        },
        dismissButton = { TextButton(onDismiss) { Text("取消") } },
    )
}

@Composable
private fun EditDialog(
    name: String, url: String,
    onNameChange: (String) -> Unit, onUrlChange: (String) -> Unit,
    onDismiss: () -> Unit, onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑订阅源") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(name, onNameChange, label = { Text("名称") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(url, onUrlChange, label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirm, enabled = name.isNotBlank()) { Text("保存") }
        },
        dismissButton = { TextButton(onDismiss) { Text("取消") } },
    )
}

/** 源管理状态 (包含校验中 URL 等) */
@Serializable
data class SourceManageState(
    val sources: List<SubscribeSource> = emptyList(),
    val checkingUrl: Boolean = false,
)

/** 异步操作结果 */
sealed interface SourceActionResult {
    data object Idle : SourceActionResult
    data class Checked(val url: String, val ok: Boolean, val msg: String = "") : SourceActionResult
    data class Error(val msg: String) : SourceActionResult
}

/**
 * 订阅源管理 ScreenModel - 完整 CRUD / 开关 / 排序 / 校验。
 * 校验:对给定 URL 发起一次 HEAD/GET,返回成功或失败(对齐原项目 checkSourceStatus 思路)。
 */
class SourceManageScreenModel(
    private val repo: SubscribeSourceRepository,
    private val fetcher: com.wuji.app.source.SourceFetcher,
) : StateScreenModel<SourceManageState>(SourceManageState()) {

    private val _action = MutableStateFlow<SourceActionResult>(SourceActionResult.Idle)
    val actionResult = _action.asStateFlow()

    init { mutableState.value = state.value.copy(sources = repo.loadAll()) }

    /** 启用状态切换 */
    fun toggleEnabled(url: String) {
        val cur = mutableState.value.sources
        val next = cur.map { if (it.url == url) it.copy(disable = !it.disable) else it }
        save(next)
    }

    /** 顺序调整:step=-1 上移,step=+1 下移 */
    fun move(url: String, step: Int) {
        val list = mutableState.value.sources.toMutableList()
        val idx = list.indexOfFirst { it.url == url }
        if (idx < 0) return
        val target = max(0, minOf(list.size - 1, idx + step))
        if (target == idx) return
        val item = list.removeAt(idx)
        list.add(target, item)
        save(list)
    }

    /** 删除订阅源 */
    fun remove(url: String) {
        repo.remove(url)
        mutableState.value = state.value.copy(sources = repo.loadAll())
    }

    /** 编辑保存(按原 url 匹配) */
    fun updateUrl(new: SubscribeSource) {
        repo.upsert(new)
        mutableState.value = state.value.copy(sources = repo.loadAll())
    }

    /**
     * 导入订阅源:
     *  - 若是 JSON,尝试解析为 SubscribeSource
     *  - 若是远程 URL,先尝试用 fetcher 拉取 JSON,再解析(失败则生成占位源)
     */
    fun importFromUrl(raw: String) {
        screenModelScope.launch {
            mutableState.value = state.value.copy(checkingUrl = true)
            runCatching {
                val src = if (raw.trim().startsWith("{")) {
                    // 本地粘贴 JSON
                    val json = kotlinx.serialization.json.Json {
                        ignoreUnknownKeys = true; isLenient = true
                    }
                    json.decodeFromString<SubscribeSource>(raw.trim())
                } else {
                    val text = fetcher.fetchString(raw).orEmpty().ifBlank { return@runCatching }
                    try {
                        val json = kotlinx.serialization.json.Json {
                            ignoreUnknownKeys = true; isLenient = true
                        }
                        json.decodeFromString<SubscribeSource>(text)
                    } catch (_: Exception) {
                        // 远端不是 JSON,生成占位订阅源,后续可手动 edit
                        SubscribeSource(
                            url = raw,
                            disable = false,
                            detail = SubscribeDetail(
                                id = raw,
                                name = "源@${raw.take(32)}",
                                urls = emptyList(),
                            ),
                        )
                    }
                }
                repo.upsert(src)
                mutableState.value = state.value.copy(sources = repo.loadAll(), checkingUrl = false)
            }.onFailure { e ->
                mutableState.value = state.value.copy(checkingUrl = false)
                _action.value = SourceActionResult.Error("导入失败:${e.message}")
            }
        }
    }

    /** 连通性校验:发一次 GET,成功 OK,失败返回消息 */
    fun checkConnectivity(url: String) {
        screenModelScope.launch {
            mutableState.value = state.value.copy(checkingUrl = true)
            _action.value = SourceActionResult.Checked(
                url = url,
                ok = runCatching { fetcher.fetchString(url) != null }.getOrDefault(false),
                msg = runCatching { fetcher.fetchString(url) }.exceptionOrNull()?.message.orEmpty(),
            )
            mutableState.value = state.value.copy(checkingUrl = false)
        }
    }

    private fun save(list: List<SubscribeSource>) {
        repo.saveAll(list)
        mutableState.value = state.value.copy(sources = list)
    }
}

/** 补齐 SubscribeSource 缺省类型:取子源第一个(SourceType 枚举已在 SourceEngine 包中) */
private inline val SubscribeItem.typeName: SourceType get() = type
