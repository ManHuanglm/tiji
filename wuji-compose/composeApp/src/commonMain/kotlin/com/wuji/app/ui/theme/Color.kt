package com.wuji.app.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/** 无极主题色板 - 对齐原项目 Vant + orange-600 主色风格 */

// 主色:橙(原项目 text-button border-b-orange-600)
val Orange500 = Color(0xFFF97316)
val Orange600 = Color(0xFFEA580C)
val Orange700 = Color(0xFFC2410C)

// 浅色
val LightBg = Color(0xFFF7F8FA)        // vant background
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF2F3F5)
val LightTextPrimary = Color(0xFF323233)
val LightTextSecondary = Color(0xFF969799)
val LightBorder = Color(0xFFEBEDF0)

// 深色
val DarkBg = Color(0xFF121212)
val DarkSurface = Color(0xFF1E1E1E)
val DarkSurfaceVariant = Color(0xFF2A2A2A)
val DarkTextPrimary = Color(0xFFE8E8E8)
val DarkTextSecondary = Color(0xFF9A9A9A)
val DarkBorder = Color(0xFF333333)

val LightColors = lightColorScheme(
    primary = Orange600,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE4D6),
    onPrimaryContainer = Orange700,
    secondary = Orange500,
    onSecondary = Color.White,
    background = LightBg,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    outlineVariant = LightBorder,
)

val DarkColors = darkColorScheme(
    primary = Orange500,
    onPrimary = Color.Black,
    primaryContainer = Orange700,
    onPrimaryContainer = Color.White,
    secondary = Orange500,
    onSecondary = Color.Black,
    background = DarkBg,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
)
