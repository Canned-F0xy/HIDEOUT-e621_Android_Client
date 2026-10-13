package com.CannedF0xy.hideout.ui.components

import android.annotation.SuppressLint
import android.view.GestureDetector
import android.view.MotionEvent
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.CannedF0xy.hideout.i18n.LocalStrings

private val PLAYBACK_SPEEDS = listOf(0.5f, 1f, 1.25f, 1.5f, 2f)
private const val SEEK_STEP_MS = 10_000L

@SuppressLint("ClickableViewAccessibility")
@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    var isControllerVisible by remember { mutableStateOf(false) }
    var isMuted by remember(exoPlayer) { mutableStateOf(exoPlayer.volume == 0f) }
    var speed by remember(exoPlayer) { mutableFloatStateOf(exoPlayer.playbackParameters.speed) }

    val playerView = remember {
        PlayerView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            useController = true
        }
    }

    DisposableEffect(exoPlayer) {
        playerView.player = exoPlayer
        playerView.setControllerVisibilityListener(PlayerView.ControllerVisibilityListener { visibility ->
            isControllerVisible = visibility == android.view.View.VISIBLE
        })
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
            playerView.setControllerVisibilityListener(null as PlayerView.ControllerVisibilityListener?)
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

        if (isControllerVisible) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 12.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable {
                            isMuted = !isMuted
                            exoPlayer.volume = if (isMuted) 0f else 1f
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = strings.cdToggleMute,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .clickable {
                            val index = PLAYBACK_SPEEDS.indexOf(speed)
                            speed = PLAYBACK_SPEEDS[(if (index < 0) 1 else index + 1) % PLAYBACK_SPEEDS.size]
                            exoPlayer.playbackParameters = PlaybackParameters(speed)
                        }
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(formatSpeed(speed), color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun formatSpeed(speed: Float): String {
    val text = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
    return "${text}x"
}
