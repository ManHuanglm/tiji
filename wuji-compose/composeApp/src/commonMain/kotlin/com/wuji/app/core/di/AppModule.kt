package com.wuji.app.core.di

import com.wuji.app.core.network.HttpClientFactory
import com.wuji.app.core.network.KtorSourceFetcher
import com.wuji.app.core.storage.AppSettings
import com.wuji.app.core.storage.createSettings
import com.wuji.app.data.SubscribeSourceRepository
import com.wuji.app.source.ExtensionLoader
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.SourceFetcher
import com.wuji.app.source.SourceType
import com.wuji.app.source.defaults.DefaultPhotoSource
import com.wuji.app.source.model.SubscribeItem
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Koin 根模块 - 集中装配核心基础设施。
 * 新增 ScreenModel 在此注册,确保 Voyager koinScreenModel() 可解析。
 */
fun appModule(): Module = module {
    single { createSettings() }
    single { AppSettings(get()) }
    single<Json> { HttpClientFactory.json }
    single<HttpClient> { HttpClientFactory.create() }

    single<SourceFetcher> { KtorSourceFetcher(get()) }

    // 订阅源持久化
    single { SubscribeSourceRepository(get(), get()) }

    // 源引擎 + 默认加载器
    single {
        SourceEngine(loader = defaultExtensionLoader(), fetcher = get())
    }

    // ==================== ScreenModel 注册 ====================
    // Photo
    factory { com.wuji.app.ui.screen.photo.PhotoScreenModel(get()) }
    factory { com.wuji.app.ui.screen.photo.PhotoDetailScreenModel(get()) }
    // Song
    factory { com.wuji.app.ui.screen.song.SongScreenModel(get()) }
    // Book
    factory { com.wuji.app.ui.screen.book.BookScreenModel(get()) }
    factory { com.wuji.app.ui.screen.book.BookDetailScreenModel(get()) }
    factory { com.wuji.app.ui.screen.book.BookReaderScreenModel(get()) }
    // Comic
    factory { com.wuji.app.ui.screen.comic.ComicScreenModel(get()) }
    factory { com.wuji.app.ui.screen.comic.ComicDetailScreenModel(get()) }
    factory { com.wuji.app.ui.screen.comic.ComicReaderScreenModel(get()) }
    // Video
    factory { com.wuji.app.ui.screen.video.VideoScreenModel(get()) }
    factory { com.wuji.app.ui.screen.video.VideoDetailScreenModel(get()) }
    // Settings / Source
    factory { com.wuji.app.ui.screen.SettingScreenModel(get()) }
    factory { com.wuji.app.ui.screen.source.SourceManageScreenModel(get(), get(), get(), get()) }
}

/**
 * 默认扩展加载器:
 * - photo 类型返回内置示例源(保证离线可演示)
 * - 其余类型暂返回 null,等待接入 JS 源桥接(QuickJS-kt)或声明式源
 */
fun defaultExtensionLoader(): ExtensionLoader = object : ExtensionLoader {
    override suspend fun load(item: SubscribeItem) = when (item.type) {
        SourceType.Photo -> DefaultPhotoSource()
        else -> null
    }
}
