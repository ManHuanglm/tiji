package com.wuji.app.core.platform

import com.wuji.app.core.platform.Platform

/**
 * 平台信息抽象 - 对齐原项目 displayStore 中 isAndroid / 移动端判定。
 * 由各平台 actual 实现提供真实值。
 */
expect object Platform {
    /** 是否 Android */
    val isAndroid: Boolean

    /** 是否桌面(Desktop/JVM) */
    val isDesktop: Boolean

    /** 当前平台名称 */
    val name: String

    /** 是否移动端视图(Android 或窄屏桌面) */
    fun isMobileView(viewportWidth: Int): Boolean
}
