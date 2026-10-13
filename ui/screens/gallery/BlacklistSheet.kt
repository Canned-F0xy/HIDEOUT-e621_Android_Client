package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.AccountBlacklistSync
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlacklistSheet(
    blacklistedTags: List<String>,
    onBlacklistSave: (List<String>) -> Unit,
    onDismiss: () -> Unit,
    loginUsername: String = ""
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var newTag by remember { mutableStateOf("") }
    var currentList by remember { mutableStateOf(blacklistedTags) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(strings.blacklistManagement, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.blacklistInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (loginUsername.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = {
                        if (!isSyncing) {
                            isSyncing = true
                            scope.launch {
                                val result = AccountBlacklistSync.fetch(loginUsername)
                                isSyncing = false
                                if (result == null) {
                                    Toast.makeText(context, strings.blacklistSyncFailed, Toast.LENGTH_SHORT).show()
                                } else {
                                    val merged = (currentList + result.importedTags).distinct()
                                    val added = merged.size - currentList.size
                                    currentList = merged
                                    Toast.makeText(context, strings.blacklistSyncResult(added, result.skippedLines), Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.blacklistSyncButton, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = newTag, onValueChange = { newTag = it }, modifier = Modifier.weight(1f), label = { Text(strings.tagInputHint) }, singleLine = true)
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(onClick = {
                    if (newTag.isNotBlank() && !currentList.contains(newTag.trim())) { currentList = currentList + newTag.trim(); newTag = "" }
                }, modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha=0.15f), RoundedCornerShape(12.dp))) {
                    Icon(Icons.Default.Add, contentDescription = strings.cdAdd, tint = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                items(currentList) { tag ->
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text(tag, color = MaterialTheme.colorScheme.onBackground)
                        IconButton(onClick = { currentList = currentList - tag }) { Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = Color.Red) }
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { onDismiss() }, modifier = Modifier.padding(end = 8.dp)) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Button(onClick = { onBlacklistSave(currentList); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text(strings.save, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}
