package com.wuji.app.ui.screen.comic

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.wuji.app.ui.components.EmptyState

/** 漫画列表页 - 对齐原项目 comic/index.vue */
object ComicScreen : Screen {
    @Composable
    override fun Content() {
        EmptyState(message = "漫画 - 接入源后展示推荐漫画与书架")
    }
}
