package com.wuji.app.core.platform

import com.wuji.app.source.SourceFetcher
import org.koin.core.context.GlobalContext
import java.io.File
import javax.imageio.ImageIO

/**
 * Desktop 端保存图片到本地。
 * 统一使用 ~/Pictures/Wuji 目录(对齐 Tauri 默认写入行为)。
 * 依赖全局 Koin 取出 SourceFetcher 获取图片字节流(复用已配置的代理/UA/Cookie)。
 */
actual suspend fun saveImageToLocal(
    url: String,
    fileName: String,
    headers: Map<String, String>?,
): String {
    val dir = File(System.getProperty("user.home"), "Pictures/Wuji").apply { mkdirs() }
    val ext = fileName.substringAfterLast('.', "png").let {
        if (it.length > 6) "png" else it
    }
    val safeName = fileName.replace(Regex("""[\\/:*?"<>|]"""), "_")
    val target = File(dir, safeName)

    // 通过 SourceFetcher 拉取字节(复用 Ktor:代理/UA 等)
    val fetcher = GlobalContext.get().get<SourceFetcher>()
    val bytes = fetcher.fetchBytes(url, headers)
    target.writeBytes(bytes)
    return target.absolutePath
}
