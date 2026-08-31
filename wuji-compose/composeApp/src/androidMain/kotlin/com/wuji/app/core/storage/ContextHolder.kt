package com.wuji.app.core.storage

import android.content.Context

/**
 * Android Context 持有者 - 提供全局 Application Context 给非 Composable 代码使用。
 *
 * 必须在 [com.wuji.app.MainActivity.onCreate] 之前(推荐在 Application#onCreate 或 Activity onCreate 起始处)
 * 调用 [init] 注入,确保 SettingsProvider 等平台工厂能获取到 Context。
 *
 * 注意:仅持有 Application Context,避免 Activity 泄漏。
 */
object ContextHolder {
    @Volatile
    lateinit var appContext: Context
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
