package com.wuji.app.source

import kotlinx.serialization.Serializable

/**
 * 订阅源资源类型枚举
 * 对应原项目 packages/source-extension/src/source/index.ts 的 SourceType
 */
@Serializable
enum class SourceType(val value: String) {
    Photo("photo"),
    Song("song"),
    Video("video"),
    Book("book"),
    Resource("resource"),
    Comic("comic");

    companion object {
        fun fromValue(value: String?): SourceType? = value?.let { v ->
            entries.firstOrNull { it.value == v }
        }
    }
}
