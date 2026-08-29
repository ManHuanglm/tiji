package com.wuji.app.download

/** Android 默认下载器:AndroidDownloader */
actual fun createPlatformDownloader(): Downloader = AndroidDownloader()
