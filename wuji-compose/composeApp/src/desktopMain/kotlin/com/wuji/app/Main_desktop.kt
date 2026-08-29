package com.wuji.app

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.wuji.app.core.platform.Platform

/**
 * 桌面(Desktop/JVM)应用入口。
 * 对齐原项目 Tauri 在 Windows 端的窗口化运行。
 */
fun main() = application {
    initKoin()
    Window(
        onCloseRequest = ::exitApplication,
        title = "无极 Wuji - ${Platform.name}",
        state = rememberWindowState(
            width = 1280.dp,
            height = 820.dp,
            position = WindowPosition(Alignment.Center),
        ),
    ) {
        App()
    }
}
