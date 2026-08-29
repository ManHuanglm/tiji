package com.wuji.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.wuji.app.core.storage.ThemeMode

/**
 * 无极主题 - 对齐原项目 settingStore 的深浅色/跟随系统主题切换。
 */
@Composable
fun WujiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (isDark) DarkColors else LightColors,
        typography = WujiTypography,
        content = content,
    )
}
