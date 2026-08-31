package com.wuji.app.source.dom

import com.fleeksoft.ksoup.Ksoup
import com.fleeksoft.ksoup.nodes.Document
import com.fleeksoft.ksoup.nodes.Element
import com.fleeksoft.ksoup.select.Elements

/**
 * HTML 文档抽象,封装 Ksoup,对齐原项目 fetchDom 返回的 DOM Document API。
 * 提供与浏览器 DOM 一致的 querySelector / querySelectorAll / getAttribute / textContent 能力。
 */
class HtmlDocument(private val doc: Document) {

    /** CSS 选择器选取全部节点 */
    fun querySelectorAll(selector: String): List<HtmlElement> =
        doc.select(selector).map { HtmlElement(it) }

    /** CSS 选择器选取首个节点 */
    fun querySelector(selector: String): HtmlElement? =
        doc.selectFirst(selector)?.let { HtmlElement(it) }

    companion object {
        /** 由 HTML 文本解析为文档 */
        fun parse(html: String, baseUri: String? = null): HtmlDocument =
            if (baseUri != null) HtmlDocument(Ksoup.parse(html, baseUri))
            else HtmlDocument(Ksoup.parse(html))
    }
}

/**
 * HTML 元素抽象,对齐浏览器 HTMLElement/DOM API
 */
class HtmlElement(private val element: Element) {

    fun querySelector(selector: String): HtmlElement? =
        element.selectFirst(selector)?.let { HtmlElement(it) }

    fun querySelectorAll(selector: String): List<HtmlElement> =
        element.select(selector).map { HtmlElement(it) }

    /** 等价于 element.getAttribute(name) */
    fun getAttribute(name: String): String? = element.attr(name).takeIf { it.isNotEmpty() }

    /** 等价于 textContent */
    val textContent: String? get() = element.text().takeIf { it.isNotEmpty() }

    /** href 等属性快捷读取 */
    fun attr(name: String): String = element.attr(name)

    fun hasAttr(name: String): Boolean = element.hasAttr(name)

    val ownText: String get() = element.ownText()

    fun parents(): List<HtmlElement> = element.parents().map { HtmlElement(it) }

    fun select(css: String): Elements = element.select(css)
}
