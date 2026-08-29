package com.wuji.app.core.network

import com.wuji.app.source.SourceFetcher
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.SIMPLE
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.readBytes
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Ktor HttpClient 工厂 - 对齐原项目 @wuji-tauri/fetch 的能力(超时/重定向/JSON/UA)。
 * 默认超时 30s,可配置 UA,用于源引擎与通用网络请求。
 */
object HttpClientFactory {

    val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        encodeDefaults = false
    }

    fun create(
        userAgent: String = DEFAULT_UA,
        connectTimeoutMs: Long = 30_000L,
        requestTimeoutMs: Long = 30_000L,
    ): HttpClient = HttpClient {
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = connectTimeoutMs
            requestTimeoutMillis = requestTimeoutMs
        }
        install(UserAgent) { agent = userAgent }
        install(io.ktor.client.plugins.logging.Logging) {
            level = LogLevel.NONE
            logger = Logger.SIMPLE
        }
        defaultRequest {
            headers.append("Accept", "text/html,application/json,*/*")
        }
    }

    /** 浏览器 UA,模拟桌面 Chrome,避免部分源拦截非浏览器请求 */
    const val DEFAULT_UA =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36"
}

/**
 * 基于 Ktor 的 [SourceFetcher] 实现,封装超时/重定向/字符编码。
 * 对齐原项目 fetch:统一返回响应文本。
 */
class KtorSourceFetcher(private val client: HttpClient) : SourceFetcher {

    override suspend fun fetchText(
        url: String,
        headers: Map<String, String>?,
        encoding: String?,
    ): String {
        val response = doRequest(url, headers)
        if (!response.status.isSuccess()) {
            Napier.w("fetchText 失败 [$url] status=${response.status.value}")
        }
        // 编码:原项目支持 gbk,这里简化为按响应字节流解码。Ktor 默认按 Content-Type 解码。
        val bytes = response.readBytes()
        return if (encoding.equals("gbk", ignoreCase = true)) {
            decodeGbk(bytes)
        } else {
            bytes.toString(Charsets.UTF_8)
        }
    }

    override suspend fun fetchBytes(url: String, headers: Map<String, String>?): ByteArray {
        val response = doRequest(url, headers)
        return response.readBytes()
    }

    private suspend fun doRequest(url: String, extraHeaders: Map<String, String>?): HttpResponse {
        return client.get(url) {
            extraHeaders?.forEach { (k, v) -> headers.append(k, v) }
        }
    }

    /** GBK 解码兜底(JVM/Desktop:Charset;Android 等平台需提供实际) */
    private fun decodeGbk(bytes: ByteArray): String {
        return try {
            String(bytes, charset("GBK"))
        } catch (e: Exception) {
            bytes.toString(Charsets.UTF_8)
        }
    }
}
