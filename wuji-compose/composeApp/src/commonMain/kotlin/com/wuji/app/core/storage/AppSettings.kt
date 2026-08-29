package com.wuji.app.core.storage

import com.russhwolf.settings.Settings
import com.russhwolf.settings.boolean
import com.russhwolf.settings.int
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

    /** 网络代理配置 */
    var proxyEnabled: Boolean by settings.boolean(PROXY_ENABLED_KEY, false)
    var proxyHost: String by settings.string(PROXY_HOST_KEY, "127.0.0.1")
    var proxyPort: Int by settings.int(PROXY_PORT_KEY, 7890)

    /** 阅读器偏好配置 */
    var readerFontSp: Int by settings.int(READER_FONT_SP_KEY, 18)
    var readerBgValue: String by settings.string(READER_BG_KEY, ReaderBackground.YELLOW.value)

    companion object {
        const val THEME_MODE_KEY = "theme_mode"
        const val FIRST_LAUNCH_KEY = "first_launch"
        const val CLOUD_SERVER_URL_KEY = "cloud_server_url"
        const val CLOUD_TOKEN_KEY = "cloud_token"
        const val LAST_SYNC_TIME_KEY = "last_sync_time"
        const val PROXY_ENABLED_KEY = "proxy_enabled"
        const val PROXY_HOST_KEY = "proxy_host"
        const val PROXY_PORT_KEY = "proxy_port"
        const val READER_FONT_SP_KEY = "reader_font_sp"
        const val READER_BG_KEY = "reader_bg"
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

/** 阅读器背景选项 - 白底/护眼/草绿/夜间 */
enum class ReaderBackground(val value: String) {
    WHITE("white"),
    YELLOW("yellow"),
    GREEN("green"),
    BLACK("black");

    companion object {
        fun fromValue(v: String?): ReaderBackground = entries.firstOrNull { it.value == v } ?: YELLOW
    }
}

