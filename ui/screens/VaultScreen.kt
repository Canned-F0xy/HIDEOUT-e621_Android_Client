package com.CannedF0xy.hideout.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import coil.ImageLoader
import coil.compose.AsyncImage
import com.CannedF0xy.hideout.download.VaultStore
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.ui.components.OverlayScaffold
import com.CannedF0xy.hideout.ui.components.VideoPlayer
import com.CannedF0xy.hideout.ui.components.ZoomableImage
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun VaultScreen(imageLoader: ImageLoader, onClose: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val files by VaultStore.files.collectAsState()
    var viewing by remember { mutableStateOf<File?>(null) }

    LaunchedEffect(Unit) { withContext(Dispatchers.IO) { VaultStore.refresh(context) } }

    OverlayScaffold(title = strings.vaultTitle, onBack = onClose) {
        if (files.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Text(strings.vaultEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 110.dp),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(files, key = { it.absolutePath }) { file ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { viewing = file },
                        contentAlignment = Alignment.Center
                    ) {
                        if (VaultStore.isVideo(file)) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                                Text(file.name, fontSize = 10.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 6.dp))
                            }
                        } else {
                            AsyncImage(
                                model = file,
                                imageLoader = imageLoader,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }

    val current = viewing
    if (current != null) {
        VaultViewer(file = current, imageLoader = imageLoader, onClose = { viewing = null })
    }
}

@Composable
private fun VaultViewer(file: File, imageLoader: ImageLoader, onClose: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var confirmDelete by remember { mutableStateOf(false) }

    BackHandler { onClose() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(strings.vaultDeleteTitle) },
            text = { Text(file.name) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    scope.launch {
                        withContext(Dispatchers.IO) { VaultStore.delete(context, file) }
                        onClose()
                    }
                }) { Text(strings.cdDelete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text(strings.cancel) } }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (VaultStore.isVideo(file)) {
            val player = remember(file) {
                ExoPlayer.Builder(context).build().apply {
                    setMediaItem(MediaItem.fromUri(Uri.fromFile(file)))
                    repeatMode = Player.REPEAT_MODE_ALL
                    prepare()
                    playWhenReady = true
                }
            }
            DisposableEffect(player) { onDispose { player.release() } }
            VideoPlayer(exoPlayer = player)
        } else {
            ZoomableImage(imageUrl = file.absolutePath, imageLoader = imageLoader, cfClearanceCookie = "", onClose = onClose)
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .systemBarsPadding()
                .padding(16.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(50))
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(onClick = {
                scope.launch {
                    val ok = withContext(Dispatchers.IO) {
                        try {
                            VaultStore.exportToGallery(context, file)
                            true
                        } catch (e: Exception) {
                            false
                        }
                    }
                    Toast.makeText(context, if (ok) strings.vaultExported else strings.vaultExportFailed, Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(Icons.Default.PhotoLibrary, contentDescription = strings.vaultExport, tint = Color.White)
            }
            IconButton(onClick = { confirmDelete = true }) {
                Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = Color.White)
            }
            if (VaultStore.isVideo(file)) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = Color.White)
                }
            }
        }
    }
}
