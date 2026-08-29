package com.wuji.app.source.model

import com.wuji.app.source.SourceType
import kotlinx.serialization.Serializable

/**
 * 单个订阅项(一类资源的一个具体源)
 * 对应原项目 SubscribeItem
 */
@Serializable
data class SubscribeItem(
    val id: String,
    val name: String,
    val type: SourceType,
    val url: String,
    val disable: Boolean = false,
    val code: String? = null,
)

/**
 * 订阅源明细(一个订阅源 URL 对应的元信息与子项列表)
 * 对应原项目 SubscribeDetail
 */
@Serializable
data class SubscribeDetail(
    val id: String,
    val name: String,
    val version: Int = 1,
    val requireVersion: Int? = null,
    val urls: List<SubscribeItem> = emptyList(),
)

/**
 * 已导入的订阅源
 * 对应原项目 SubscribeSource
 */
@Serializable
data class SubscribeSource(
    val url: String,
    val disable: Boolean = false,
    val detail: SubscribeDetail,
)

/**
 * 市场源权限
 */
@Serializable
enum class MarketSourcePermission(val value: String) {
    NoLogin("noLogin"),
    Login("login"),
    Vip("vip"),
    Pro("pro");

    companion object {
        fun fromValue(v: String?): MarketSourcePermission? = v?.let {
            entries.firstOrNull { entry -> entry.value == v }
        }
    }
}

@Serializable
data class MarketSourceContent(
    val id: String = "",
    val name: String,
    val type: SourceType,
    val disabled: Boolean = false,
    val source: String,
    val url: String,
    val code: String? = null,
)

@Serializable
data class MarketSource(
    val id: String,
    val name: String,
    val version: Int = 1,
    val permissions: List<MarketSourcePermission> = emptyList(),
    val sourceContents: List<MarketSourceContent> = emptyList(),
    val isPublic: Boolean = true,
    val isBanned: Boolean = false,
    val thumbsUp: Int = 0,
)

@Serializable
data class PagedMarketSource(
    val data: List<MarketSource> = emptyList(),
    val page: Int = 1,
    val limit: Int = 20,
    val total: Int = 0,
    val totalPages: Int = 0,
)
