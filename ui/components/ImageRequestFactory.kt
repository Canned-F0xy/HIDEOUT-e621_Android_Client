package com.CannedF0xy.hideout.ui.components

import android.content.Context
import androidx.compose.runtime.*
import coil.request.ImageRequest
import com.CannedF0xy.hideout.network.NetworkModule

@Composable
fun imageRequest(context: Context, imageUrl: Any, cookie: String = ""): ImageRequest {
    return remember(imageUrl, cookie) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .addHeader("User-Agent", NetworkModule.DEFAULT_USER_AGENT)
            .apply {
                if (cookie.isNotBlank()) addHeader("Cookie", cookie)
            }
            .crossfade(200)
            .build()
    }
}
