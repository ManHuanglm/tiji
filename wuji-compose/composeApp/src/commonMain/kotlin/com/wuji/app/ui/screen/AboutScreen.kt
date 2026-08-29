package com.wuji.app.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import com.wuji.app.ui.components.AppTopBar

/** 关于页 - 对齐原项目 about/index.vue */
object AboutScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        Scaffold(topBar = { AppTopBar(title = "关于", onBack = { navigator?.pop() }) }) { p ->
            Column(Modifier.fillMaxSize().padding(p).padding(20.dp)) {
                Text("无极 Wuji", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("版本 0.2.7 (Compose Multiplatform)", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(16.dp))
                Text(
                    "一款界面简洁、功能强大的跨平台资源聚合浏览器。聚合图片、音乐、书籍、漫画、视频资源的浏览、搜索与收藏。",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(Modifier.height(16.dp))
                Text("技术栈:Kotlin + Compose Multiplatform", style = MaterialTheme.typography.bodySmall)
                Text("许可证:GPL-3.0", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
