package com.wuji.app.core.platform

/** 应用版本信息跨平台对象 */
expect object AppInfo {
    val versionName: String
    val versionCode: Int
}

/** 统计缓存目录总大小(字节)。Kamel/Coil/Ktor 缓存均会算入。 */
expect fun cacheDirByteSize(): Long

/** 清空应用缓存目录(不删除空目录本体)。 */
expect fun clearCacheDir()
