package com.wuji.app.core.platform

import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.wuji.app.core.storage.ContextHolder
import com.wuji.app.source.SourceFetcher
import org.koin.core.context.GlobalContext.get
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Android 端保存图片到公共相册目录。
 * - API ≥ 29 (Android 10+): 写入 MediaStore.Images.Media,无需外部存储权限
 * - API < 29: 写入 Environment.getExternalStoragePublicDirectory(Pictures),需 WRITE_EXTERNAL_STORAGE
 */
actual suspend fun saveImageToLocal(
    url: String,
    fileName: String,
    headers: Map<String, String>?,
): String {
    val context = ContextHolder.appContext
    val fetcher = get().get<SourceFetcher>()
    val bytes = fetcher.fetchBytes(url, headers)

    val ext = fileName.substringAfterLast('.', "png").take(6)
    val mimeType = when (ext.lowercase(Locale.ROOT)) {
        "jpg", "jpeg" -> "image/jpeg"
        "webp" -> "image/webp"
        "png", "" -> "image/png"
        else -> "image/*"
    }
    val safeName = fileName.replace(Regex("""[\\/:*?"<>|]"""), "_").let {
        if (it.contains('.') || it.length > 32) it else
            it + "_" + SimpleDateFormat("yyMMdd_HHmmss", Locale.US).format(Date()) + "." + ext
    }

    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, safeName)
            put(MediaStore.Images.Media.MIME_TYPE, mimeType)
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Wuji")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values,
        ) ?: error("MediaStore insert failed")
        context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        context.contentResolver.update(uri, values, null, null)
        uri.toString()
    } else {
        @Suppress("DEPRECATION")
        val dir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES,
        ).resolve("Wuji").apply { mkdirs() }
        val target = dir.resolve(safeName)
        target.writeBytes(bytes)
        target.absolutePath
    }
}
