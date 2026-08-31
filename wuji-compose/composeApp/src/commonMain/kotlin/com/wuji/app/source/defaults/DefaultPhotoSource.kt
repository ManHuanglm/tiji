package com.wuji.app.source.defaults

import com.wuji.app.core.network.urlEncode
import com.wuji.app.source.PhotoExtension
import com.wuji.app.source.dom.HtmlDocument
import com.wuji.app.source.model.PhotoDetail
import com.wuji.app.source.model.PhotoItem
import com.wuji.app.source.model.PagedList

/**
 * 内置示例图片源 - 演示源引擎运行(无需外部 JS 源即可浏览内容)。
 * 解析一个公共壁纸站的列表/搜索/详情页,作为引擎正确性的样板。
 *
 * 实际订阅源通过 SourceEngine + ExtensionLoader 动态加载,
 * 本类仅用于离线演示与引擎联调。
 */
class DefaultPhotoSource : PhotoExtension() {

    private val host = "https://wallhaven.cc"

    override suspend fun getRecommendList(pageNo: Int): PagedList<PhotoItem>? {
        val url = "$host/search?categories=111&purity=100&sorting=toplist&page=$pageNo"
        val doc = fetchDom(url, mapOf("User-Agent" to DEFAULT_UA))
        return parseList(doc)
    }

    override suspend fun search(keyword: String, pageNo: Int): PagedList<PhotoItem>? {
        val url = "$host/search?q=${keyword.urlEncode()}&page=$pageNo"
        val doc = fetchDom(url, mapOf("User-Agent" to DEFAULT_UA))
        return parseList(doc)
    }

    override suspend fun getPhotoDetail(item: PhotoItem, pageNo: Int): PhotoDetail? {
        val url = item.url ?: return null
        val doc = fetchDom(url, mapOf("User-Agent" to DEFAULT_UA))
        val photo = doc.querySelector("#wallpaper")?.getAttribute("src")
        return PhotoDetail(
            item = item,
            photos = listOfNotNull(photo),
            page = 1,
            sourceId = id,
        )
    }

    private fun parseList(doc: HtmlDocument): PagedList<PhotoItem> {
        val items = doc.querySelectorAll("figure.thumb-listing-item").mapNotNull { el ->
            val preview = el.querySelector("img")?.getAttribute("data-src")
                ?: el.querySelector("img")?.getAttribute("src")
            val link = el.querySelector("a.preview")?.getAttribute("href")
            val resolution = el.querySelector(".wall-res")?.textContent
            if (link.isNullOrBlank()) null else PhotoItem(
                id = link,
                title = resolution,
                cover = listOfNotNull(preview),
                url = link,
                sourceId = id,
            )
        }
        val lastPage = doc.querySelector("ul.pagination li:last-child a")?.getAttribute("href")
        val totalPage = lastPage?.let { Regex("page=(\\d+)").find(it)?.groupValues?.get(1)?.toIntOrNull() }
        return PagedList(list = items, page = 1, totalPage = totalPage)
    }

    private val DEFAULT_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/130.0 Safari/537.36"
}
