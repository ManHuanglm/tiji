package com.wuji.app.ui.screen

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.components.EmptyState

/** 订阅源市场 - 对齐原项目 source/SourceMarket.vue:发现并一键导入推荐源 */
object SourceMarketScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "订阅源市场", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("源市场待接入云端服务", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}

/** 我的源 - 对齐原项目 source/MySource.vue:展示/编辑我创建的源 */
object SourceMyScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "我的源", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("暂未创建源,可在此新建并编辑源规则", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}

/** 创建源 - 对齐原项目 source/CreateSourceContent.vue:源规则可视化编辑器 */
object CreateSourceScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "创建源", onBack = { navigator?.pop() }) }) { p ->
            EmptyState("源规则编辑器(选择器/字段映射/解析预览)", modifier = Modifier.fillMaxSize().padding(p))
        }
    }
}
