package com.wuji.app.ui.screen.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import com.wuji.app.ui.navigation.PhotoTab
import com.wuji.app.ui.navigation.BookTab
import com.wuji.app.ui.navigation.ComicTab
import com.wuji.app.ui.navigation.SongTab
import com.wuji.app.ui.navigation.VideoTab
import com.wuji.app.ui.screen.AboutScreen
import com.wuji.app.ui.screen.SettingScreen
import com.wuji.app.ui.screen.source.SourceManageScreen
import com.wuji.app.ui.screen.download.DownloadManagerScreen

/**
 * 首页 - 对齐原项目 home/index.vue:聚合各资源入口与快捷功能。
 * 提供 5 类资源快速入口 + 源管理/下载/设置/关于入口。
 */
@Composable
fun HomeScreen() {
    val tabNavigator = LocalTabNavigator.current
    val navigator = LocalNavigator.current

    val entries = buildList {
        add(HomeEntry("图片", "浏览图片图集") { tabNavigator.current = PhotoTab })
        add(HomeEntry("音乐", "搜索与播放音乐") { tabNavigator.current = SongTab })
        add(HomeEntry("书籍", "书架与阅读") { tabNavigator.current = BookTab })
        add(HomeEntry("漫画", "漫画阅读") { tabNavigator.current = ComicTab })
        add(HomeEntry("视频", "视频播放") { tabNavigator.current = VideoTab })
        add(HomeEntry("订阅源管理", "导入/管理/创建源") { navigator?.push(SourceManageScreen) })
        add(HomeEntry("下载管理", "查看下载任务") { navigator?.push(DownloadManagerScreen) })
        add(HomeEntry("设置", "主题/同步/偏好") { navigator?.push(SettingScreen) })
        add(HomeEntry("关于", "版本与说明") { navigator?.push(AboutScreen) })
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("无极", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(
            "跨平台资源聚合浏览器",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            items(entries) { entry ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { entry.onClick() },
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            entry.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

private data class HomeEntry(val title: String, val desc: String, val onClick: () -> Unit)
