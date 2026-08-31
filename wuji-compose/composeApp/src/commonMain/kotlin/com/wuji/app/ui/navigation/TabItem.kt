package com.wuji.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CommentBank
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.outlined.VideoCameraBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.CommentBank
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PhotoLibrary
import androidx.compose.material.icons.rounded.VideoCameraBack
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.ui.graphics.vector.ImageVector
import com.wuji.app.source.SourceType

/**
 * Tab 项定义 - 对齐原项目 displayStore.tabBarPages(Home/Photo/Song/Book/Comic/Video)。
 * 桌面端侧栏展示全部 6 项,移动端底部仅展示 5 项资源(Home 作为落地页)。
 */
enum class TabItem(
    val label: String,
    val outline: ImageVector,
    val filled: ImageVector,
    val sourceType: SourceType?,
    val route: String,
) {
    Home("首页", Icons.Outlined.Home, Icons.Rounded.Home, null, "home"),
    Photo("图片", Icons.Outlined.PhotoLibrary, Icons.Rounded.PhotoLibrary, SourceType.Photo, "photo"),
    Song("音乐", Icons.Outlined.MusicNote, Icons.Rounded.MusicNote, SourceType.Song, "song"),
    Book("书籍", Icons.Outlined.BookmarkBorder, Icons.Rounded.Bookmark, SourceType.Book, "book"),
    Comic("漫画", Icons.Outlined.CommentBank, Icons.Rounded.CommentBank, SourceType.Comic, "comic"),
    Video("视频", Icons.Outlined.VideoCameraBack, Icons.Rounded.VideoCameraBack, SourceType.Video, "video");

    companion object {
        /** 移动端底部 tab(不含 Home) */
        val mobileTabs = listOf(Photo, Song, Book, Comic, Video)
        /** 桌面端侧栏(含 Home) */
        val desktopTabs = listOf(Home, Photo, Song, Book, Comic, Video)
    }
}
