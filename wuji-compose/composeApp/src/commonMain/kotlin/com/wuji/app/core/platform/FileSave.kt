package com.wuji.app.core.platform

/**
 * 文件存储能力 - 对齐原项目 Tauri 插件的文件保存。
 * 通过各平台 actual 实现:
 *  - Desktop: 写入用户目录 Pictures/Wuji (Windows/macOS)、~/Pictures/Wuji (Linux)
 *  - Android: 写入 MediaStore.Images(需 WRITE_EXTERNAL_STORAGE 或 MediaStore API)
 *
 * 返回最终保存的文件路径,失败抛出异常(UI 捕获并提示)。
 */
expect suspend fun saveImageToLocal(
    url: String,
    fileName: String,
    headers: Map<String, String>? = null,
): String
