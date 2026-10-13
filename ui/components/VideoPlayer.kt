package com.CannedF0xy.hideout.ui.components

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

private const val SEEK_STEP_MS = 10_000L

@SuppressLint("ClickableViewAccessibility")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current

    val playerView = remember {
        PlayerView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            useController = true
        }
    }

    DisposableEffect(exoPlayer) {
        playerView.player = exoPlayer
        val detector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                val forward = e.x > playerView.width / 2f
                val duration = exoPlayer.duration.coerceAtLeast(0L)
                val target = exoPlayer.currentPosition + if (forward) SEEK_STEP_MS else -SEEK_STEP_MS
                exoPlayer.seekTo(if (duration > 0) target.coerceIn(0L, duration) else target.coerceAtLeast(0L))
                return true
            }
        })
        playerView.setOnTouchListener { _, event -> detector.onTouchEvent(event) }
        onDispose {
            playerView.setOnTouchListener(null)
            playerView.player = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { playerView },
            update = { view ->
                view.post {
                    view.requestLayout()
                    view.invalidate()
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
