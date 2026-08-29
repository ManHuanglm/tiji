package com.wuji.app.ui.screen.photo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.wuji.app.core.platform.saveImageToLocal
import com.wuji.app.source.SourceEngine
import com.wuji.app.source.model.PhotoDetail
import com.wuji.app.source.model.PhotoItem
import io.github.aakira.napier.Napier
import kotlinx.coroutines.launch

/**
 * 图片详情 ScreenModel - 承载详情加载、分页图集、保存图片三部分状态。
 * 对齐原项目 PhotoDetail.vue 的详情加载逻辑与下载功能。
 */
class PhotoDetailScreenModel(private val sourceEngine: SourceEngine) : ScreenModel {

    var uiState by mutableStateOf<PhotoDetailUiState>(PhotoDetailUiState.Loading)
        private set

    /** 保存进度态 (null=空闲, 非空=正在保存/保存结果) */
    var saveResult by mutableStateOf<SaveResult?>(null)
        private set

    private var currentDetail: PhotoDetail? = null

    fun load(item: PhotoItem, pageNo: Int = 1) {
        uiState = PhotoDetailUiState.Loading
        screenModelScope.launch {
            uiState = try {
                val ext = sourceEngine.getPhotoExtensions().firstOrNull { it.id == item.sourceId }
                val detail = ext?.execGetPhotoDetail(item, pageNo)
                if (detail == null || detail.photos.isEmpty()) {
                    PhotoDetailUiState.Error("未获取到图片")
                } else {
                    currentDetail = detail
                    PhotoDetailUiState.Success(
                        photos = detail.photos,
                        page = detail.page,
                        totalPage = detail.totalPage,
                        headers = detail.photosHeaders,
                    )
                }
            } catch (e: Exception) {
                Napier.w("PhotoDetail load: ${e.message}")
                PhotoDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /** 加载下一页图集 */
    fun loadNext(item: PhotoItem) {
        val cur = uiState as? PhotoDetailUiState.Success ?: return
        if (cur.loadingMore || cur.totalPage != null && cur.page >= cur.totalPage) return
        uiState = cur.copy(loadingMore = true)
        screenModelScope.launch {
            val nextPage = cur.page + 1
            val ext = sourceEngine.getPhotoExtensions().firstOrNull { it.id == item.sourceId }
            val detail = ext?.execGetPhotoDetail(item, nextPage)
            uiState = if (detail == null || detail.photos.isEmpty()) {
                cur.copy(hasMore = false, loadingMore = false)
            } else {
                currentDetail = detail
                cur.copy(
                    photos = cur.photos + detail.photos,
                    page = nextPage,
                    hasMore = (detail.totalPage ?: Int.MAX_VALUE) > nextPage,
                    loadingMore = false,
                )
            }
        }
    }

    /** 保存单张图片到本地 */
    fun savePhoto(url: String, headers: Map<String, String>?) {
        if (saveResult is SaveResult.Saving) return
        val name = url.substringAfterLast('/', "photo.png").takeIf { it.contains('.') }
            ?: "photo_${System.currentTimeMillis()}.jpg"
        saveResult = SaveResult.Saving(name)
        screenModelScope.launch {
            saveResult = runCatching {
                val path = saveImageToLocal(url, name, headers)
                SaveResult.Ok(name, path)
            }.getOrElse { e ->
                Napier.w("savePhoto failed: ${e.message}")
                SaveResult.Fail(name, e.message ?: "保存失败")
            }
        }
    }

    fun clearSaveResult() { saveResult = null }
}

/** 保存进度态 */
sealed interface SaveResult {
    val fileName: String
    data class Saving(override val fileName: String) : SaveResult
    data class Ok(override val fileName: String, val savedPath: String) : SaveResult
    data class Fail(override val fileName: String, val message: String) : SaveResult
}

sealed interface PhotoDetailUiState {
    data object Loading : PhotoDetailUiState
    data class Error(val message: String) : PhotoDetailUiState
    data class Success(
        val photos: List<String>,
        val page: Int,
        val totalPage: Int? = null,
        val hasMore: Boolean = true,
        val loadingMore: Boolean = false,
        val headers: Map<String, String>? = null,
    ) : PhotoDetailUiState
}
