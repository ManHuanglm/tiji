package com.wuji.app.download

/** Desktop 默认下载器:DesktopDownloader */
actual fun createPlatformDownloader(): Downloader = DesktopDownloader()
