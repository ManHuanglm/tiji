package com.wuji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

/**
 * Android 应用主入口 Activity。
 * 只负责装载 Compose 根视图;Context 注入 + Koin 初始化已下沉至 [WujiApplication.onCreate]。
 *
 * 红米 K50 / MIUI 兼容性说明:
 * 将 Koin + ContextHolder 初始化从 Activity 迁移到 Application,可规避 MIUI
 * 对 Activity 生命周期的激进调度(多窗口/动画/分屏导致的重复 onCreate)造成的重复启动崩溃。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}
