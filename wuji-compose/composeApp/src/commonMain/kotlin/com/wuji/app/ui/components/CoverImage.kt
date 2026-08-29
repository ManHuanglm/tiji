package com.wuji.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource

/** 详情页通用封面 - 1:1 比例,加载中占位,失败占位。 */
@Composable
fun CoverImage(
    url: String?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    aspect: Float = 0.75f,
) {
    val safeUrl = url
    if (safeUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(aspect)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(contentDescription.orEmpty(), color = Color.Gray)
        }
        return
    }
    KamelImage(
        resource = asyncPainterResource(safeUrl),
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        onLoading = { CircularProgressIndicator() },
        onFailure = {
            Box(contentAlignment = Alignment.Center) {
                Text("无封面", color = Color.Gray)
            }
        },
        modifier = modifier.fillMaxWidth().aspectRatio(aspect),
    )
}
