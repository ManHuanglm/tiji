package com.wuji.app.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.long
import com.russhwolf.settings.string

/**
 * 应用持久化键值存储 - 对齐原项目基于 tauri-plugin-store / localStorage 的本地配置持久化。
 * 通过 multiplatform-settings,各平台底层为 Preferences(Android)/ Properties(Desktop)。
 */
class AppSettings(private val settings: Settings) {

    var themeMode: String by settings.string(THEME_MODE_KEY, ThemeMode.SYSTEM.value)

    /** 是否首次启动(用于引导导入默认源) */
    var firstLaunch: Boolean by settings.boolean(FIRST_LAUNCH_KEY, true)

    var cloudServerUrl: String by settings.string(CLOUD_SERVER_URL_KEY, "")

    var cloudToken: String by settings.string(CLOUD_TOKEN_KEY, "")

    var lastSyncTime: Long by settings.long(LAST_SYNC_TIME_KEY, 0L)

    companion object {
        const val THEME_MODE_KEY = "theme_mode"
        const val FIRST_LAUNCH_KEY = "first_launch"
        const val CLOUD_SERVER_URL_KEY = "cloud_server_url"
        const val CLOUD_TOKEN_KEY = "cloud_token"
        const val LAST_SYNC_TIME_KEY = "last_sync_time"
    }
}

/** 主题模式 - 对齐原项目 settingStore 的深浅色主题 */
enum class ThemeMode(val value: String) {
    LIGHT("light"),
    DARK("dark"),
    SYSTEM("system");

    companion object {
        fun fromValue(v: String?): ThemeMode = entries.firstOrNull { it.value == v } ?: SYSTEM
    }
}
