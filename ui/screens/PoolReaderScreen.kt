package com.CannedF0xy.hideout.ui.screens

import com.CannedF0xy.hideout.util.DataSaver
import com.CannedF0xy.hideout.download.FileNameFormatter
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import com.CannedF0xy.hideout.download.BatchDownloader
import com.CannedF0xy.hideout.download.DownloadQueue
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.PoolContent
import com.CannedF0xy.hideout.network.PoolRepository
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.imageRequest

@Composable
fun PoolReaderScreen(
    poolId: Int,
    isNsfwEnabled: Boolean,
    blacklistedTags: List<String>,
    imageLoader: ImageLoader,
    cfClearanceCookie: String,
    downloadTreeUri: String,
    onClose: () -> Unit,
    fileNameTemplate: String = FileNameFormatter.DEFAULT_TEMPLATE,
    isDataSaverEnabled: Boolean = false
) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    var content by remember(poolId) { mutableStateOf<PoolContent?>(null) }
    var isError by remember(poolId) { mutableStateOf(false) }
    var reloadKey by remember { mutableStateOf(0) }
    var showBars by remember { mutableStateOf(true) }
    val listState = rememberLazyListState()

    LaunchedEffect(poolId, reloadKey) {
        isError = false
        content = null
        try {
            content = PoolRepository.load(poolId, isNsfwEnabled, blacklistedTags)
        } catch (e: Exception) {
            isError = true
        }
    }

    BackHandler { onClose() }

    val currentPage by remember { derivedStateOf { listState.firstVisibleItemIndex + 1 } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { showBars = !showBars }
    ) {
        val loaded = content
        when {
            isError -> {
                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings.loadFailed, color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { reloadKey++ },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) { Text(strings.retry, color = MaterialTheme.colorScheme.onPrimary) }
                }
            }
            loaded == null -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
            }
            loaded.posts.isEmpty() -> {
                Text(strings.poolEmpty, color = Color.White, modifier = Modifier.align(Alignment.Center))
            }
            else -> {
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(loaded.posts, key = { _, post -> post.id }) { _, post ->
                        PoolPage(post, imageLoader, cfClearanceCookie, strings.videoPageLabel)
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showBars,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.7f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.ArrowBack, contentDescription = strings.cdBack, tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        loaded?.pool?.displayName() ?: "Pool #$poolId",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (loaded != null && loaded.posts.isNotEmpty()) {
                        Text(strings.poolPageIndicator(currentPage, loaded.posts.size), color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                }
                if (loaded != null && loaded.posts.isNotEmpty()) {
                    IconButton(onClick = {
                        if (DataSaver.shouldSave(context, isDataSaverEnabled)) {
                            Toast.makeText(context, strings.dataSaverBatchBlocked, Toast.LENGTH_LONG).show()
                        } else {
                            val requests = BatchDownloader.toRequests(loaded.posts, fileNameTemplate, poolId)
                            DownloadQueue.enqueueAll(context, requests, downloadTreeUri, strings, silent = true)
                            Toast.makeText(context, strings.batchQueued(requests.size), Toast.LENGTH_SHORT).show()
                        }
                    }) {
                        Icon(Icons.Default.Download, contentDescription = strings.poolDownloadAll, tint = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun PoolPage(post: Post, imageLoader: ImageLoader, cfClearanceCookie: String, videoLabel: String) {
    val context = LocalContext.current
    val ext = post.file?.ext ?: ""
    val isVideo = ext == "webm" || ext == "mp4" || ext == "swf"
    val url = if (isVideo) post.sample?.url ?: post.preview?.url else post.sample?.url ?: post.file?.url ?: post.preview?.url
    val width = post.file?.width ?: 0
    val height = post.file?.height ?: 0
    val ratio = if (width > 0 && height > 0) width.toFloat() / height.toFloat() else 0.7f

    Box(modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp).aspectRatio(ratio)) {
        if (url != null) {
            AsyncImage(
                model = imageRequest(context, url, cfClearanceCookie),
                imageLoader = imageLoader,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (isVideo) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(videoLabel, color = Color.White, fontSize = 13.sp)
            }
        }
    }
}
