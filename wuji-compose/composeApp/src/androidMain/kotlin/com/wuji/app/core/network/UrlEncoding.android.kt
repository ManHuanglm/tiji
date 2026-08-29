package com.wuji.app.core.network

import java.net.URLEncoder

/**
 * Android 端 URL 编码实现(JVM 平台通用)。
 * 与桌面端实现一致,使用 java.net.URLEncoder。
 */
actual fun String.urlEncode(): String =
    URLEncoder.encode(this, "UTF-8").replace("+", "%20")
