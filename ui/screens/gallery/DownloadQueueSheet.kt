package com.CannedF0xy.hideout.ui.screens.gallery

import com.CannedF0xy.hideout.util.DataSaver
import com.CannedF0xy.hideout.download.FileNameFormatter
import android.widget.Toast
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
import com.CannedF0xy.hideout.download.BatchDownloader
import com.CannedF0xy.hideout.download.DownloadQueue
import com.CannedF0xy.hideout.download.DownloadStatus
import com.CannedF0xy.hideout.download.DownloadTask
import com.CannedF0xy.hideout.i18n.AppStrings
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.SearchQueryBuilder
import kotlinx.coroutines.launch

private val BATCH_SIZES = listOf(50, 100, 200, 320)

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadQueueSheet(
    currentAppliedTag: String,
    isNsfwEnabled: Boolean,
    blacklistedTags: List<String>,
    downloadTreeUri: String,
    onDismiss: () -> Unit,
    fileNameTemplate: String = FileNameFormatter.DEFAULT_TEMPLATE,
    isDataSaverEnabled: Boolean = false
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val tasks by DownloadQueue.tasks.collectAsState()
    var batchSize by remember { mutableIntStateOf(BATCH_SIZES.first()) }
    var isCollecting by remember { mutableStateOf(false) }

    val searchLabel = when {
        currentAppliedTag == SearchQueryBuilder.HOT_QUERY -> strings.weeklyHot
        currentAppliedTag.isBlank() -> strings.latestPosts
        else -> currentAppliedTag
    }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(strings.downloadQueueTitle, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(strings.batchDownloadTitle, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text(searchLabel, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BATCH_SIZES.forEach { size ->
                            val selected = size == batchSize
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                                        RoundedCornerShape(50)
                                    )
                                    .clickable { batchSize = size }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    "$size",
                                    fontSize = 13.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            if (DataSaver.shouldSave(context, isDataSaverEnabled)) {
                                Toast.makeText(context, strings.dataSaverBatchBlocked, Toast.LENGTH_LONG).show()
                            } else if (!isCollecting) {
                                isCollecting = true
                                scope.launch {
                                    try {
                                        val posts = BatchDownloader.collectSearchPosts(currentAppliedTag, isNsfwEnabled, blacklistedTags, batchSize)
                                        val requests = BatchDownloader.toRequests(posts, fileNameTemplate)
                                        if (requests.isEmpty()) {
                                            Toast.makeText(context, strings.batchNothing, Toast.LENGTH_SHORT).show()
                                        } else {
                                            DownloadQueue.enqueueAll(context, requests, downloadTreeUri, strings, silent = true)
                                            Toast.makeText(context, strings.batchQueued(requests.size), Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, strings.loadFailed, Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isCollecting = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isCollecting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        } else {
                            Text(strings.batchDownloadButton(batchSize), color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(strings.downloadQueueListTitle(tasks.size), fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.weight(1f))
                TextButton(onClick = { DownloadQueue.clearFinished() }) { Text(strings.clearFinished, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary) }
                TextButton(onClick = { DownloadQueue.cancelAll() }) { Text(strings.cancelAll, fontSize = 12.sp, color = Color.Red) }
            }

            if (tasks.isEmpty()) {
                Text(strings.downloadQueueEmpty, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                    items(tasks.asReversed(), key = { it.id }) { task ->
                        DownloadTaskRow(task, strings)
                    }
                }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}

@Composable
private fun DownloadTaskRow(task: DownloadTask, strings: AppStrings) {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(task.request.fileName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onBackground, maxLines = 1, overflow = TextOverflow.Ellipsis)
                val statusText = when (task.status) {
                    DownloadStatus.QUEUED -> strings.downloadStatusQueued
                    DownloadStatus.RUNNING -> strings.downloadStatusRunning((task.progress * 100).toInt())
                    DownloadStatus.DONE -> strings.downloadStatusDone
                    DownloadStatus.FAILED -> strings.downloadStatusFailed(task.error ?: "")
                    DownloadStatus.CANCELED -> strings.downloadStatusCanceled
                }
                val statusColor = when (task.status) {
                    DownloadStatus.DONE -> MaterialTheme.colorScheme.primary
                    DownloadStatus.FAILED -> Color.Red
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Text(statusText, fontSize = 11.sp, color = statusColor, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            when (task.status) {
                DownloadStatus.QUEUED, DownloadStatus.RUNNING -> IconButton(onClick = { DownloadQueue.cancel(task.id) }) {
                    Icon(Icons.Default.Close, contentDescription = strings.cancel, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DownloadStatus.FAILED, DownloadStatus.CANCELED -> IconButton(onClick = { DownloadQueue.retry(task.id) }) {
                    Icon(Icons.Default.Refresh, contentDescription = strings.retry, tint = MaterialTheme.colorScheme.primary)
                }
                DownloadStatus.DONE -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(12.dp))
            }
        }
        if (task.status == DownloadStatus.RUNNING) {
            LinearProgressIndicator(
                progress = { task.progress },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
