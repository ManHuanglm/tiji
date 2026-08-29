package com.wuji.app.ui.screen.song

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import com.wuji.app.ui.components.EmptyState

/** 音乐列表页 - 对齐原项目 song/index.vue。聚合多源歌曲推荐/搜索,待接入 SongScreenModel */
object SongScreen : Screen {
    @Composable
    override fun Content() {
        // 复用图片模板:接入 SongScreenModel 后替换为音乐卡片网格 + 播放器入口
        EmptyState(message = "音乐 - 接入源后展示推荐歌单/歌曲")
    }
}
