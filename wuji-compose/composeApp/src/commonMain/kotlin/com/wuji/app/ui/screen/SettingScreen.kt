package com.wuji.app.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.StateScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.koinScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.core.storage.AppSettings
import com.wuji.app.core.storage.ThemeMode
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.screen.auth.LoginScreen
import com.wuji.app.ui.screen.auth.UserScreen
import com.wuji.app.ui.screen.sync.ManageSyncScreen
import kotlinx.coroutines.launch

/** 设置页 - 对齐原项目 setting/index.vue:主题/账号/同步/关于 */
object SettingScreen : Screen {
    @Composable
    override fun Content() {
        val model = koinScreenModel<SettingScreenModel>()
        val state by model.state.collectAsState()
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "设置", onBack = { navigator?.pop() }) }) { p ->
            Column(Modifier.fillMaxSize().padding(p).padding(16.dp)) {
                Text("主题", style = MaterialTheme.typography.titleMedium)
                ThemeMode.entries.forEach { mode ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    ) {
                        RadioButton(selected = state.themeMode == mode, onClick = { model.setTheme(mode) })
                        Text(mode.label)
                    }
                }
                Text("账号", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                Row {
                    TextButton(onClick = { navigator?.push(UserScreen) }) { Text("用户中心") }
                    TextButton(onClick = { navigator?.push(LoginScreen) }) { Text("登录") }
                }
                Text("云同步", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 16.dp))
                TextButton(onClick = { navigator?.push(ManageSyncScreen) }) { Text("同步管理") }
            }
        }
    }
}

private val ThemeMode.label: String get() = when (this) {
    ThemeMode.LIGHT -> "浅色"
    ThemeMode.DARK -> "深色"
    ThemeMode.SYSTEM -> "跟随系统"
}

data class SettingState(val themeMode: ThemeMode = ThemeMode.SYSTEM)

class SettingScreenModel(private val settings: AppSettings) : StateScreenModel<SettingState>(SettingState()) {
    init {
        mutableState.value = SettingState(ThemeMode.fromValue(settings.themeMode))
    }

    fun setTheme(mode: ThemeMode) {
        settings.themeMode = mode.value
        mutableState.value = SettingState(mode)
    }
}
