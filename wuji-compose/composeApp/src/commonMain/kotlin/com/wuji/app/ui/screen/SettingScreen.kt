package com.wuji.app.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CleaningServices
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.core.platform.AppInfo
import com.wuji.app.core.platform.cacheDirByteSize
import com.wuji.app.core.platform.clearCacheDir
import com.wuji.app.core.storage.AppSettings
import com.wuji.app.core.storage.ReaderBackground
import com.wuji.app.core.storage.ThemeMode
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.screen.auth.LoginScreen
import com.wuji.app.ui.screen.auth.UserScreen
import com.wuji.app.ui.screen.sync.ManageSyncScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

/**
 * 设置页 - 完整项:
 *  主题切换、缓存大小/清理、代理(开关/主机/端口)、阅读器偏好(字号/背景)、版本号、账号/同步入口。
 */
object SettingScreen : Screen {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val model = koinScreenModel<SettingScreenModel>()
        val state by model.state.collectAsState()
        val nav = LocalNavigator.current
        val snackbar = remember { SnackbarHostState() }
        val scope = rememberCoroutineScope()
        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

        var showProxy by remember { mutableStateOf(false) }
        var showReader by remember { mutableStateOf(false) }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                AppTopBar(
                    title = "设置", onBack = { nav?.pop() },
                    scrollBehavior = scrollBehavior,
                )
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad).padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SectionTitle("外观主题")
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            .selectable(state.themeMode == mode) { model.setTheme(mode) },
                    ) {
                        RadioButton(selected = state.themeMode == mode, onClick = { model.setTheme(mode) })
                        Text(mode.label, style = MaterialTheme.typography.bodyLarge)
                    }
                }

                SectionTitle("缓存管理")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "当前缓存大小:${formatSize(state.cacheBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    FilledTonalButton(
                        onClick = {
                            model.clearCache()
                            scope.launch { snackbar.showSnackbar("缓存已清理") }
                        },
                        enabled = !state.clearing,
                    ) {
                        if (state.clearing) CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                        ) else Icon(Icons.Outlined.CleaningServices, null)
                        Spacer(Modifier.padding(end = 4.dp))
                        Text("清理缓存")
                    }
                }

                SectionTitle("网络代理")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "启用代理:${if (state.proxyEnabled) "${state.proxyHost}:${state.proxyPort}" else "关闭"}",
                        Modifier.weight(1f),
                    )
                    Switch(
                        checked = state.proxyEnabled,
                        onCheckedChange = model::setProxyEnabled,
                    )
                    FilledTonalButton({ showProxy = true }) { Text("配置") }
                }

                SectionTitle("阅读器偏好")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        "字号:${state.readerFontSp}sp  背景:${state.readerBg.label}",
                        Modifier.weight(1f),
                    )
                    FilledTonalButton({ showReader = true }) { Text("配置") }
                }
                // 预览
                Box(
                    Modifier.fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .padding(horizontal = 4.dp),
                ) {
                    ReaderBackgroundPreview(state.readerBg, state.readerFontSp)
                }

                SectionTitle("账号与同步")
                Row {
                    TextButton({ nav?.push(UserScreen) }) { Text("用户中心") }
                    TextButton({ nav?.push(LoginScreen) }) { Text("登录") }
                    TextButton({ nav?.push(ManageSyncScreen) }) { Text("同步管理") }
                }

                SectionTitle("关于")
                Text("应用名称: 无极 Wuji")
                Text("版本: ${AppInfo.versionName} (${AppInfo.versionCode})")
                Text("构建渠道: Compose Multiplatform KMP")
            }
        }

        if (showProxy) {
            ProxyDialog(
                host = state.proxyHost,
                port = state.proxyPort.toString(),
                enabled = state.proxyEnabled,
                onHostChange = { model.setProxyHost(it) },
                onPortChange = model::setProxyPortText,
                onEnabledChange = model::setProxyEnabled,
                onDismiss = { showProxy = false },
            )
        }
        if (showReader) {
            ReaderDialog(
                fontSp = state.readerFontSp,
                bg = state.readerBg,
                onFontSp = model::setReaderFontSp,
                onBg = model::setReaderBg,
                onDismiss = { showReader = false },
            )
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        Modifier.padding(top = 6.dp),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

@Composable
private fun ProxyDialog(
    host: String, port: String, enabled: Boolean,
    onHostChange: (String) -> Unit, onPortChange: (String) -> Unit,
    onEnabledChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("代理配置") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("启用代理", Modifier.weight(1f))
                    Switch(checked = enabled, onCheckedChange = onEnabledChange)
                }
                OutlinedTextField(host, onHostChange, label = { Text("主机,如 127.0.0.1") },
                    modifier = Modifier.fillMaxWidth())
                OutlinedTextField(port, onPortChange, label = { Text("端口,如 7890") },
                    modifier = Modifier.fillMaxWidth())
                Text("重新启动应用后生效", color = MaterialTheme.colorScheme.outline)
            }
        },
        confirmButton = { FilledTonalButton(onDismiss) { Text("完成") } },
    )
}

@Composable
private fun ReaderDialog(
    fontSp: Int, bg: ReaderBackground,
    onFontSp: (Int) -> Unit, onBg: (ReaderBackground) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("阅读器偏好") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("字号: ${fontSp}sp")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilledTonalButton(onClick = { onFontSp((fontSp - 1).coerceAtLeast(12)) }) { Text("A-") }
                    FilledTonalButton(onClick = { onFontSp((fontSp + 1).coerceAtMost(32)) }) { Text("A+") }
                    listOf(14, 16, 18, 20, 22, 24).forEach { sz ->
                        FilledTonalButton(onClick = { onFontSp(sz) }) { Text("$sz") }
                    }
                }
                Text("背景:")
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ReaderBackground.entries.forEach { b ->
                        Surface(
                            onClick = { onBg(b) },
                            color = b.color,
                            border = BorderStroke(
                                1.dp,
                                if (b == bg) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            ),
                            shape = CircleShape,
                            modifier = Modifier.padding(4.dp),
                        ) {
                            Box(Modifier.padding(10.dp)) { Text(b.label, color = b.contrastColor) }
                        }
                    }
                }
                ReaderBackgroundPreview(bg, fontSp)
            }
        },
        confirmButton = { FilledTonalButton(onDismiss) { Text("完成") } },
    )
}

@Composable
private fun ReaderBackgroundPreview(bg: ReaderBackground, fontSp: Int) {
    Surface(
        color = bg.color,
        contentColor = bg.contrastColor,
        shape = MaterialTheme.shapes.small,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            "  示例文字:无极 Wuji 阅读器预览。The quick brown fox jumps over the lazy dog.  ",
            modifier = Modifier.padding(16.dp),
            fontSize = fontSp.sp,
            lineHeight = (fontSp * 1.7).sp,
        )
    }
}

private fun formatSize(bytes: Long): String {
    val b = bytes.absoluteValue.toDouble()
    return when {
        b < 1024 -> "$b B"
        b < 1024 * 1024 -> "%.2f KB".format(b / 1024)
        b < 1024 * 1024 * 1024 -> "%.2f MB".format(b / 1024 / 1024)
        else -> "%.2f GB".format(b / 1024 / 1024 / 1024)
    }
}

private val ThemeMode.label: String get() = when (this) {
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
    ThemeMode.SYSTEM -> "跟随系统"
}

private val ReaderBackground.label: String get() = when (this) {
    ReaderBackground.WHITE -> "白底"
    ReaderBackground.YELLOW -> "护眼"
    ReaderBackground.GREEN -> "草绿"
    ReaderBackground.BLACK -> "夜间"
}

private val ReaderBackground.color: Color get() = when (this) {
    ReaderBackground.WHITE -> Color(0xFFFFFFFF.toInt())
    ReaderBackground.YELLOW -> Color(0xFFF5EFDB.toInt())
    ReaderBackground.GREEN -> Color(0xFFE3EDD3.toInt())
    ReaderBackground.BLACK -> Color(0xFF1A1A1A.toInt())
}

private val ReaderBackground.contrastColor: Color get() = when (this) {
    ReaderBackground.BLACK -> Color(0xFFE8E8E8.toInt())
    else -> Color(0xFF222222.toInt())
}

data class SettingState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val cacheBytes: Long = 0L,
    val clearing: Boolean = false,
    val proxyEnabled: Boolean = false,
    val proxyHost: String = "127.0.0.1",
    val proxyPort: Int = 7890,
    val readerFontSp: Int = 18,
    val readerBg: ReaderBackground = ReaderBackground.YELLOW,
)

/** 完整设置 ScreenModel - 主题/缓存/代理/阅读器偏好/版本号 */
class SettingScreenModel(private val settings: AppSettings) :
    StateScreenModel<SettingState>(SettingState()) {

    init {
        screenModelScope.launch {
            mutableState.value = SettingState(
                themeMode = ThemeMode.fromValue(settings.themeMode),
                cacheBytes = runCatching { cacheDirByteSize() }.getOrDefault(0L),
                proxyEnabled = settings.proxyEnabled,
                proxyHost = settings.proxyHost.ifBlank { "127.0.0.1" },
                proxyPort = settings.proxyPort.takeIf { it in 1..65535 } ?: 7890,
                readerFontSp = settings.readerFontSp.takeIf { it in 12..32 } ?: 18,
                readerBg = ReaderBackground.fromValue(settings.readerBgValue),
            )
        }
    }

    fun setTheme(mode: ThemeMode) {
        settings.themeMode = mode.value
        mutableState.value = state.value.copy(themeMode = mode)
    }

    fun clearCache() {
        mutableState.value = state.value.copy(clearing = true)
        screenModelScope.launch {
            runCatching { clearCacheDir() }
            val size = runCatching { cacheDirByteSize() }.getOrDefault(0L)
            mutableState.value = state.value.copy(clearing = false, cacheBytes = size)
        }
    }

    fun setProxyEnabled(enabled: Boolean) {
        settings.proxyEnabled = enabled
        mutableState.value = state.value.copy(proxyEnabled = enabled)
    }

    fun setProxyHost(host: String) {
        settings.proxyHost = host
        mutableState.value = state.value.copy(proxyHost = host)
    }

    fun setProxyPortText(portText: String) {
        val p = portText.toIntOrNull() ?: return
        if (p !in 1..65535) return
        settings.proxyPort = p
        mutableState.value = state.value.copy(proxyPort = p)
    }

    fun setReaderFontSp(sp: Int) {
        settings.readerFontSp = sp
        mutableState.value = state.value.copy(readerFontSp = sp)
    }

    fun setReaderBg(bg: ReaderBackground) {
        settings.readerBgValue = bg.value
        mutableState.value = state.value.copy(readerBg = bg)
    }
}
