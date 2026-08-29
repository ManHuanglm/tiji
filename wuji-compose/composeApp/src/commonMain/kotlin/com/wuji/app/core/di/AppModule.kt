package com.wuji.app.core.di

import com.wuji.app.core.network.HttpClientFactory
import com.wuji.app.core.network.KtorSourceFetcher
import com.wuji.app.core.platform.Platform
import com.wuji.app.core.storage.AppSettings
import com.wuji.app.core.storage.createSettings
import com.wuji.app.data.SubscribeSourceRepository
import com.wuji.app.download.Downloader
import com.wuji.app.download.createPlatformDownloader
import com.wuji.app.source.ExtensionLoader
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.SourceFetcher
import com.wuji.app.source.SourceType
import com.wuji.app.source.defaults.DefaultBookSource
import com.wuji.app.source.defaults.DefaultComicSource
import com.wuji.app.source.defaults.DefaultPhotoSource
import com.wuji.app.source.defaults.DefaultSongSource
import com.wuji.app.source.defaults.DefaultVideoSource
import com.wuji.app.source.model.SubscribeItem
import io.ktor.client.HttpClient
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin 根模块 - 集中装配核心基础设施(对齐原项目 Pinia store + Tauri 插件的运行时依赖)。
 */
fun appModule(): Module = module {
    single { createSettings() }
    single { AppSettings(get()) }
    single { HttpClientFactory.json }
    single { HttpClientFactory.create() }

    single<SourceFetcher> { KtorSourceFetcher(get()) }

    // 订阅源持久化
    single { SubscribeSourceRepository(get(), get()) }

    // 源引擎 + 默认加载器(声明式/JS 源桥接)
    single { SourceEngine(loader = defaultExtensionLoader(), fetcher = get()) }

    // 下载管理器:按平台 actual 提供实现,单例(内部持有协程域)
    single<Downloader> { createPlatformDownloader() }

    // ====== ScreenModel 注册(对齐原项目各 Pinia store 的状态机) ======
    // Photo
    factory { com.wuji.app.ui.screen.photo.PhotoScreenModel(get()) }
    factory { com.wuji.app.ui.screen.photo.PhotoDetailScreenModel(get()) }
    // Song
    factory { com.wuji.app.ui.screen.song.SongScreenModel(get()) }
    factory { com.wuji.app.ui.screen.song.SongDetailScreenModel(get()) }
    // Book
    factory { com.wuji.app.ui.screen.book.BookScreenModel(get()) }
    factory { com.wuji.app.ui.screen.book.BookDetailScreenModel(get()) }
    // Comic
    factory { com.wuji.app.ui.screen.comic.ComicScreenModel(get()) }
    factory { com.wuji.app.ui.screen.comic.ComicDetailScreenModel(get()) }
    // Video
    factory { com.wuji.app.ui.screen.video.VideoScreenModel(get()) }
    factory { com.wuji.app.ui.screen.video.VideoDetailScreenModel(get()) }
    // Setting / Source / Download
    factory { com.wuji.app.ui.screen.SettingScreenModel(get()) }
    factory { com.wuji.app.ui.screen.source.SourceManageScreenModel(get(), get()) }
    factory { com.wuji.app.ui.screen.download.DownloadManagerScreenModel(get()) }
}

/**
 * 默认扩展加载器:保证「无外部订阅源也能演示」的内置示例源。
 * photo / song / book / comic / video 各返回一个对应默认源。
 * 其他类型暂返回 null,后续接入 JS 源桥接 (QuickJS-kt) 或声明式源时补齐。
 */
fun defaultExtensionLoader(): ExtensionLoader = object : ExtensionLoader {
    override suspend fun load(item: SubscribeItem) = when (item.type) {
        SourceType.Photo -> DefaultPhotoSource()
        SourceType.Song -> DefaultSongSource()
        SourceType.Book -> DefaultBookSource()
        SourceType.Comic -> DefaultComicSource()
        SourceType.Video -> DefaultVideoSource()
        else -> null
    }
}
