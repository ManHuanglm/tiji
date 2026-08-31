package com.wuji.app.data

import com.russhwolf.settings.Settings
import com.wuji.app.source.model.SubscribeSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 订阅源持久化仓库 - 对齐原项目 subscribeSourceStore.useStorageAsync 持久化逻辑。
 * 以 JSON 串形式存入 Settings,支持增删查。
 */
class SubscribeSourceRepository(
    private val settings: Settings,
    private val json: Json,
) {
    private val key = "subscribe_sources"

    /** 读取全部订阅源 */
    fun loadAll(): List<SubscribeSource> = runCatching {
        settings.getStringOrNull(key)?.let { raw ->
            json.decodeFromString<List<SubscribeSource>>(raw)
        } ?: emptyList()
    }.getOrDefault(emptyList())

    /** 全量保存 */
    fun saveAll(list: List<SubscribeSource>) {
        settings.putString(key, json.encodeToString(list))
    }

    /** 新增或更新(按 url 去重) */
    fun upsert(source: SubscribeSource) {
        val list = loadAll().toMutableList()
        val idx = list.indexOfFirst { it.url == source.url }
        if (idx >= 0) list[idx] = source else list.add(source)
        saveAll(list)
    }

    fun remove(url: String) {
        saveAll(loadAll().filterNot { it.url == url })
    }
}
