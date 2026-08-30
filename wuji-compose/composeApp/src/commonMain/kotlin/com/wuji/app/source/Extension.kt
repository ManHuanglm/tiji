package com.wuji.app.source

import com.wuji.app.source.dom.HtmlDocument
import com.wuji.app.source.dom.HtmlElement
import com.wuji.app.source.model.BookItem
import com.wuji.app.source.model.ComicItem
import com.wuji.app.source.model.VideoItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 源扩展基类 - 对应原项目 packages/source-extension/src/baseExtension.ts 的 Extension 抽象类。
 *
 * 提供源开发所需工具:网络请求 fetch、DOM 解析 fetchDom、URL 拼接 urlJoin、
 * 以及通用列表规则解析 queryBookElements / queryComicElements / queryVideoElements。
 *
 * 各资源类型的扩展(图/音/书/漫/视)继承本类并实现对应的抽象方法。
 */
abstract class Extension {
    /** 当前源 ID(由 SourceEngine 注入) */
    var id: String = ""

    /** 当前源基础地址,用于相对 URL 拼接 */
    var baseUrl: String = ""

    /** 网络请求器(由 SourceEngine 注入,实现与原项目 fetch 一致的能力:超时/重定向/代理) */
    lateinit var fetcher: SourceFetcher

    /** URL 拼接,对齐原项目 urlJoin(parts, { baseUrl }) */
    fun urlJoin(vararg parts: String?): String {
        val filtered = parts.filterNotNull().filter { it.isNotBlank() }
        if (filtered.isEmpty()) return baseUrl
        return buildString {
            if (filtered.first().startsWith("http").not() && baseUrl.isNotBlank()) {
                // 相对地址:以 baseUrl 为前缀拼接
                append(baseUrl.trimEnd('/'))
                if (filtered.first().startsWith("/").not()) append('/')
            }
            filtered.forEachIndexed { index, part ->
                val p = part.trim()
                if (index == 0 && p.startsWith("http")) {
                    append(p.trimEnd('/'))
                } else {
                    if (p.startsWith("/").not() && last() != '/') append('/')
                    append(p.trim('/'))
                }
            }
        }
    }

    /** 抓取并解析为 DOM,对齐原项目 fetchDom */
    suspend fun fetchDom(
        url: String,
        headers: Map<String, String>? = null,
        encoding: String? = null,
    ): HtmlDocument = withContext(Dispatchers.Default) {
        val html = fetcher.fetchText(url, headers, encoding)
        HtmlDocument.parse(html, baseUrl)
    }

    /**
     * 通用书籍/漫画列表规则解析,对齐原项目 queryBookElements / queryComicElements。
     * 通过 CSS 选择器从列表页 DOM 抽取结构化条目。
     */
    suspend fun queryBookElements(
        body: HtmlDocument,
        tags: BookListTags,
    ): List<BookItem> = queryElements(body, tags) { el, coverE ->
        BookItem(
            id = urlJoin(baseUrl, el.querySelector(tags.url)?.getAttribute("href") ?: ""),
            title = (el.querySelector(tags.title)?.textContent ?: el.getAttribute("title"))?.trim(),
            desc = el.querySelector(tags.intro)?.textContent?.trim(),
            cover = coverE,
            author = el.querySelector(tags.author)?.textContent?.trim(),
            tags = el.querySelectorAll(tags.tags).mapNotNull { it.textContent }.joinToString(" "),
            status = el.querySelector(tags.status)?.textContent?.trim(),
            url = el.querySelector(tags.url)?.getAttribute("href")?.let { urlJoin(baseUrl, it) },
            latestChapter = el.querySelector(tags.latestChapter)?.textContent?.trim(),
            latestUpdate = el.querySelector(tags.latestUpdate)?.textContent?.trim(),
            sourceId = "",
        )
    }.filter { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() }

    /** 漫画列表解析复用书籍逻辑(与原项目 this.queryComicElements = this.queryBookElements 一致) */
    suspend fun queryComicElements(
        body: HtmlDocument,
        tags: BookListTags,
    ): List<ComicItem> = queryElements(body, tags) { el, coverE ->
        ComicItem(
            id = urlJoin(baseUrl, el.querySelector(tags.url)?.getAttribute("href") ?: ""),
            title = (el.querySelector(tags.title)?.textContent ?: el.getAttribute("title"))?.trim(),
            desc = el.querySelector(tags.intro)?.textContent?.trim(),
            cover = coverE,
            author = el.querySelector(tags.author)?.textContent?.trim(),
            tags = el.querySelectorAll(tags.tags).mapNotNull { it.textContent }.joinToString(" "),
            status = el.querySelector(tags.status)?.textContent?.trim(),
            url = el.querySelector(tags.url)?.getAttribute("href")?.let { urlJoin(baseUrl, it) },
            latestChapter = el.querySelector(tags.latestChapter)?.textContent?.trim(),
            latestUpdate = el.querySelector(tags.latestUpdate)?.textContent?.trim(),
            sourceId = "",
        )
    }.filter { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() }

    /**
     * 视频列表规则解析,对齐原项目 queryVideoElements。
     */
    suspend fun queryVideoElements(
        body: HtmlDocument,
        tags: VideoListTags,
    ): List<VideoItem> = queryElements(body, tags.toBookTags()) { el, coverE ->
        VideoItem(
            id = urlJoin(baseUrl, el.querySelector(tags.url)?.getAttribute("href") ?: ""),
            title = (el.querySelector(tags.title)?.textContent ?: el.getAttribute("title"))?.trim(),
            intro = el.querySelector(tags.intro)?.textContent?.trim(),
            cover = coverE,
            releaseDate = el.querySelector(tags.releaseDate)?.textContent?.trim(),
            country = el.querySelector(tags.country)?.textContent?.trim(),
            duration = el.querySelector(tags.duration)?.textContent?.trim(),
            director = el.querySelector(tags.director)?.textContent?.trim(),
            actors = el.querySelector(tags.cast)?.textContent?.trim(),
            tags = el.querySelectorAll(tags.tags).mapNotNull { it.textContent }.joinToString(" "),
            status = el.querySelector(tags.status)?.textContent?.trim(),
            url = el.querySelector(tags.url)?.getAttribute("href")?.let { urlJoin(baseUrl, it) },
            sourceId = "",
        )
    }.filter { !it.title.isNullOrBlank() && !it.url.isNullOrBlank() }

    /**
     * 列表元素通用抽取骨架:遍历 element 选择器,解析懒加载封面 url,再交由 [map] 映射为具体条目。
     */
    private suspend fun <T> queryElements(
        body: HtmlDocument,
        tags: BookListTags,
        map: suspend (HtmlElement, String?) -> T,
    ): List<T> = withContext(Dispatchers.Default) {
        body.querySelectorAll(tags.element).mapNotNull { el ->
            val coverE = resolveCover(el.querySelector(tags.cover), tags.coverDomain)
            try {
                map(el, coverE)
            } catch (e: Exception) {
                Napier.w("queryElements 映射失败: ${e.message}")
                null
            }
        }
    }

    /** 解析懒加载封面,对齐原项目对 data-original / lazy-src / data-src / style.backgroundImage 等的处理 */
    private fun resolveCover(img: HtmlElement?, coverDomain: String?): String? {
        if (img == null) return null
        val candidates = listOf(
            "data-original", "data-original-src", "lazy-src",
            "data-lazy-src", "data-lazy-original-src", "data-img",
            "data-src", "src", "data-setbg",
        )
        var cover = candidates.firstNotNullOfOrNull { img.getAttribute(it) }
        if (cover.isNullOrBlank()) {
            // 兼容 style.backgroundImage: url(...)
            // Ksoup style 简化处理:attr("style")
            val style = img.getAttribute("style") ?: ""
            val regex = Regex("""url\(["']?(.*?)["']?\)""")
            cover = regex.find(style)?.groupValues?.getOrNull(1)
        }
        return cover?.let { normalizeCoverUrl(it, coverDomain) }
    }

    private fun normalizeCoverUrl(cover: String, coverDomain: String?): String? {
        if (cover.startsWith("http://") || cover.startsWith("https://")) return cover
        if (cover.startsWith("//")) return "https:$cover"
        if (cover.startsWith("data:")) return cover // base64 直接保留
        return urlJoin(coverDomain ?: baseUrl, cover)
    }
}

/** 书籍/漫画列表选择器规则 */
data class BookListTags(
    val element: String = ".bookbox",
    val cover: String = "img",
    val coverHeaders: Map<String, String>? = null,
    val title: String = "h3 a",
    val intro: String = ".intro",
    val author: String = ".author a",
    val tags: String = ".tags",
    val status: String = ".status",
    val url: String = "a",
    val latestChapter: String = ".latestchapter a",
    val latestUpdate: String = ".update",
    val coverDomain: String? = null,
)

/** 视频列表选择器规则 */
data class VideoListTags(
    val element: String = ".bookbox",
    val cover: String = "img",
    val title: String = "h3 a",
    val intro: String = ".intro",
    val releaseDate: String = ".year",
    val country: String = ".area",
    val duration: String = ".time",
    val director: String = ".director",
    val cast: String = ".actor",
    val tags: String = ".tags",
    val status: String = ".status",
    val url: String = "a",
    val latestUpdate: String = ".update",
    val coverDomain: String? = null,
    val coverHeaders: Map<String, String>? = null,
) {
    fun toBookTags() = BookListTags(
        element, cover, coverHeaders, title, intro, director, tags, status, url, "", latestUpdate, coverDomain,
    )
}

/** 网络请求抽象(由 SourceEngine 提供具体实现,内部走 Ktor,封装超时/重定向/代理/编码) */
interface SourceFetcher {
    suspend fun fetchText(
        url: String,
        headers: Map<String, String>? = null,
        encoding: String? = null,
    ): String

    /** 便捷别名(fetchString = fetchText,供业务直观调用) */
    suspend fun fetchString(url: String, headers: Map<String, String>? = null): String? =
        runCatching { fetchText(url, headers) }.getOrNull()

    suspend fun fetchBytes(
        url: String,
        headers: Map<String, String>? = null,
    ): ByteArray

    /**
     * 流式获取,用于下载、图片保存等大文件场景。
     * 流式避免一次性占用内存;默认实现通过 [fetchBytes] 包一层 ByteArrayInputStream,
     * 具体 Ktor/KtorSourceFetcher 可重写为真实 ByteReadChannel→InputStream(若需要更高效)。
     */
    suspend fun fetchStream(
        url: String,
        headers: Map<String, String>? = null,
    ): java.io.InputStream? = runCatching {
        java.io.ByteArrayInputStream(fetchBytes(url, headers))
    }.getOrNull()
}
