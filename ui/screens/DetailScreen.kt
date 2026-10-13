package com.CannedF0xy.hideout.ui.screens

import com.CannedF0xy.hideout.util.ImageSource
import com.CannedF0xy.hideout.util.MediaUrlResolver
import com.CannedF0xy.hideout.util.MediaQuality
import com.CannedF0xy.hideout.download.FileNameFormatter
import com.CannedF0xy.hideout.util.DataSaver
import com.CannedF0xy.hideout.ui.components.TagInfoDialog
import com.CannedF0xy.hideout.ui.components.CollectionPickerDialog
import com.CannedF0xy.hideout.network.SimilarImageSearch
import com.CannedF0xy.hideout.data.ViewHistoryStore
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.CannedF0xy.hideout.R
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.data.LocalPostManager
import com.CannedF0xy.hideout.download.downloadMediaWithRange
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.VoteRepository
import com.CannedF0xy.hideout.network.FavoriteRepository
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.CommentsSection
import com.CannedF0xy.hideout.ui.components.TagSection
import com.CannedF0xy.hideout.ui.components.VideoPlayer
import com.CannedF0xy.hideout.ui.components.ZoomableImage
import com.CannedF0xy.hideout.ui.theme.NeonOrange
import com.CannedF0xy.hideout.util.getCachedFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    post: Post,
    isActivePage: Boolean,
    appPrefs: AppPreferences,
    isDarkTheme: Boolean,
    isAutoPlay: Boolean,
    gifEnabledLoader: ImageLoader,
    cfClearanceCookie: String,
    onClose: () -> Unit,
    onTagClick: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    var isFullScreen by remember { mutableStateOf(false) }

    val savedTreeUri by appPrefs.downloadTreeUri.collectAsState(initial = "")
    val imageQuality by appPrefs.imageQuality.collectAsState(initial = MediaQuality.DEFAULT)
    val videoQuality by appPrefs.videoQuality.collectAsState(initial = MediaQuality.DEFAULT)
    var isVoting by remember { mutableStateOf(false) }
    var isSearchingSimilar by remember { mutableStateOf(false) }
    var showCollectionPicker by remember { mutableStateOf(false) }
    var tagInfoTarget by remember { mutableStateOf<String?>(null) }
    val isDataSaverEnabled by appPrefs.isDataSaverEnabled.collectAsState(initial = false)
    val fileNameTemplate by appPrefs.fileNameTemplate.collectAsState(initial = FileNameFormatter.DEFAULT_TEMPLATE)
    val isSavingData = DataSaver.shouldSave(context, isDataSaverEnabled)

    LaunchedEffect(isActivePage) {
        if (isActivePage) ViewHistoryStore.record(context, post)
    }

    if (showCollectionPicker) {
        CollectionPickerDialog(post = post, onDismiss = { showCollectionPicker = false })
    }

    val activeTagInfo = tagInfoTarget
    if (activeTagInfo != null) {
        TagInfoDialog(
            tag = activeTagInfo,
            appPrefs = appPrefs,
            onSearch = { tag ->
                tagInfoTarget = null
                onTagClick(tag)
            },
            onDismiss = { tagInfoTarget = null }
        )
    }

    fun submitVote(score: Int) {
        if (isVoting) return
        if (NetworkModule.username.isBlank() || NetworkModule.apiKey.isBlank()) {
            Toast.makeText(context, strings.loginFirst, Toast.LENGTH_SHORT).show()
            return
        }
        val currentVote = LocalPostManager.getVote(post)
        val isCancelling = if (score > 0) currentVote > 0 else currentVote < 0
        isVoting = true
        scope.launch {
            var result = VoteRepository.vote(post.id, score, noUnvote = !isCancelling)
            if (isCancelling && result != null && (result.our_score ?: 0) == score) {
                result = VoteRepository.vote(post.id, score, noUnvote = false)
            }
            if (result == null) {
                isVoting = false
                Toast.makeText(context, strings.voteFailed, Toast.LENGTH_SHORT).show()
                return@launch
            }
            LocalPostManager.applyVoteResult(post, result.score, result.our_score)
            val shouldFavorite = (result.our_score ?: 0) > 0
            if (shouldFavorite != LocalPostManager.isFavorited(post)) {
                if (FavoriteRepository.setFavorite(post.id, shouldFavorite)) {
                    LocalPostManager.markFavorite(post, shouldFavorite)
                    Toast.makeText(context, if (shouldFavorite) strings.favAdded else strings.favRemoved, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, strings.favAddFailedLogin, Toast.LENGTH_SHORT).show()
                }
            }
            isVoting = false
        }
    }

    val ext = post.file?.ext ?: ""
    val md5 = post.file?.md5 ?: ""
    val cachedFile = getCachedFile(context, md5, ext)

    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    LaunchedEffect(isActivePage) {
        if (isActivePage && (ext == "webm" || ext == "mp4")) {
            val effectiveVideoQuality = MediaUrlResolver.capForDataSaver(videoQuality, MediaQuality.LOW, DataSaver.shouldSave(context, isDataSaverEnabled))
            val mediaUrl = MediaUrlResolver.videoUrl(post, effectiveVideoQuality) ?: post.file?.url
            if (mediaUrl != null) {
                val dataSourceFactory = DefaultHttpDataSource.Factory()
                    .setUserAgent(NetworkModule.DEFAULT_USER_AGENT)
                    .setDefaultRequestProperties(
                        if (cfClearanceCookie.isNotBlank()) mapOf("Cookie" to cfClearanceCookie) else emptyMap()
                    )

                val player = ExoPlayer.Builder(context)
                    .setMediaSourceFactory(DefaultMediaSourceFactory(context).setDataSourceFactory(dataSourceFactory))
                    .build().apply {
                        val mediaUri = if (cachedFile != null && effectiveVideoQuality == MediaQuality.ORIGINAL) Uri.fromFile(cachedFile) else Uri.parse(mediaUrl)
                        setMediaItem(MediaItem.fromUri(mediaUri))
                        prepare()
                        playWhenReady = isAutoPlay && !DataSaver.shouldSave(context, isDataSaverEnabled)
                        repeatMode = Player.REPEAT_MODE_ALL
                    }
                exoPlayer = player
            }
        } else {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    DisposableEffect(post.id) {
        onDispose {
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    val scrollState = rememberScrollState()
    var dragOffsetY by remember { mutableFloatStateOf(0f) }
    val animatedOffsetY = remember { Animatable(0f) }
    var canPullDown by remember { mutableStateOf(false) }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (dragOffsetY > 0f && available.y < 0f) {
                    dragOffsetY += available.y
                    if (dragOffsetY < 0f) dragOffsetY = 0f
                    scope.launch { animatedOffsetY.snapTo(dragOffsetY) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (source == NestedScrollSource.Drag && available.y > 0f) {
                    if (canPullDown && scrollState.value == 0) {
                        dragOffsetY += available.y * 0.4f
                        scope.launch { animatedOffsetY.snapTo(dragOffsetY) }
                        return Offset(0f, available.y)
                    }
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (dragOffsetY > 150f) {
                    onClose()
                    return Velocity.Zero
                } else if (dragOffsetY > 0f) {
                    scope.launch { animatedOffsetY.animateTo(0f, tween(200)) }
                    dragOffsetY = 0f
                }
                return Velocity.Zero
            }
        }
    }

    if (isFullScreen && post.file?.url != null && ext != "swf") {
        Dialog(onDismissRequest = { isFullScreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            if (ext == "webm" || ext == "mp4") {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (exoPlayer != null) {
                        VideoPlayer(exoPlayer = exoPlayer!!)
                    }
                    IconButton(onClick = { isFullScreen = false }, modifier = Modifier.align(Alignment.TopEnd).systemBarsPadding().padding(16.dp)) {
                        Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = Color.White)
                    }
                }
            } else {
                val fullUrl = post.file.url
                val zoomUrl = if (cachedFile != null) cachedFile.absolutePath else fullUrl
                ZoomableImage(
                    imageUrl = zoomUrl,
                    imageLoader = gifEnabledLoader,
                    cfClearanceCookie = cfClearanceCookie,
                    onClose = { isFullScreen = false }
                )
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .offset(y = animatedOffsetY.value.dp)
            .graphicsLayer {
                alpha = (1f - (kotlin.math.abs(animatedOffsetY.value) / 400f)).coerceIn(0.4f, 1f)
            }
            .nestedScroll(nestedScrollConnection)
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        if (event.changes.any { it.pressed }) {
                            canPullDown = (scrollState.value == 0)
                        }
                    }
                }
            },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
                Box(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp).background(Color.Black), contentAlignment = Alignment.Center) {
                    if (ext == "swf") {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_launcher_warning),
                                contentDescription = strings.unsupportedSwf,
                                modifier = Modifier.size(120.dp).padding(bottom = 16.dp),
                                contentScale = ContentScale.Fit
                            )
                            Text(
                                text = strings.unsupportedSwf,
                                color = NeonOrange,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    shadow = androidx.compose.ui.graphics.Shadow(
                                        color = NeonOrange.copy(alpha = 0.6f),
                                        offset = Offset(0f, 0f),
                                        blurRadius = 16f
                                    )
                                ),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else if (ext == "webm" || ext == "mp4") {
                        if (post.file?.url != null) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                if (!isFullScreen && exoPlayer != null) {
                                    VideoPlayer(exoPlayer = exoPlayer!!)
                                }
                                IconButton(onClick = { isFullScreen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                                    Icon(Icons.Default.Fullscreen, contentDescription = strings.cdFullscreen, tint = Color.White, modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                    } else if (post.file?.url != null || post.sample?.url != null || post.preview?.url != null) {
                        val effectiveImageQuality = MediaUrlResolver.capForDataSaver(imageQuality, MediaQuality.MEDIUM, isSavingData)
                        val imageSource = if (ext == "swf") {
                            (post.sample?.url ?: post.preview?.url)?.let { ImageSource(it, null) }
                        } else {
                            MediaUrlResolver.imageSource(post, effectiveImageQuality)
                        }
                        val displayUrl = imageSource?.url

                        if (displayUrl != null) {
                            val imageDataSource = if (ext == "swf" || effectiveImageQuality != MediaQuality.ORIGINAL) displayUrl else (cachedFile ?: displayUrl)
                            val maxDimension = imageSource?.maxDimensionPx
                            val detailImageRequest = remember(imageDataSource, cfClearanceCookie, maxDimension) {
                                ImageRequest.Builder(context)
                                    .data(imageDataSource)
                                    .apply { if (maxDimension != null) size(maxDimension) }
                                    .addHeader("User-Agent", NetworkModule.DEFAULT_USER_AGENT)
                                    .apply {
                                        if (cfClearanceCookie.isNotBlank()) addHeader("Cookie", cfClearanceCookie)
                                    }
                                    .crossfade(200)
                                    .build()
                            }
                            AsyncImage(
                                model = detailImageRequest,
                                imageLoader = gifEnabledLoader,
                                contentDescription = "Full Image",
                                modifier = Modifier.fillMaxSize().pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { isFullScreen = true }
                                    )
                                },
                                contentScale = ContentScale.Fit
                            )
                        } else {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                        }
                    } else {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(64.dp))
                    }

                    Row(
                        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth().systemBarsPadding().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.background(Color.Black.copy(alpha=0.5f), RoundedCornerShape(50))
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = strings.cdBack, tint = Color.White)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {
                                    if (!isSearchingSimilar) {
                                        isSearchingSimilar = true
                                        scope.launch {
                                            val query = SimilarImageSearch.findSimilarQuery(post)
                                            isSearchingSimilar = false
                                            if (query != null) onTagClick(query)
                                            else Toast.makeText(context, strings.similarNotFound, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.background(Color.Black.copy(alpha=0.5f), RoundedCornerShape(50))
                            ) {
                                if (isSearchingSimilar) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                                } else {
                                    Icon(Icons.Default.ImageSearch, contentDescription = strings.cdSimilarSearch, tint = Color.White)
                                }
                            }
                            IconButton(
                                onClick = { showCollectionPicker = true },
                                modifier = Modifier.background(Color.Black.copy(alpha=0.5f), RoundedCornerShape(50))
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = strings.collectionPickerTitle, tint = Color.White)
                            }
                            if (post.file?.url != null) {
                                IconButton(
                                    onClick = {
                                        downloadMediaWithRange(
                                            context = context,
                                            url = post.file.url,
                                            fileName = FileNameFormatter.format(fileNameTemplate, post, ext),
                                            md5 = md5,
                                            ext = ext,
                                            treeUriStr = savedTreeUri,
                                            cfClearanceCookie = cfClearanceCookie,
                                            strings = strings
                                        )
                                    },
                                    modifier = Modifier.background(Color.Black.copy(alpha=0.5f), RoundedCornerShape(50))
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = strings.cdDownload, tint = Color.White)
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Card(
                            modifier = Modifier.weight(1f).height(76.dp),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(modifier = Modifier.fillMaxSize().padding(start = 20.dp, end = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                                    Text(strings.fileLabel(ext.uppercase()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(strings.scoreLabel(LocalPostManager.getScore(post)), fontSize = 20.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
                                }
                                val myVote = LocalPostManager.getVote(post)
                                IconButton(onClick = { submitVote(1) }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        Icons.Default.ThumbUp,
                                        contentDescription = strings.cdVoteUp,
                                        tint = if (myVote > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(onClick = { submitVote(-1) }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        Icons.Default.ThumbDown,
                                        contentDescription = strings.cdVoteDown,
                                        tint = if (myVote < 0) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    val hasPool = !post.pools.isNullOrEmpty()
                    val relatedQuery = post.getAllRelatedIdsQuery()
                    if (hasPool || relatedQuery != null) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            if (hasPool) {
                                Card(
                                    modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(20.dp)).clickable { onTagClick("pool:${post.pools!!.first()}") },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.List, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(strings.viewPool(post.pools!!.first()), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                            if (relatedQuery != null) {
                                Card(
                                    modifier = Modifier.weight(1f).height(64.dp).clip(RoundedCornerShape(20.dp)).clickable { onTagClick(relatedQuery) },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    Row(modifier = Modifier.fillMaxSize(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.width(8.dp))
                                        Text(strings.viewAllRelated, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                    }

                    if (!post.description.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(strings.descLabel, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 8.dp))
                                Text(post.description, color = MaterialTheme.colorScheme.onBackground, fontSize = 14.sp)
                            }
                        }
                    }

                    TagSection(title = strings.artistLabel, tags = post.tags?.artist ?: emptyList(), color = Color(0xFFFF5252), onTagClick = onTagClick, onTagLongClick = { tag -> tagInfoTarget = tag })
                    TagSection(title = strings.copyrightLabel, tags = post.tags?.copyright ?: emptyList(), color = Color(0xFFE040FB), onTagClick = onTagClick, onTagLongClick = { tag -> tagInfoTarget = tag })
                    TagSection(title = strings.characterLabel, tags = post.tags?.character ?: emptyList(), color = Color(0xFF69F0AE), onTagClick = onTagClick, onTagLongClick = { tag -> tagInfoTarget = tag })
                    TagSection(title = strings.generalLabel, tags = post.tags?.general ?: emptyList(), color = Color(0xFF40C4FF), onTagClick = onTagClick, onTagLongClick = { tag -> tagInfoTarget = tag })

                    if (!post.sources.isNullOrEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(strings.sourcesLabel, fontWeight = FontWeight.Bold, color = if (isDarkTheme) Color.White else Color.Black, modifier = Modifier.padding(bottom = 8.dp))
                                post.sources.forEach { sourceUrl ->
                                    Text(
                                        text = sourceUrl,
                                        color = Color(0xFF40C4FF),
                                        fontSize = 13.sp,
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                            try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))) } catch(e:Exception){}
                                        }
                                    )
                                }
                            }
                        }
                    }

                    CommentsSection(postId = post.id)

                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}
