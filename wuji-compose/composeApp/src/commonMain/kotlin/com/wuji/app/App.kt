package com.wuji.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.wuji.app.core.di.appModule
import com.wuji.app.core.storage.AppSettings
import com.wuji.app.core.storage.ThemeMode
import com.wuji.app.ui.navigation.MainScreen
import com.wuji.app.ui.theme.WujiTheme
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.compose.koinInject
import org.koin.core.context.startKoin

/**
 * 应用根 Composable。
 * 装配 Koin、主题、Voyager 导航与页面切换动画,承载无极全部页面与功能。
 */
@Composable
fun App() {
    val settings = koinInject<AppSettings>()
    val themeFlow = remember { kotlinx.coroutines.flow.MutableStateFlow(ThemeMode.fromValue(settings.themeMode)) }
    val themeMode by themeFlow.collectAsState()

    WujiTheme(themeMode) {
        Navigator(MainScreen) { navigator ->
            SlideTransition(navigator)
        }
    }
}

/** 初始化 Koin 容器(桌面/通用入口共用) */
fun initKoin() {
    Napier.base(DebugAntilog())
    startKoin {
        modules(appModule())
    }
}
