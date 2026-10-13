package com.CannedF0xy.hideout.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.CannedF0xy.hideout.i18n.LocalStrings
import java.io.File

@Composable
fun ZoomableImage(imageUrl: String, imageLoader: ImageLoader, cfClearanceCookie: String, onClose: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val strings = LocalStrings.current
    val context = LocalContext.current

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scale = if (scale > 1f) 1f else 2.5f; offset = Offset.Zero }) }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) offset += pan else offset = Offset.Zero
                }
            }
    ) {
        val zoomImageRequest = remember(imageUrl, cfClearanceCookie) {
            ImageRequest.Builder(context)
                .data(if (imageUrl.startsWith("/")) File(imageUrl) else imageUrl)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36")
                .apply { if (cfClearanceCookie.isNotBlank()) addHeader("Cookie", cfClearanceCookie) }
                .build()
        }

        AsyncImage(model = zoomImageRequest, imageLoader = imageLoader, contentDescription = "Zoomable Image", modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y), contentScale = ContentScale.Fit)
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).systemBarsPadding().padding(16.dp)) { Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = Color.White) }
    }
}
