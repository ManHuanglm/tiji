package com.wuji.app.ui.screen.video

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.wuji.app.ui.components.EmptyState

/** 视频列表页 - 对齐原项目 video/index.vue */
object VideoScreen : Screen {
    @Composable
    override fun Content() {
        EmptyState(message = "视频 - 接入源后展示推荐视频与收藏")
    }
}
