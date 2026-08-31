package com.wuji.app.core.network

import java.net.URLEncoder

actual fun String.urlEncode(): String =
    URLEncoder.encode(this, "UTF-8").replace("+", "%20")
