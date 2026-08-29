package com.wuji.app.core.platform

/** Desktop(JVM)平台实现 */
actual object Platform {
    actual val isAndroid: Boolean = false
    actual val isDesktop: Boolean = true
    actual val name: String = "Desktop"

    /** 桌面端:窗口宽度 ≤ 420 视为移动端视图(对齐原项目 matchMedia 420px 断点) */
    actual fun isMobileView(viewportWidth: Int): Boolean = viewportWidth <= 420
}
