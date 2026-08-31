package com.wuji.app.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions
import com.wuji.app.ui.components.AppTopBar
import com.wuji.app.ui.screen.SettingScreen
import com.wuji.app.ui.screen.book.BookScreen
import com.wuji.app.ui.screen.comic.ComicScreen
import com.wuji.app.ui.screen.home.HomeScreen
import com.wuji.app.ui.screen.photo.PhotoScreen
import com.wuji.app.ui.screen.song.SongScreen
import com.wuji.app.ui.screen.video.VideoScreen

/** 主 Tabbar 外壳页 - 对齐原项目 views/tabbar/index.vue,承载 Home + 5 类资源 tab */
object MainScreen : Screen {
    @Composable
    override fun Content() {
        val navigator = LocalNavigator.current
        TabNavigator(HomeTab) {
            Scaffold(
                topBar = {
                    AppTopBar(
                        title = currentTabLabel(),
                        actions = {
                            IconButton(onClick = { navigator?.push(SettingScreen) }) {
                                Icon(Icons.Outlined.Settings, contentDescription = "设置")
                            }
                        },
                    )
                },
                bottomBar = {
                    val tabNavigator = LocalTabNavigator.current
                    NavigationBar {
                        TabItem.entries.forEach { item ->
                            val tab = tabFor(item)
                            NavigationBarItem(
                                selected = tabNavigator.current == tab,
                                onClick = { tabNavigator.current = tab },
                                icon = { Icon(item.outline, contentDescription = item.label) },
                                label = { Text(item.label) },
                            )
                        }
                    }
                },
            ) { padding ->
                androidx.compose.foundation.layout.Box(
                    Modifier.fillMaxSize().padding(padding),
                ) {
                    CurrentTab()
                }
            }
        }
    }
}

@Composable
private fun currentTabLabel(): String =
    LocalTabNavigator.current.current.options.title

private fun tabFor(item: TabItem): Tab = when (item) {
    TabItem.Home -> HomeTab
    TabItem.Photo -> PhotoTab
    TabItem.Song -> SongTab
    TabItem.Book -> BookTab
    TabItem.Comic -> ComicTab
    TabItem.Video -> VideoTab
}

/**
 * 各资源 Tab 定义 - 内联 object : Tab 实现。
 *
 * 注意:不使用带 `@Composable () -> Unit` 参数的工厂函数,
 * 因为在顶层 val 初始化(非 Composable 上下文)中调用此类函数会触发
 * Compose 编译器的解析限制,导致函数符号无法被解析。
 * 故每个 Tab 直接内联实现 options 与 Content。
 */
internal val HomeTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Home.ordinal.toUShort(), TabItem.Home.label, null)
    @Composable
    override fun Content() = HomeScreen()
}
internal val PhotoTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Photo.ordinal.toUShort(), TabItem.Photo.label, null)
    @Composable
    override fun Content() = PhotoScreen.Content()
}
internal val SongTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Song.ordinal.toUShort(), TabItem.Song.label, null)
    @Composable
    override fun Content() = SongScreen.Content()
}
internal val BookTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Book.ordinal.toUShort(), TabItem.Book.label, null)
    @Composable
    override fun Content() = BookScreen.Content()
}
internal val ComicTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Comic.ordinal.toUShort(), TabItem.Comic.label, null)
    @Composable
    override fun Content() = ComicScreen.Content()
}
internal val VideoTab: Tab = object : Tab {
    override val options: TabOptions
        @Composable
        get() = TabOptions(TabItem.Video.ordinal.toUShort(), TabItem.Video.label, null)
    @Composable
    override fun Content() = VideoScreen.Content()
}
