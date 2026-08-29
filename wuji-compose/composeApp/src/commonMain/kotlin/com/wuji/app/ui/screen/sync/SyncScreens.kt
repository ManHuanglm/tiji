package com.wuji.app.ui.screen.sync

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState

/** 同步管理 - 对齐原项目 sync/ManageSync.vue */
object ManageSyncScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "同步管理", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("云同步待接入云端服务(收藏夹/偏好同步)", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}

object SyncToServerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "上传到云端", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("待接入", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}

object SyncFromServerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "从云端恢复", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("待接入", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}
