package com.wuji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Android 应用主入口 Activity。
 * 对齐桌面端 Main_desktop.kt:负责初始化 Koin 并装载 Compose 根。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initKoin()
        setContent {
            App()
        }
    }
}
