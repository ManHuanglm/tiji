package com.wuji.app.core.storage

import com.russhwolf.settings.Settings

/**
 * 平台 Settings 工厂 - 各平台 actual 提供 Settings 实例。
 *
 * 采用 `expect fun` 而非 `expect class`:经实测,在 `expect class` 成员签名中引用
 * 外部 KMP 类型(如 multiplatform-settings 的 `Settings`)会令 expect 声明解析失败,
 * 进而级联导致 desktop actual 无法匹配。`expect fun` 不复现该问题。
 *
 * 对齐原项目 tauri-plugin-store:桌面端落盘到用户目录,移动端使用系统 Preferences。
 */
expect fun createSettings(): Settings
