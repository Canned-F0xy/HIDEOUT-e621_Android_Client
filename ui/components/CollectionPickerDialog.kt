package com.CannedF0xy.hideout.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.CollectionStore
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.screens.CreateCollectionDialog
import kotlinx.coroutines.launch

@Composable
fun CollectionPickerDialog(post: Post, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val collections by CollectionStore.collections.collectAsState()
    var showCreate by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { CollectionStore.ensureLoaded(context) }

    if (showCreate) {
        CreateCollectionDialog(
            onCreate = { name ->
                showCreate = false
                scope.launch {
                    val created = CollectionStore.create(context, name)
                    CollectionStore.togglePost(context, created.id, post)
                }
            },
            onDismiss = { showCreate = false }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(strings.collectionPickerTitle) },
        text = {
            Column {
                if (collections.isEmpty()) {
                    Text(strings.collectionsEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 320.dp)) {
                        items(collections, key = { it.id }) { collection ->
                            val checked = collection.posts.any { it.id == post.id }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { scope.launch { CollectionStore.togglePost(context, collection.id, post) } }
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = { scope.launch { CollectionStore.togglePost(context, collection.id, post) } },
                                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                                )
                                Text(collection.name, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(strings.done, color = MaterialTheme.colorScheme.primary) } },
        dismissButton = { TextButton(onClick = { showCreate = true }) { Text(strings.collectionCreate) } }
    )
}
