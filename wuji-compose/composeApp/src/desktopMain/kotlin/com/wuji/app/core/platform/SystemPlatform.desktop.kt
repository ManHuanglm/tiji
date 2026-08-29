package com.wuji.app.core.platform

import java.io.File

/** Desktop 端:缓存目录使用 ~/.cache/wuji (Linux) / ~/Library/Caches/Wuji (mac) / %AppData%\Wuji\Cache (Win) */
actual object AppInfo {
    actual val versionName: String get() = "1.0.0"
    actual val versionCode: Int get() = 1
}

private fun cacheDir(): File {
    val os = System.getProperty("os.name", "").lowercase()
    val home = File(System.getProperty("user.home"))
    return when {
        os.startsWith("mac") -> home.resolve("Library/Caches/Wuji")
        os.startsWith("win") -> {
            val app = System.getenv("AppData")?.let(::File)
                ?: home.resolve("AppData").resolve("Roaming")
            app.resolve("Wuji").resolve("Cache")
        }
        else -> {
            val xdg = System.getenv("XDG_CACHE_HOME")?.let(::File)
                ?: home.resolve(".cache")
            xdg.resolve("wuji")
        }
    }
}

actual fun cacheDirByteSize(): Long {
    val dir = cacheDir()
    return walkSize(dir)
}

actual fun clearCacheDir() {
    cacheDir().listFiles()?.forEach { it.deleteRecursively() }
}

private fun walkSize(f: File): Long {
    if (!f.exists()) return 0L
    return if (f.isFile) f.length() else f.listFiles()?.sumOf(::walkSize) ?: 0L
}
