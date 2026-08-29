package com.wuji.app.ui.screen.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState

/** 下载管理 - 对齐原项目 download/DownloadManager.vue */
object DownloadManagerScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "下载管理", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("暂无下载任务", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}
