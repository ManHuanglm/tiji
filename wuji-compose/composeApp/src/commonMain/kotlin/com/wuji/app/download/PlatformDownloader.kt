package com.wuji.app.download

/** 各平台创建默认 Downloader。用于在 commonMain 中统一绑定到 Koin 容器。 */
expect fun createPlatformDownloader(): Downloader
