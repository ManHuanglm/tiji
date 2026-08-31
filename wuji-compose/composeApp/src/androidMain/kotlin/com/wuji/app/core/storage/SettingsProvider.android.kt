package com.wuji.app.core.storage

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Android 端 Settings 工厂。
 *
 * 使用应用私有 SharedPreferences 持久化,对齐原项目 tauri-plugin-store 在移动端的本地存储。
 * Context 由 [ContextHolder] 提供,Application 启动时通过 [ContextHolder.init] 注入。
 */
actual fun createSettings(): Settings {
    val prefs = ContextHolder.appContext
        .getSharedPreferences("wuji_settings", Context.MODE_PRIVATE)
    return SharedPreferencesSettings(prefs)
}
