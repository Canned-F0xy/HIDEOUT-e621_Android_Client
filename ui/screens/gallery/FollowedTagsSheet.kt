package com.CannedF0xy.hideout.ui.screens.gallery

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.follow.FollowChecker
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.SearchQueryBuilder
import com.CannedF0xy.hideout.notification.NotificationHelper
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FollowedTagsSheet(
    appPrefs: AppPreferences,
    currentAppliedTag: String,
    isNsfwEnabled: Boolean,
    blacklistedTags: List<String>,
    onOpenTag: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val followed by appPrefs.followedTags.collectAsState(initial = emptyMap())
    var newTag by remember { mutableStateOf("") }
    var isChecking by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }

    fun follow(rawTag: String) {
        val tag = rawTag.trim()
        if (tag.isEmpty() || followed.containsKey(tag)) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !NotificationHelper.canNotify(context)) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        scope.launch {
            val newestId = FollowChecker.fetchNewestId(tag, isNsfwEnabled, blacklistedTags) ?: 0
            appPrefs.setFollowedTag(tag, newestId)
            Toast.makeText(context, strings.followAdded(tag), Toast.LENGTH_SHORT).show()
        }
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(strings.followedTagsTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.followedTagsInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = newTag,
                    onValueChange = { newTag = it },
                    modifier = Modifier.weight(1f),
                    label = { Text(strings.tagInputHint) },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { follow(newTag); newTag = "" },
                    modifier = Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.Default.Add, contentDescription = strings.cdAdd, tint = MaterialTheme.colorScheme.primary)
                }
            }

            val canFollowCurrent = currentAppliedTag.isNotBlank() &&
                currentAppliedTag != SearchQueryBuilder.HOT_QUERY &&
                !followed.containsKey(currentAppliedTag.trim())
            if (canFollowCurrent) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { follow(currentAppliedTag) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(strings.followCurrentSearch(currentAppliedTag.trim()), color = MaterialTheme.colorScheme.primary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (followed.isEmpty()) {
                Text(strings.followedTagsEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 260.dp)) {
                    items(followed.keys.sorted()) { tag ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable { onOpenTag(tag) }.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            Text(
                                tag,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f).padding(start = 12.dp)
                            )
                            IconButton(onClick = { scope.launch { appPrefs.removeFollowedTag(tag) } }) {
                                Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = Color.Red)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        if (!isChecking) {
                            isChecking = true
                            scope.launch {
                                val count = try { FollowChecker.check(context) } catch (e: Exception) { 0 }
                                isChecking = false
                                Toast.makeText(context, if (count > 0) strings.followCheckFound(count) else strings.followCheckNone, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (isChecking) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(strings.followCheckNow, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}
