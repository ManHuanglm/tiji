package com.wuji.app.core.platform

import android.content.Intent
import android.net.Uri
import com.wuji.app.core.storage.ContextHolder

/** Android 端通过 Intent.ACTION_VIEW 打开外部 URL (系统浏览器/默认播放器) */
actual fun openUrlExternal(url: String) {
    runCatching {
        val ctx = ContextHolder.appContext
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        ctx.startActivity(intent)
    }
}
