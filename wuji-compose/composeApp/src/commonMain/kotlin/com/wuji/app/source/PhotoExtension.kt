package com.wuji.app.source

import com.wuji.app.source.model.PhotoDetail
import com.wuji.app.source.model.PhotoItem
import com.wuji.app.source.model.PagedList

/**
 * 图片源扩展 - 对应原项目 PhotoExtension。
 * 实现三类核心能力:推荐列表、搜索、详情。
 */
abstract class PhotoExtension : Extension() {
    var hasDetailPage: Boolean = true

    /** 首页推荐列表 */
    abstract suspend fun getRecommendList(pageNo: Int = 1): PagedList<PhotoItem>?

    /** 搜索 */
    abstract suspend fun search(keyword: String, pageNo: Int = 1): PagedList<PhotoItem>?

    /** 获取图片详情(分页图集) */
    abstract suspend fun getPhotoDetail(item: PhotoItem, pageNo: Int = 1): PhotoDetail?

    /** 包裹推荐列表,补全 id/sourceId,对齐原项目 execGetRecommendList */
    suspend fun execGetRecommendList(pageNo: Int = 1): PagedList<PhotoItem>? {
        return try {
            getRecommendList(pageNo)?.let { ret ->
                ret.copy(list = ret.list.map { it.ensureIds(id) })
            }
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("execGetRecommendList failed: ${e.message}")
            null
        }
    }

    /** 包裹搜索:空关键词退化为推荐,对齐原项目 execSearch */
    suspend fun execSearch(keyword: String, pageNo: Int = 1): PagedList<PhotoItem>? {
        if (keyword.isBlank()) return execGetRecommendList(pageNo)
        return try {
            search(keyword, pageNo)?.let { ret ->
                ret.copy(list = ret.list.map { it.ensureIds(id) })
            }
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("execSearch failed: ${e.message}")
            null
        }
    }

    /** 包裹详情,补全 sourceId,对齐原项目 execGetPhotoDetail */
    suspend fun execGetPhotoDetail(item: PhotoItem, pageNo: Int = 1): PhotoDetail? {
        return try {
            getPhotoDetail(item, pageNo)?.copy(item = item.ensureIds(id), sourceId = id)
        } catch (e: Exception) {
            io.github.aakira.napier.Napier.w("execGetPhotoDetail failed: ${e.message}")
            null
        }
    }
}

/** 补全 PhotoItem 的 id 与 sourceId(对齐原项目 transformResult 逻辑) */
private fun PhotoItem.ensureIds(sid: String): PhotoItem = copy(
    id = id.ifBlank { url ?: (title + sid) },
    sourceId = sid,
)
