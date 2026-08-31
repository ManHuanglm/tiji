package com.wuji.app.core.storage

import com.russhwolf.settings.PropertiesSettings
import com.russhwolf.settings.Settings
import java.io.File
import java.util.Properties

/**
 * Desktop 平台 Settings 实现:基于 Java Properties 落盘到用户目录 ~/.wuji/settings.properties。
 * 对齐原项目 tauri-plugin-store 在桌面端的本地存储。
 */
actual fun createSettings(): Settings {
    val dir = File(System.getProperty("user.home"), ".wuji")
    if (!dir.exists()) dir.mkdirs()
    val file = File(dir, "settings.properties")
    return PropertiesSettings(
        delegate = Properties().apply {
            if (file.exists()) file.inputStream().use { load(it) }
        },
        onModify = { props ->
            file.outputStream().use { props.store(it, "Wuji Desktop Settings") }
        },
    )
}
