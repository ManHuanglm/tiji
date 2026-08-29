package com.wuji.app.ui.screen.book

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.wuji.app.ui.components.EmptyState

/** 书籍列表页 - 对齐原项目 book/index.vue */
object BookScreen : Screen {
    @Composable
    override fun Content() {
        EmptyState(message = "书籍 - 接入源后展示推荐书籍与书架")
    }
}
