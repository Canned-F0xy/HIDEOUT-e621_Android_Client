package com.CannedF0xy.hideout.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.data.CollectionStore
import com.CannedF0xy.hideout.data.LocalCollection
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.OverlayScaffold
import com.CannedF0xy.hideout.ui.components.PostThumbGrid
import com.CannedF0xy.hideout.ui.components.imageRequest
import kotlinx.coroutines.launch

@Composable
fun CollectionsScreen(
    imageLoader: ImageLoader,
    cfClearanceCookie: String,
    appPrefs: AppPreferences,
    isDarkTheme: Boolean,
    isAutoPlay: Boolean,
    onTagClick: (String) -> Unit,
    onClose: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val collections by CollectionStore.collections.collectAsState()
    var openedId by remember { mutableStateOf<Long?>(null) }
    var showCreate by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<LocalCollection?>(null) }
    var viewerPosts by remember { mutableStateOf<List<Post>?>(null) }
    var viewerStartIndex by remember { mutableStateOf(0) }

    val openedViewerPosts = viewerPosts
    if (openedViewerPosts != null && openedViewerPosts.isNotEmpty()) {
        ListDetailPagerScreen(
            posts = openedViewerPosts,
            initialIndex = viewerStartIndex,
            appPrefs = appPrefs,
            isDarkTheme = isDarkTheme,
            isAutoPlay = isAutoPlay,
            gifEnabledLoader = imageLoader,
            cfClearanceCookie = cfClearanceCookie,
            onClose = { viewerPosts = null },
            onTagClick = { tag ->
                viewerPosts = null
                onTagClick(tag)
            }
        )
        return
    }

    LaunchedEffect(Unit) { CollectionStore.ensureLoaded(context) }

    if (showCreate) {
        CreateCollectionDialog(
            onCreate = { name -> showCreate = false; scope.launch { CollectionStore.create(context, name) } },
            onDismiss = { showCreate = false }
        )
    }

    val deleting = pendingDelete
    if (deleting != null) {
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text(strings.collectionDeleteTitle) },
            text = { Text(strings.collectionDeleteMessage(deleting.name)) },
            confirmButton = {
                TextButton(onClick = {
                    pendingDelete = null
                    if (openedId == deleting.id) openedId = null
                    scope.launch { CollectionStore.delete(context, deleting.id) }
                }) { Text(strings.cdDelete, color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { pendingDelete = null }) { Text(strings.cancel) } }
        )
    }

    val opened = collections.firstOrNull { it.id == openedId }
    if (opened != null) {
        OverlayScaffold(
            title = opened.name,
            onBack = { openedId = null },
            actions = {
                IconButton(onClick = { pendingDelete = opened }) {
                    Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        ) {
            if (opened.posts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(strings.collectionEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                PostThumbGrid(
                    posts = opened.posts,
                    imageLoader = imageLoader,
                    cfClearanceCookie = cfClearanceCookie,
                    onPostClick = { post ->
                        val snapshot = opened.posts
                        viewerStartIndex = snapshot.indexOfFirst { it.id == post.id }.coerceAtLeast(0)
                        viewerPosts = snapshot
                    },
                    onRemove = { post -> scope.launch { CollectionStore.removePost(context, opened.id, post.id) } },
                    removeDescription = strings.cdDelete
                )
            }
        }
        return
    }

    OverlayScaffold(
        title = strings.collectionsTitle,
        onBack = onClose,
        actions = {
            IconButton(onClick = { showCreate = true }) {
                Icon(Icons.Default.Add, contentDescription = strings.collectionCreate, tint = MaterialTheme.colorScheme.primary)
            }
        }
    ) {
        if (collections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(strings.collectionsEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(12.dp), modifier = Modifier.fillMaxSize()) {
                items(collections, key = { it.id }) { collection ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { openedId = collection.id }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            val cover = collection.posts.firstOrNull()?.let { it.preview?.url ?: it.sample?.url }
                            if (cover != null) {
                                AsyncImage(
                                    model = imageRequest(context, cover, cfClearanceCookie),
                                    imageLoader = imageLoader,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(collection.name, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(strings.collectionCount(collection.posts.size), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { pendingDelete = collection }) {
                            Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCollectionDialog(onCreate: (String) -> Unit, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.collectionCreate) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                label = { Text(strings.collectionNameHint) }
            )
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onCreate(name) }) { Text(strings.save, color = MaterialTheme.colorScheme.primary) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(strings.cancel) } }
    )
}
