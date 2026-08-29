package com.wuji.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.wuji.app.core.storage.ContextHolder

/**
 * Android 应用主入口 Activity。
 * 对齐桌面端 Main_desktop.kt:负责初始化 Context、Koin 并装载 Compose 根。
 *
 * 注意调用顺序:[ContextHolder.init] 必须先于 [initKoin],
 * 因 Koin 的 appModule 会触发 createSettings(),后者依赖 ApplicationContext。
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ContextHolder.init(this)
        initKoin()
        setContent {
            App()
        }
    }
}
