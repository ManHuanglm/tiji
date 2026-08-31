package com.wuji.app

import android.app.Application
import com.wuji.app.core.storage.ContextHolder

/**
 * 无极全局 Application。
 *
 * 职责:
 * 1. 提前注入 [ContextHolder],确保 Settings/Koin 等需要 ApplicationContext 的组件能安全获取。
 * 2. 启动时唯一一次调用 [initKoin],避免 Activity 重建(旋转/分屏)导致的 Koin 重复启动崩溃。
 *
 * 红米 K50 / MIUI 特殊兼容性说明:
 * - MIUI 对 Activity 生命周期较为激进, Activity.onCreate 可能在 Application 完全初始化前被调度,
 *   所以把 ContextHolder + Koin 初始化下沉到 Application.onCreate 是唯一稳妥方案。
 */
class WujiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        ContextHolder.init(this)
        initKoin()
    }
}
