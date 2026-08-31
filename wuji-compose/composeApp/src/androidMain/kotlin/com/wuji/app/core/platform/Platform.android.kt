package com.wuji.app.core.platform

/**
 * Android 平台实现 - 对齐原项目 displayStore 的移动端判定。
 * isMobileView 直接返回 true(移动端默认走移动视图)。
 */
actual object Platform {
    actual val isAndroid: Boolean = true
    actual val isDesktop: Boolean = false
    actual val name: String = "Android"

    /**
     * Android 端始终视为移动端视图。
     * viewportWidth 参数保留以对齐 common 接口签名,实际未使用。
     */
    actual fun isMobileView(viewportWidth: Int): Boolean = true
}
