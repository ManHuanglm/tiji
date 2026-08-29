package com.wuji.app.core.platform

import java.awt.Desktop
import java.net.URI

/** Desktop 端调用系统默认浏览器/播放器打开 URL */
actual fun openUrlExternal(url: String) {
    runCatching {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI(url))
        }
    }
}
