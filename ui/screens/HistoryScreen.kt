package com.CannedF0xy.hideout.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import com.CannedF0xy.hideout.data.ViewHistoryStore
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.OverlayScaffold
import com.CannedF0xy.hideout.ui.components.PostThumbGrid
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    imageLoader: ImageLoader,
    cfClearanceCookie: String,
    onOpenPost: (Post) -> Unit,
    onClose: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val posts by ViewHistoryStore.posts.collectAsState()
    var confirmClear by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { ViewHistoryStore.ensureLoaded(context) }

    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text(strings.historyClearTitle) },
            text = { Text(strings.historyClearMessage) },
            confirmButton = {
                TextButton(onClick = {
                    confirmClear = false
                    scope.launch { ViewHistoryStore.clear(context) }
                }) { Text(strings.clearAll, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmClear = false }) { Text(strings.cancel) }
            }
        )
    }

    OverlayScaffold(
        title = strings.historyTitle,
        onBack = onClose,
        actions = {
            if (posts.isNotEmpty()) {
                IconButton(onClick = { confirmClear = true }) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = strings.clearAll, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    ) {
        if (posts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.historyEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            PostThumbGrid(
                posts = posts,
                imageLoader = imageLoader,
                cfClearanceCookie = cfClearanceCookie,
                onPostClick = onOpenPost,
                onRemove = { post -> scope.launch { ViewHistoryStore.remove(context, post.id) } },
                removeDescription = strings.cdDelete
            )
        }
    }
}
