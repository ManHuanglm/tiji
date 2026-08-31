package com.wuji.app.source

import com.wuji.app.source.model.SubscribeItem
import com.wuji.app.source.model.SubscribeSource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 源引擎 - 对齐原项目 extensionStore + subscribeSourceStore 的运行时分发逻辑。
 *
 * 职责:
 * 1. 持有已加载的各类型源扩展实例(按 sourceId 索引)
 * 2. 将启用的订阅源按 [SourceType] 注入到对应资源仓库(photo/song/book/comic/video)
 * 3. 提供类型化批量调用入口(推荐/搜索)
 *
 * 原项目通过 new Function(codeString) 动态执行 JS 源代码;本实现提供 Kotlin 接口化的源注册,
 * JS 源兼容通过 [ExtensionLoader] 桥接(推荐 QuickJS-kt,见文档)。
 */
class SourceEngine(
    private val loader: ExtensionLoader,
    private val fetcher: SourceFetcher,
) {
    /** 各类型启用的源扩展(sourceId -> Extension) */
    private val photoExtensions = mutableMapOf<String, PhotoExtension>()
    private val songExtensions = mutableMapOf<String, SongExtension>()
    private val bookExtensions = mutableMapOf<String, BookExtension>()
    private val comicExtensions = mutableMapOf<String, ComicExtension>()
    private val videoExtensions = mutableMapOf<String, VideoExtension>()

    private val mutex = Mutex()

    /**
     * 加载并注册一个订阅源下全部子项,对齐原项目 loadSubscribeSources。
     * 将每个 [SubscribeItem] 经 [ExtensionLoader] 解析为 Extension 实例并注入对应类型表。
     */
    suspend fun loadSubscribeSource(source: SubscribeSource) = mutex.withLock {
        source.detail.urls.filter { !it.disable }.forEach { item ->
            try {
                val ext = loader.load(item)
                if (ext != null) {
                    ext.id = item.id
                    ext.baseUrl = baseUrlOf(item.url)
                    ext.fetcher = fetcher
                    registerTyped(item, ext)
                    Napier.i("已加载源: ${item.name} (${item.type})")
                }
            } catch (e: Exception) {
                Napier.w("加载源失败 ${item.name}: ${e.message}")
            }
        }
    }

    /** 按 [SourceType] 注入扩展实例到对应表 */
    private fun registerTyped(item: SubscribeItem, ext: Extension) {
        when (item.type) {
            SourceType.Photo -> (ext as? PhotoExtension)?.let { photoExtensions[item.id] = it }
            SourceType.Song -> (ext as? SongExtension)?.let { songExtensions[item.id] = it }
            SourceType.Book -> (ext as? BookExtension)?.let { bookExtensions[item.id] = it }
            SourceType.Comic -> (ext as? ComicExtension)?.let { comicExtensions[item.id] = it }
            SourceType.Video -> (ext as? VideoExtension)?.let { videoExtensions[item.id] = it }
            SourceType.Resource -> Unit
        }
    }

    fun getPhotoExtensions(): List<PhotoExtension> = photoExtensions.values.toList()
    fun getSongExtensions(): List<SongExtension> = songExtensions.values.toList()
    fun getBookExtensions(): List<BookExtension> = bookExtensions.values.toList()
    fun getComicExtensions(): List<ComicExtension> = comicExtensions.values.toList()
    fun getVideoExtensions(): List<VideoExtension> = videoExtensions.values.toList()

    fun removeSource(sourceId: String) {
        photoExtensions.remove(sourceId)
        songExtensions.remove(sourceId)
        bookExtensions.remove(sourceId)
        comicExtensions.remove(sourceId)
        videoExtensions.remove(sourceId)
    }

    private fun baseUrlOf(url: String): String {
        if (url.isBlank()) return ""
        val schemeEnd = url.indexOf("://").takeIf { it > 0 } ?: return url
        val hostEnd = url.indexOf('/', schemeEnd + 3)
        return if (hostEnd > 0) url.substring(0, hostEnd) else url
    }
}

/**
 * 源扩展加载器:将 [SubscribeItem] 解析为具体的 [Extension] 实例。
 * - 声明式源(JSON 规则):直接构造 Kotlin 扩展
 * - JS 代码源(code 字段):由平台实现通过 JS 引擎(QuickJS-kt)桥接执行
 */
interface ExtensionLoader {
    suspend fun load(item: SubscribeItem): Extension?
}
