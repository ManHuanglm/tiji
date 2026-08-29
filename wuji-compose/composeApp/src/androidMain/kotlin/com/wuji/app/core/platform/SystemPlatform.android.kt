package com.wuji.app.core.platform

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.wuji.app.core.storage.ContextHolder
import java.io.File

/** Android 端版本信息:直接读 PackageInfo */
actual object AppInfo {
    actual val versionName: String
        get() = runCatching {
            val ctx = ContextHolder.appContext
            val pm: PackageManager = ctx.packageManager
            val info: PackageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(ctx.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(ctx.packageName, 0)
            }
            info.versionName ?: "1.0.0"
        }.getOrDefault("1.0.0")

    actual val versionCode: Int
        get() = runCatching {
            val ctx = ContextHolder.appContext
            val pm = ctx.packageManager
            val info = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.getPackageInfo(ctx.packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                @Suppress("DEPRECATION")
                pm.getPackageInfo(ctx.packageName, 0)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) info.longVersionCode.toInt()
            else @Suppress("DEPRECATION") info.versionCode
        }.getOrDefault(1)
}

actual fun cacheDirByteSize(): Long {
    val ctx = ContextHolder.appContext
    var size = walkSize(ctx.cacheDir)
    ctx.externalCacheDir?.let { size += walkSize(it) }
    return size
}

actual fun clearCacheDir() {
    val ctx = ContextHolder.appContext
    ctx.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
    ctx.externalCacheDir?.listFiles()?.forEach { it.deleteRecursively() }
}

private fun walkSize(f: File): Long {
    if (!f.exists()) return 0L
    return if (f.isFile) f.length() else f.listFiles()?.sumOf(::walkSize) ?: 0L
}
