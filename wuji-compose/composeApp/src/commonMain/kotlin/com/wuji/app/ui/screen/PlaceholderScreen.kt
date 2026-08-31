package com.wuji.app.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState

/**
 * 通用占位页骨架 - 供尚未完整实现具体业务的页面统一渲染(结构与导航对齐原项目)。
 * 各资源类列表/详情页待接入对应 ScreenModel 后替换。
 */
class PlaceholderScreen(private val title: String, private val hint: String = "功能开发中") : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(
            topBar = { AppTopBar(title = title, onBack = { navigator?.pop() }) },
        ) { padding ->
            EmptyState(message = hint, modifier = Modifier.fillMaxSize().padding(padding))
        }
    }
}
