package com.CannedF0xy.hideout.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val baseColor = MaterialTheme.colorScheme.surfaceVariant
    val colors = listOf(
        baseColor.copy(alpha = 0.3f),
        baseColor.copy(alpha = 0.6f),
        baseColor.copy(alpha = 0.3f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translation"
    )
    return this.background(
        brush = Brush.linearGradient(
            colors = colors,
            start = Offset.Zero,
            end = Offset(x = translateAnim.value, y = translateAnim.value)
        )
    )
}
