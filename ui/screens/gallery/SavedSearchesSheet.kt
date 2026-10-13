package com.CannedF0xy.hideout.ui.screens.gallery

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.SearchQueryBuilder
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedSearchesSheet(
    appPrefs: AppPreferences,
    currentAppliedTag: String,
    onOpenSearch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val savedSearches by appPrefs.savedSearches.collectAsState(initial = emptyList())
    val canSaveCurrent = currentAppliedTag.isNotBlank() && currentAppliedTag != SearchQueryBuilder.HOT_QUERY
    var nameInput by remember(currentAppliedTag) { mutableStateOf(if (canSaveCurrent) currentAppliedTag.trim() else "") }
    var queryInput by remember(currentAppliedTag) { mutableStateOf(if (canSaveCurrent) currentAppliedTag.trim() else "") }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(strings.savedSearchesTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.savedSearchesInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = queryInput,
                onValueChange = { queryInput = it },
                label = { Text(strings.savedSearchQueryHint) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text(strings.savedSearchNameHint) },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = {
                        if (queryInput.isNotBlank()) {
                            val query = queryInput
                            val name = nameInput
                            scope.launch {
                                appPrefs.addSavedSearch(name, query)
                                Toast.makeText(context, strings.savedSearchAdded, Toast.LENGTH_SHORT).show()
                            }
                            queryInput = ""
                            nameInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) { Text(strings.save, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (savedSearches.isEmpty()) {
                Text(strings.savedSearchesEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    itemsIndexed(savedSearches, key = { _, item -> item.query }) { index, item ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenSearch(item.query) }.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.PushPin, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                                Text(item.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                if (item.name != item.query) {
                                    Text(item.query, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                            IconButton(
                                onClick = { scope.launch { appPrefs.moveSavedSearch(index, index - 1) } },
                                enabled = index > 0
                            ) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = strings.cdMoveUp) }
                            IconButton(
                                onClick = { scope.launch { appPrefs.moveSavedSearch(index, index + 1) } },
                                enabled = index < savedSearches.size - 1
                            ) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = strings.cdMoveDown) }
                            IconButton(onClick = { scope.launch { appPrefs.removeSavedSearch(item.query) } }) {
                                Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = Color.Red)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}
