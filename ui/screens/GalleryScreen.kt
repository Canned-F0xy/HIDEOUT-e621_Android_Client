package com.CannedF0xy.hideout.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import com.CannedF0xy.hideout.ui.screens.gallery.SelectionActionBar
import com.CannedF0xy.hideout.ui.screens.gallery.SavedSearchesSheet
import com.CannedF0xy.hideout.ui.components.MultiCollectionPickerDialog
import com.CannedF0xy.hideout.util.DataSaver
import com.CannedF0xy.hideout.download.DownloadQueue
import com.CannedF0xy.hideout.download.BatchDownloader
import com.CannedF0xy.hideout.download.FileNameFormatter
import com.CannedF0xy.hideout.ui.navigation.OverlayKeys
import com.CannedF0xy.hideout.ui.screens.gallery.SearchAssistPanel
import com.CannedF0xy.hideout.ui.screens.gallery.DownloadQueueSheet
import com.CannedF0xy.hideout.ui.screens.gallery.FollowedTagsSheet
import com.CannedF0xy.hideout.network.PoolRepository
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import coil.ImageLoader
import coil.compose.AsyncImage
import com.CannedF0xy.hideout.R
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.data.LocalPostManager
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.model.AutocompleteTag
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.imageRequest
import com.CannedF0xy.hideout.ui.components.shimmerEffect
import com.CannedF0xy.hideout.ui.screens.gallery.BlacklistSheet
import com.CannedF0xy.hideout.ui.screens.gallery.GalleryDrawerContent
import com.CannedF0xy.hideout.ui.screens.gallery.LoginSheet
import com.CannedF0xy.hideout.ui.screens.gallery.SettingsSheet
import com.CannedF0xy.hideout.util.formatDuration
import com.CannedF0xy.hideout.util.getCachedFile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@kotlin.OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    lazyPagingItems: LazyPagingItems<Post>,
    searchInput: TextFieldValue, currentAppliedTag: String,
    gridState: LazyStaggeredGridState,
    gifEnabledLoader: ImageLoader,
    cfClearanceCookie: String,
    onSearchInputChange: (TextFieldValue) -> Unit, onSearch: () -> Unit,
    isVpnActive: Boolean, onVpnActionClick: () -> Unit, onPostClick: (Int) -> Unit,
    isDarkTheme: Boolean, onThemeToggle: () -> Unit,
    isNsfwEnabled: Boolean, onNsfwToggle: () -> Unit,
    isAutoPlay: Boolean, onAutoPlayToggle: () -> Unit,
    loginUsername: String, loginApiKey: String, onLoginSave: (String, String) -> Unit,
    onFavoritesClick: () -> Unit, onHotPostsClick: () -> Unit,
    blacklistedTags: List<String>, onBlacklistSave: (List<String>) -> Unit,
    appPrefs: AppPreferences,
    appLanguage: String, onLanguageChange: (String) -> Unit,
    onOpenPoolReader: (Int) -> Unit = {},
    onOpenOverlay: (String) -> Unit = {},
    layoutWidthDp: Int? = null
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    val screenWidth = layoutWidthDp ?: LocalConfiguration.current.screenWidthDp
    val initialColumns = when {
        screenWidth >= 840 -> 4
        screenWidth >= 600 -> 3
        else -> 2
    }
    var gridColumns by remember(initialColumns) { mutableIntStateOf(initialColumns) }
    var currentZoom by remember { mutableFloatStateOf(1f) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var isSearchFocused by remember { mutableStateOf(false) }
    var expanded by remember { mutableStateOf(false) }
    var tagSuggestions by remember { mutableStateOf<List<AutocompleteTag>>(emptyList()) }

    var showLoginDialog by remember { mutableStateOf(false) }
    var showBlacklistDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showFollowSheet by remember { mutableStateOf(false) }
    var showDownloadSheet by remember { mutableStateOf(false) }
    val searchHistory by appPrefs.searchHistory.collectAsState(initial = emptyList())
    val isIncognito by appPrefs.isIncognito.collectAsState(initial = false)
    val savedSearches by appPrefs.savedSearches.collectAsState(initial = emptyList())
    val fileNameTemplate by appPrefs.fileNameTemplate.collectAsState(initial = FileNameFormatter.DEFAULT_TEMPLATE)
    val isDataSaverEnabled by appPrefs.isDataSaverEnabled.collectAsState(initial = false)
    var showSavedSearchesSheet by remember { mutableStateOf(false) }
    var selectedPosts by remember { mutableStateOf<Map<Int, Post>>(emptyMap()) }
    var showMultiCollectionPicker by remember { mutableStateOf(false) }
    val isSelectionMode = selectedPosts.isNotEmpty()

    fun toggleSelection(post: Post) {
        selectedPosts = if (selectedPosts.containsKey(post.id)) selectedPosts - post.id else selectedPosts + (post.id to post)
    }

    fun openSearchQuery(query: String) {
        onSearchInputChange(TextFieldValue(query, TextRange(query.length)))
        expanded = false
        onSearch()
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    LaunchedEffect(currentAppliedTag) { selectedPosts = emptyMap() }

    val savedTreeUri by appPrefs.downloadTreeUri.collectAsState(initial = "")
    var isPillVisible by remember { mutableStateOf(true) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            scope.launch {
                val jsonStr = appPrefs.exportSettingsBase64()
                context.contentResolver.openOutputStream(it)?.use { out ->
                    out.write(jsonStr.toByteArray(Charsets.UTF_8))
                }
                Toast.makeText(context, strings.exportSuccess, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            scope.launch {
                try {
                    val text = context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
                    val success = appPrefs.importSettingsBase64(text)
                    if (success) {
                        Toast.makeText(context, strings.importSuccess, Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, strings.importFailed, Toast.LENGTH_SHORT).show()
                    }
                } catch(e: Exception) {
                    Toast.makeText(context, strings.importFailed, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val dirPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            scope.launch { appPrefs.setDownloadTreeUri(it.toString()) }
            Toast.makeText(context, strings.settingsSaved, Toast.LENGTH_SHORT).show()
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (available.y < -10f && !isSearchFocused) {
                    isPillVisible = false
                } else if (available.y > 10f) {
                    isPillVisible = true
                }
                return Offset.Zero
            }
        }
    }

    BackHandler(enabled = isSelectionMode) { selectedPosts = emptyMap() }

    if (showMultiCollectionPicker && isSelectionMode) {
        MultiCollectionPickerDialog(
            posts = selectedPosts.values.toList(),
            onDone = {
                showMultiCollectionPicker = false
                selectedPosts = emptyMap()
            },
            onDismiss = { showMultiCollectionPicker = false }
        )
    }

    if (showSavedSearchesSheet) {
        SavedSearchesSheet(
            appPrefs = appPrefs,
            currentAppliedTag = currentAppliedTag,
            onOpenSearch = { query ->
                showSavedSearchesSheet = false
                openSearchQuery(query)
            },
            onDismiss = { showSavedSearchesSheet = false }
        )
    }

    BackHandler(enabled = isSearchFocused || expanded) {
        expanded = false
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    LaunchedEffect(Unit) {
        delay(100)
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    if (showLoginDialog) {
        LoginSheet(
            loginUsername = loginUsername,
            loginApiKey = loginApiKey,
            onLoginSave = onLoginSave,
            onDismiss = { showLoginDialog = false }
        )
    }

    if (showBlacklistDialog) {
        BlacklistSheet(
            blacklistedTags = blacklistedTags,
            onBlacklistSave = onBlacklistSave,
            onDismiss = { showBlacklistDialog = false },
            loginUsername = loginUsername
        )
    }

    if (showFollowSheet) {
        FollowedTagsSheet(
            appPrefs = appPrefs,
            currentAppliedTag = currentAppliedTag,
            isNsfwEnabled = isNsfwEnabled,
            blacklistedTags = blacklistedTags,
            onOpenTag = { tag ->
                showFollowSheet = false
                onSearchInputChange(TextFieldValue(tag, TextRange(tag.length)))
                onSearch()
            },
            onDismiss = { showFollowSheet = false }
        )
    }

    if (showDownloadSheet) {
        DownloadQueueSheet(
            currentAppliedTag = currentAppliedTag,
            isNsfwEnabled = isNsfwEnabled,
            blacklistedTags = blacklistedTags,
            downloadTreeUri = savedTreeUri,
            onDismiss = { showDownloadSheet = false },
            fileNameTemplate = fileNameTemplate,
            isDataSaverEnabled = isDataSaverEnabled
        )
    }

    if (showSettingsDialog) {
        SettingsSheet(
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle,
            isAutoPlay = isAutoPlay,
            onAutoPlayToggle = onAutoPlayToggle,
            appLanguage = appLanguage,
            onLanguageChange = onLanguageChange,
            savedTreeUri = savedTreeUri,
            onPickDownloadDir = { dirPickerLauncher.launch(null) },
            onExportSettings = { exportLauncher.launch("hideout_settings_backup.json") },
            onImportSettings = { importLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
            onDismiss = { showSettingsDialog = false },
            appPrefs = appPrefs
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            GalleryDrawerContent(
                loginUsername = loginUsername,
                isNsfwEnabled = isNsfwEnabled,
                onLoginClick = { scope.launch { drawerState.close() }; showLoginDialog = true },
                onFavoritesClick = { scope.launch { drawerState.close() }; onFavoritesClick() },
                onHotPostsClick = { scope.launch { drawerState.close() }; onHotPostsClick() },
                onBlacklistClick = { scope.launch { drawerState.close() }; showBlacklistDialog = true },
                onNsfwClick = { scope.launch { drawerState.close() }; onNsfwToggle() },
                onSettingsClick = { scope.launch { drawerState.close() }; showSettingsDialog = true },
                onFollowClick = { scope.launch { drawerState.close() }; showFollowSheet = true },
                onDownloadsClick = { scope.launch { drawerState.close() }; showDownloadSheet = true },
                isIncognito = isIncognito,
                onIncognitoClick = {
                    scope.launch {
                        drawerState.close()
                        appPrefs.setIncognito(!isIncognito)
                    }
                    Toast.makeText(context, if (isIncognito) strings.incognitoDisabled else strings.incognitoEnabled, Toast.LENGTH_SHORT).show()
                },
                onHistoryClick = { scope.launch { drawerState.close() }; onOpenOverlay(OverlayKeys.HISTORY) },
                onCollectionsClick = { scope.launch { drawerState.close() }; onOpenOverlay(OverlayKeys.COLLECTIONS) },
                onVaultClick = { scope.launch { drawerState.close() }; onOpenOverlay(OverlayKeys.VAULT) },
                onSavedSearchesClick = { scope.launch { drawerState.close() }; showSavedSearchesSheet = true }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .nestedScroll(nestedScrollConnection)
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus(force = true); keyboardController?.hide() }) }
            ) {
                Box(modifier = Modifier.size(0.dp).focusRequester(focusRequester).focusable())

                LaunchedEffect(searchInput.text, isSearchFocused) {
                    if (!isSearchFocused) { expanded = false; return@LaunchedEffect }
                    val lastWord = searchInput.text.split(" ").lastOrNull() ?: ""
                    if (lastWord.length >= 2) {
                        delay(300)
                        try {
                            tagSuggestions = NetworkModule.api!!.getAutocomplete(lastWord)
                            expanded = tagSuggestions.isNotEmpty()
                        } catch (e: Exception) { expanded = false }
                    } else { expanded = false }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.size >= 2) {
                                        val zoom = event.calculateZoom()
                                        currentZoom *= zoom
                                        if (currentZoom > 1.2f) {
                                            if (gridColumns > 1) gridColumns -= 1
                                            currentZoom = 1f
                                        } else if (currentZoom < 0.8f) {
                                            if (gridColumns < 5) gridColumns += 1
                                            currentZoom = 1f
                                        }
                                        event.changes.forEach { it.consume() }
                                    } else {
                                        currentZoom = 1f
                                    }
                                }
                            }
                        }
                    ) {
                        if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.Loading) {
                            val shimmerHeights = remember { List(12) { (150..250).random().dp } }
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(gridColumns),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 88.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(shimmerHeights.size) { index ->
                                    Box(modifier = Modifier.fillMaxWidth().height(shimmerHeights[index]).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                                }
                            }
                        } else if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.Error) {
                            val state = lazyPagingItems.loadState.refresh as LoadState.Error
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = strings.cdError, tint = Color.Red, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(strings.loadFailed, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                                    Text(state.error.localizedMessage ?: "Unknown Error", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                                    Button(onClick = { lazyPagingItems.retry() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text(strings.retry, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                                }
                            }
                        } else if (lazyPagingItems.itemCount == 0 && lazyPagingItems.loadState.refresh is LoadState.NotLoading) {
                            Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                    Icon(Icons.Default.Warning, contentDescription = strings.cdNoResults, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(64.dp))
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(strings.noResults, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            LazyVerticalStaggeredGrid(
                                columns = StaggeredGridCells.Fixed(gridColumns),
                                state = gridState,
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 88.dp, bottom = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalItemSpacing = 12.dp,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(
                                    count = lazyPagingItems.itemCount,
                                    key = { index ->
                                        val post = lazyPagingItems.peek(index)
                                        if (post != null) "${post.id}_$index" else index
                                    }
                                ) { index ->
                                    val post = lazyPagingItems[index]
                                    if (post != null) {
                                        val ext = post.file?.ext ?: ""
                                        val md5 = post.file?.md5 ?: ""
                                        val cachedFile = getCachedFile(context, md5, ext)

                                        val imageModel = if (ext == "swf") {
                                            post.preview?.url ?: post.sample?.url
                                        } else {
                                            cachedFile ?: post.preview?.url ?: post.sample?.url ?: post.file?.url
                                        }

                                        val isSelected = selectedPosts.containsKey(post.id)
                                        Card(
                                            modifier = Modifier.fillMaxWidth().combinedClickable(
                                                onClick = {
                                                    if (isSelectionMode) {
                                                        toggleSelection(post)
                                                    } else {
                                                        focusManager.clearFocus(force = true); keyboardController?.hide(); onPostClick(index)
                                                    }
                                                },
                                                onLongClick = { toggleSelection(post) }
                                            ),
                                            shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(0.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                        ) {
                                            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp), contentAlignment = Alignment.Center) {
                                                if (ext == "swf") {
                                                    Image(
                                                        painter = painterResource(id = R.drawable.ic_launcher_warning),
                                                        contentDescription = strings.unsupportedSwf,
                                                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(32.dp),
                                                        contentScale = ContentScale.Fit
                                                    )
                                                } else if (imageModel != null) {
                                                    AsyncImage(
                                                        model = imageRequest(context, imageModel, cfClearanceCookie),
                                                        imageLoader = gifEnabledLoader,
                                                        contentDescription = "${strings.idLabel(post.id)}",
                                                        modifier = Modifier.fillMaxWidth(),
                                                        contentScale = ContentScale.FillWidth
                                                    )
                                                } else {
                                                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(48.dp))
                                                }

                                                if (LocalPostManager.isFavorited(post)) {
                                                    Icon(
                                                        Icons.Default.Favorite,
                                                        contentDescription = null,
                                                        tint = Color.Red,
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(8.dp)
                                                            .size(20.dp)
                                                    )
                                                }

                                                if (ext.isNotBlank()) {
                                                    Box(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                        Text(ext.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }

                                                if (isSelectionMode) {
                                                    Box(
                                                        modifier = Modifier
                                                            .matchParentSize()
                                                            .background(if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.15f))
                                                    )
                                                    Icon(
                                                        if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                                        contentDescription = null,
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                                                        modifier = Modifier.align(Alignment.TopStart).padding(8.dp).size(24.dp)
                                                    )
                                                }

                                                if ((ext == "webm" || ext == "mp4") && post.duration != null && post.duration > 0) {
                                                    Box(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                        Text(formatDuration(post.duration), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp)
                ) {
                    AnimatedVisibility(
                        visible = isPillVisible || isSearchFocused,
                        enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                            shadowElevation = 12.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = strings.cdOpenMenu, tint = MaterialTheme.colorScheme.primary)
                                }

                                val titleText = when {
                                    currentAppliedTag == "#HOT#" -> strings.weeklyHot
                                    currentAppliedTag.isBlank() -> strings.latestPosts
                                    else -> strings.searchResults(currentAppliedTag)
                                }

                                TextField(
                                    value = searchInput,
                                    onValueChange = onSearchInputChange,
                                    modifier = Modifier.weight(1f).onFocusChanged { focusState -> isSearchFocused = focusState.isFocused },
                                    placeholder = {
                                        Text(
                                            if (isSearchFocused) strings.searchTags else titleText,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            fontSize = 14.sp
                                        )
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { expanded = false; onSearch(); focusManager.clearFocus(force = true); keyboardController?.hide() }),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                IconButton(onClick = onVpnActionClick) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = strings.enableWireGuard,
                                        tint = if (isVpnActive) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = expanded && tagSuggestions.isNotEmpty(),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            shape = RoundedCornerShape(24.dp),
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        ) {
                            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                                items(tagSuggestions) { tag ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val words = searchInput.text.split(" ").dropLast(1).toMutableList()
                                            if (words.isNotEmpty() && words[0].isBlank()) words.clear()
                                            words.add(tag.name)
                                            val newText = words.joinToString(" ") + " "
                                            onSearchInputChange(TextFieldValue(newText, TextRange(newText.length)))
                                            expanded = false
                                        }.padding(horizontal = 20.dp, vertical = 14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tag.name, color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.SemiBold)
                                        Text("${tag.post_count}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    if (tag != tagSuggestions.last()) {
                                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f)))
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = isSearchFocused && !(expanded && tagSuggestions.isNotEmpty()),
                        enter = fadeIn() + expandVertically(expandFrom = Alignment.Top),
                        exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top)
                    ) {
                        SearchAssistPanel(
                            searchText = searchInput.text,
                            history = searchHistory,
                            isNsfwEnabled = isNsfwEnabled,
                            onApplyFilter = { newText ->
                                onSearchInputChange(TextFieldValue(newText, TextRange(newText.length)))
                            },
                            onHistoryClick = { query ->
                                onSearchInputChange(TextFieldValue(query, TextRange(query.length)))
                                expanded = false
                                onSearch()
                                focusManager.clearFocus(force = true)
                                keyboardController?.hide()
                            },
                            onHistoryRemove = { query -> scope.launch { appPrefs.removeSearchHistory(query) } },
                            onHistoryClear = { scope.launch { appPrefs.clearSearchHistory() } },
                            savedSearches = savedSearches,
                            onSavedSearchClick = { query -> openSearchQuery(query) }
                        )
                    }
                }

                if (isSelectionMode) {
                    SelectionActionBar(
                        selectedCount = selectedPosts.size,
                        onSelectAll = {
                            val all = LinkedHashMap(selectedPosts)
                            for (i in 0 until lazyPagingItems.itemCount) {
                                lazyPagingItems.peek(i)?.let { all[it.id] = it }
                            }
                            selectedPosts = all
                        },
                        onDownload = {
                            if (DataSaver.shouldSave(context, isDataSaverEnabled)) {
                                Toast.makeText(context, strings.dataSaverBatchBlocked, Toast.LENGTH_LONG).show()
                            } else {
                                val requests = BatchDownloader.toRequests(selectedPosts.values.toList(), fileNameTemplate)
                                DownloadQueue.enqueueAll(context, requests, savedTreeUri, strings, silent = true)
                                Toast.makeText(context, strings.batchQueued(requests.size), Toast.LENGTH_SHORT).show()
                                selectedPosts = emptyMap()
                            }
                        },
                        onAddToCollection = { showMultiCollectionPicker = true },
                        onCancel = { selectedPosts = emptyMap() },
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    )
                }

                val poolIdForReader = PoolRepository.parsePoolId(currentAppliedTag)
                if (poolIdForReader != null && !isSelectionMode) {
                    ExtendedFloatingActionButton(
                        onClick = { onOpenPoolReader(poolIdForReader) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                        text = { Text(strings.poolReaderMode, fontWeight = FontWeight.Bold) },
                        modifier = Modifier.align(Alignment.BottomEnd).padding(20.dp)
                    )
                }
            }
        }
    }
}
