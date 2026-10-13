package com.CannedF0xy.hideout.ui

import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.ui.platform.LocalConfiguration
import com.CannedF0xy.hideout.download.FileNameFormatter
import com.CannedF0xy.hideout.ui.screens.VaultScreen
import com.CannedF0xy.hideout.ui.screens.CollectionsScreen
import com.CannedF0xy.hideout.ui.screens.HistoryScreen
import com.CannedF0xy.hideout.ui.navigation.OverlayKeys
import com.CannedF0xy.hideout.vpn.VpnReconnector
import com.CannedF0xy.hideout.vpn.VpnGuard
import kotlinx.coroutines.delay
import com.CannedF0xy.hideout.ui.screens.PoolReaderScreen
import com.CannedF0xy.hideout.ui.navigation.PendingNavigation
import com.CannedF0xy.hideout.follow.FollowChecker
import com.CannedF0xy.hideout.follow.FollowCheckWorker
import android.app.Activity
import android.content.Intent
import android.net.VpnService
import android.os.Build.VERSION.SDK_INT
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.filter
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.E621PagingSource
import com.CannedF0xy.hideout.network.GithubRelease
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.network.checkGithubUpdate
import com.CannedF0xy.hideout.network.model.Post
import com.CannedF0xy.hideout.ui.components.CloudflareBypassDialog
import com.CannedF0xy.hideout.ui.components.UpdateDialog
import com.CannedF0xy.hideout.ui.navigation.NavEntry
import com.CannedF0xy.hideout.ui.screens.DetailPagerScreen
import com.CannedF0xy.hideout.ui.screens.GalleryScreen
import com.CannedF0xy.hideout.ui.screens.VpnSetupScreen
import com.CannedF0xy.hideout.vpn.VpnCleanupService
import com.CannedF0xy.hideout.vpn.VpnManager
import com.CannedF0xy.hideout.vpn.autoConnectMullvad
import com.google.gson.Gson
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Composable
fun MainApp(
    appPrefs: AppPreferences,
    isDarkTheme: Boolean,
    appLanguage: String
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    val isNsfwEnabled by appPrefs.isNsfwEnabled.collectAsState(initial = false)
    val loginUsername by appPrefs.username.collectAsState(initial = "")
    val loginApiKey by appPrefs.apiKey.collectAsState(initial = "")
    val blacklistedTagsSet by appPrefs.blacklistedTags.collectAsState(initial = emptySet())
    val cfClearanceCookie by appPrefs.cfClearance.collectAsState(initial = "")
    val savedStackStr by appPrefs.searchStack.collectAsState(initial = "")
    val isVpnBypassedPref by appPrefs.isVpnBypassed.collectAsState(initial = false)
    val savedVpnConfigText by appPrefs.vpnConfigText.collectAsState(initial = "")
    val isAutoPlay by appPrefs.isAutoPlay.collectAsState(initial = true)
    val downloadTreeUri by appPrefs.downloadTreeUri.collectAsState(initial = "")

    var isVpnBypassed by remember { mutableStateOf(false) }
    LaunchedEffect(isVpnBypassedPref) { isVpnBypassed = isVpnBypassedPref }

    var showCaptchaDialog by remember { mutableStateOf(false) }
    var hasCheckedCaptchaOnStartup by remember { mutableStateOf(false) }
    var updateRelease by remember { mutableStateOf<GithubRelease?>(null) }

    LaunchedEffect(Unit) {
        scope.launch {
            val release = checkGithubUpdate()
            if (release != null) {
                try {
                    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                    val currentVersion = packageInfo.versionName ?: ""
                    val latestTag = release.tag_name

                    fun extractNumbers(ver: String): List<Int> {
                        return ver.split(Regex("[^0-9]+")).filter { it.isNotEmpty() }.map { it.toInt() }
                    }

                    val lParts = extractNumbers(latestTag)
                    val cParts = extractNumbers(currentVersion)

                    var isGreater = false
                    val length = maxOf(lParts.size, cParts.size)
                    for (i in 0 until length) {
                        val l = lParts.getOrElse(i) { 0 }
                        val c = cParts.getOrElse(i) { 0 }
                        if (l > c) { isGreater = true; break }
                        if (l < c) { isGreater = false; break }
                    }

                    if (isGreater) {
                        updateRelease = release
                    }
                } catch (e: Exception) {}
            }
        }
    }

    LaunchedEffect(loginUsername, loginApiKey, cfClearanceCookie) {
        NetworkModule.username = loginUsername
        NetworkModule.apiKey = loginApiKey
        NetworkModule.cfClearance = cfClearanceCookie
    }

    val backend = remember { VpnManager.getBackend(context) }
    val tunnel = VpnManager.tunnel
    val vpnState = VpnManager.vpnState.value

    var vpnConfig by remember { mutableStateOf<Config?>(null) }
    var vpnErrorMessage by remember { mutableStateOf<String?>(null) }
    var setupMode by remember { mutableStateOf<String?>(null) }
    var mullvadAccountInput by remember { mutableStateOf("") }

    LaunchedEffect(savedVpnConfigText) {
        if (savedVpnConfigText.isNotBlank()) {
            try {
                val config = Config.parse(savedVpnConfigText.byteInputStream(Charsets.UTF_8))
                vpnConfig = config
                val intent = VpnService.prepare(context)
                if (intent == null) {
                    scope.launch(Dispatchers.IO) {
                        try {
                            backend.setState(tunnel, Tunnel.State.UP, config)
                            context.startService(Intent(context, VpnCleanupService::class.java))
                        } catch (e: Exception) {}
                    }
                }
            } catch (e: Exception) {}
        }
    }

    LaunchedEffect(Unit) {
        NetworkModule.onCloudflareChallenge = {
            scope.launch(Dispatchers.Main) { showCaptchaDialog = true }
        }
    }

    LaunchedEffect(vpnState, isVpnBypassed) {
        if ((vpnState == Tunnel.State.UP || isVpnBypassed) && !hasCheckedCaptchaOnStartup) {
            if (cfClearanceCookie.isBlank()) {
                showCaptchaDialog = true
            }
            hasCheckedCaptchaOnStartup = true
        }
    }

    val followedTags by appPrefs.followedTags.collectAsState(initial = null)
    val hasFollowedTags = followedTags?.isNotEmpty()
    val isNetworkReady = vpnState == Tunnel.State.UP || isVpnBypassed

    LaunchedEffect(hasFollowedTags) {
        when (hasFollowedTags) {
            true -> FollowCheckWorker.schedule(context)
            false -> FollowCheckWorker.cancel(context)
            null -> Unit
        }
    }

    LaunchedEffect(isNetworkReady, hasFollowedTags) {
        if (isNetworkReady && hasFollowedTags == true) {
            while (true) {
                try {
                    FollowChecker.check(context)
                } catch (e: Exception) {
                }
                delay(FOLLOW_CHECK_INTERVAL_MS)
            }
        }
    }

    var readerPoolId by remember { mutableStateOf<Int?>(null) }

    var navStack by remember { mutableStateOf(listOf(NavEntry(""))) }
    var isStackLoaded by remember { mutableStateOf(false) }

    LaunchedEffect(savedStackStr) {
        if (!isStackLoaded && savedStackStr.isNotEmpty()) {
            try {
                val parsed = Gson().fromJson(savedStackStr, Array<NavEntry>::class.java).toList()
                if (parsed.isNotEmpty()) navStack = parsed
            } catch (e: Exception) {
                val split = savedStackStr.split("||")
                if (split.isNotEmpty()) {
                    navStack = split.map { NavEntry(query = it) }
                }
            }
            isStackLoaded = true
        } else if (!isStackLoaded) {
            isStackLoaded = true
        }
    }

    val currentEntry = navStack.lastOrNull() ?: NavEntry("")
    val currentTag = currentEntry.query
    val selectedPostIndex = currentEntry.detailIdx

    var searchInput by remember { mutableStateOf(TextFieldValue("")) }

    LaunchedEffect(currentTag) {
        val text = if (currentTag == "#HOT#") "" else currentTag
        searchInput = TextFieldValue(text = text, selection = TextRange(text.length))
    }

    val blacklistedList = remember(blacklistedTagsSet) { blacklistedTagsSet.toList() }

    val pagerCache = remember {
        object : java.util.LinkedHashMap<String, Flow<androidx.paging.PagingData<Post>>>(5, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Flow<androidx.paging.PagingData<Post>>>?): Boolean {
                return size > 5
            }
        }
    }

    val gridStateCache = remember {
        object : java.util.LinkedHashMap<String, LazyStaggeredGridState>(5, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, LazyStaggeredGridState>?): Boolean {
                return size > 5
            }
        }
    }

    val listCacheKey = "${currentTag}_${isNsfwEnabled}_${blacklistedList.hashCode()}"

    val deduplicatedFlow = remember(listCacheKey) {
        pagerCache.getOrPut(listCacheKey) {
            Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = {
                    E621PagingSource(
                        query = currentTag,
                        isNsfwEnabled = isNsfwEnabled,
                        blacklistedTags = blacklistedList,
                        onAuthError = {
                            scope.launch {
                                appPrefs.setCredentials("", "")
                                Toast.makeText(context, strings.loginFailed, Toast.LENGTH_LONG).show()
                            }
                        },
                        onCloudflareBlocked = {
                            scope.launch(Dispatchers.Main) { showCaptchaDialog = true }
                        }
                    )
                }
            ).flow.map { pagingData ->
                val seen = Collections.newSetFromMap(ConcurrentHashMap<Int, Boolean>())
                pagingData.filter { post -> seen.add(post.id) }
            }.cachedIn(scope)
        }
    }

    val lazyPagingItems = deduplicatedFlow.collectAsLazyPagingItems()

    val gridState = remember(listCacheKey) {
        gridStateCache.getOrPut(listCacheKey) { LazyStaggeredGridState() }
    }

    val gifEnabledLoader = remember {
        ImageLoader.Builder(context)
            .okHttpClient(NetworkModule.client)
            .components {
                if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory())
            }
            .build()
    }

    LaunchedEffect(currentEntry) {
        if (currentEntry.detailIdx == null) {
            if (gridState.firstVisibleItemIndex != currentEntry.scrollIdx || gridState.firstVisibleItemScrollOffset != currentEntry.scrollOff) {
                gridState.scrollToItem(currentEntry.scrollIdx, currentEntry.scrollOff)
            }
        }
    }

    val isKillSwitchEnabled by appPrefs.isKillSwitchEnabled.collectAsState(initial = false)
    val isAutoReconnectEnabled by appPrefs.isAutoReconnectEnabled.collectAsState(initial = true)

    LaunchedEffect(isKillSwitchEnabled, savedVpnConfigText) {
        VpnGuard.killSwitchEnabled = isKillSwitchEnabled
        VpnGuard.usesInAppTunnel = savedVpnConfigText.isNotBlank()
    }

    LaunchedEffect(vpnState) {
        if (vpnState == Tunnel.State.UP) {
            lazyPagingItems.retry()
            return@LaunchedEffect
        }
        if (isAutoReconnectEnabled && savedVpnConfigText.isNotBlank() && VpnManager.shouldAutoReconnect()) {
            Toast.makeText(context, strings.vpnReconnecting, Toast.LENGTH_SHORT).show()
            val reconnected = VpnReconnector.reconnect(context, savedVpnConfigText)
            Toast.makeText(context, if (reconnected) strings.vpnReconnected else strings.vpnReconnectFailed, Toast.LENGTH_SHORT).show()
        }
    }

    var activeOverlay by remember { mutableStateOf<String?>(null) }
    var detailPaneKey by remember { mutableIntStateOf(0) }
    val fileNameTemplate by appPrefs.fileNameTemplate.collectAsState(initial = FileNameFormatter.DEFAULT_TEMPLATE)
    val isDataSaverEnabled by appPrefs.isDataSaverEnabled.collectAsState(initial = false)

    fun pushTag(newTag: String) {
        val top = navStack.last().copy(
            scrollIdx = gridState.firstVisibleItemIndex,
            scrollOff = gridState.firstVisibleItemScrollOffset,
            detailIdx = selectedPostIndex
        )
        val newStack = navStack.dropLast(1) + top + NavEntry(query = newTag)
        navStack = newStack
        scope.launch { appPrefs.setSearchStack(Gson().toJson(newStack)) }
    }

    fun popStack() {
        if (navStack.size > 1) {
            val popped = navStack.dropLast(1)
            navStack = popped
            scope.launch { appPrefs.setSearchStack(Gson().toJson(popped)) }
        }
    }

    fun closeDetail() {
        val currentIdx = selectedPostIndex ?: 0
        val top = navStack.last().copy(detailIdx = null, scrollIdx = currentIdx, scrollOff = 0)
        val newStack = navStack.dropLast(1) + top
        navStack = newStack
        scope.launch { appPrefs.setSearchStack(Gson().toJson(newStack)) }
    }

    val pendingOpenTag = PendingNavigation.openTag
    LaunchedEffect(pendingOpenTag, isStackLoaded) {
        if (pendingOpenTag != null && isStackLoaded) {
            PendingNavigation.openTag = null
            readerPoolId = null
            activeOverlay = null
            if (currentTag != pendingOpenTag) pushTag(pendingOpenTag)
        }
    }

    val pendingRelease = updateRelease
    if (pendingRelease != null) {
        UpdateDialog(release = pendingRelease, onDismiss = { updateRelease = null })
    }

    if (showCaptchaDialog) {
        CloudflareBypassDialog(
            onSuccess = { newCookie ->
                if (newCookie.isNotBlank()) {
                    scope.launch { appPrefs.setCfClearance(newCookie) }
                    NetworkModule.cfClearance = newCookie
                }
                showCaptchaDialog = false
                lazyPagingItems.refresh()
            },
            onCancel = { showCaptchaDialog = false }
        )
    }

    BackHandler(enabled = isStackLoaded && (navStack.size > 1 || currentEntry.detailIdx != null)) {
        if (currentEntry.detailIdx != null) {
            closeDetail()
        } else {
            popStack()
        }
    }

    BackHandler(enabled = isStackLoaded && setupMode == null && selectedPostIndex == null && navStack.size <= 1) {
        Thread { VpnManager.stopVpnSync(context) }.start()
        activity?.finishAffinity()
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (setupMode == "MULLVAD") {
                scope.launch {
                    vpnErrorMessage = null
                    val success = autoConnectMullvad(mullvadAccountInput, context, appPrefs)
                    if (success) {
                        isVpnBypassed = true
                        appPrefs.setVpnBypassed(true)
                    } else {
                        vpnErrorMessage = strings.loginFailedMullvad
                    }
                }
            } else {
                vpnConfig?.let { config ->
                    scope.launch(Dispatchers.IO) {
                        try {
                            backend.setState(tunnel, Tunnel.State.UP, config)
                            context.startService(Intent(context, VpnCleanupService::class.java))
                        } catch (e: Exception) {}
                    }
                    isVpnBypassed = true
                    scope.launch { appPrefs.setVpnBypassed(true) }
                }
            }
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(it)
                if (inputStream != null) {
                    val text = inputStream.bufferedReader(Charsets.UTF_8).use { reader -> reader.readText() }
                    val cleanText = text.replace("\uFEFF", "").trim()
                    scope.launch { appPrefs.setVpnConfig(cleanText) }
                    vpnConfig = Config.parse(cleanText.byteInputStream(Charsets.UTF_8))
                    vpnErrorMessage = null
                    Toast.makeText(context, strings.configSaved, Toast.LENGTH_SHORT).show()
                } else { vpnErrorMessage = strings.fileReadFailed }
            } catch (e: Exception) {
                vpnErrorMessage = strings.parseFailed
            }
        }
    }

    if (vpnState != Tunnel.State.UP && !isVpnBypassed) {
        VpnSetupScreen(
            setupMode = setupMode,
            onSetupModeChange = { setupMode = it },
            vpnErrorMessage = vpnErrorMessage,
            hasVpnConfig = vpnConfig != null,
            mullvadAccountInput = mullvadAccountInput,
            onMullvadAccountChange = { mullvadAccountInput = it },
            onSelectConfig = { filePickerLauncher.launch("*/*") },
            onEnableTunnel = {
                val intent = VpnService.prepare(context)
                if (intent != null) vpnPermissionLauncher.launch(intent)
                else {
                    scope.launch(Dispatchers.IO) {
                        try {
                            backend.setState(tunnel, Tunnel.State.UP, vpnConfig)
                            context.startService(Intent(context, VpnCleanupService::class.java))
                        } catch (e: Exception) {}
                    }
                    isVpnBypassed = true
                    scope.launch { appPrefs.setVpnBypassed(true) }
                }
            },
            onConnectMullvad = {
                val intent = VpnService.prepare(context)
                if (intent != null) vpnPermissionLauncher.launch(intent)
                else {
                    scope.launch {
                        vpnErrorMessage = null
                        val success = autoConnectMullvad(mullvadAccountInput, context, appPrefs)
                        if (success) {
                            isVpnBypassed = true
                            appPrefs.setVpnBypassed(true)
                        } else {
                            vpnErrorMessage = strings.loginFailedMullvad
                        }
                    }
                }
            },
            onSkipSetup = {
                isVpnBypassed = true
                scope.launch { appPrefs.setVpnBypassed(true) }
            }
        )
    } else if (isStackLoaded) {
        val screenWidthDp = LocalConfiguration.current.screenWidthDp
        val isWideLayout = screenWidthDp >= TWO_PANE_MIN_WIDTH_DP
        val detailPane: @Composable () -> Unit = {
            DetailPagerScreen(
                lazyPagingItems = lazyPagingItems,
                initialIndex = selectedPostIndex ?: 0,
                appPrefs = appPrefs,
                isDarkTheme = isDarkTheme,
                isAutoPlay = isAutoPlay,
                gifEnabledLoader = gifEnabledLoader,
                cfClearanceCookie = cfClearanceCookie,
                onClose = {
                    closeDetail()
                },
                onTagClick = { clickedTag -> pushTag(clickedTag) },
                onPageChanged = { newIdx ->
                    if (currentEntry.detailIdx != newIdx) {
                        val top = navStack.last().copy(detailIdx = newIdx)
                        val newStack = navStack.dropLast(1) + top
                        navStack = newStack
                        scope.launch { appPrefs.setSearchStack(Gson().toJson(newStack)) }
                    }
                }
            )
        }
        if (selectedPostIndex != null && !isWideLayout) {
            detailPane()
        } else {
            val showSidePane = isWideLayout && selectedPostIndex != null
            Box(modifier = Modifier.fillMaxSize()) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(modifier = Modifier.weight(if (showSidePane) GALLERY_PANE_WEIGHT else 1f).fillMaxHeight()) {
                        GalleryScreen(
                            lazyPagingItems = lazyPagingItems,
                            searchInput = searchInput,
                            currentAppliedTag = currentTag,
                            gridState = gridState,
                            gifEnabledLoader = gifEnabledLoader,
                            cfClearanceCookie = cfClearanceCookie,
                            onSearchInputChange = { searchInput = it },
                            onSearch = {
                                val submitted = searchInput.text
                                if (submitted.isNotBlank()) scope.launch { appPrefs.addSearchHistory(submitted) }
                                if (submitted != currentTag) pushTag(submitted)
                            },
                            isVpnActive = (vpnState == Tunnel.State.UP),
                            onVpnActionClick = {
                                if (vpnState == Tunnel.State.UP) {
                                    Thread { VpnManager.stopVpnSync(context) }.start()
                                } else {
                                    isVpnBypassed = false
                                    scope.launch { appPrefs.setVpnBypassed(false) }
                                }
                            },
                            onPostClick = { index ->
                                detailPaneKey++
                                val top = navStack.last().copy(
                                    scrollIdx = gridState.firstVisibleItemIndex,
                                    scrollOff = gridState.firstVisibleItemScrollOffset,
                                    detailIdx = index
                                )
                                val newStack = navStack.dropLast(1) + top
                                navStack = newStack
                                scope.launch { appPrefs.setSearchStack(Gson().toJson(newStack)) }
                            },
                            isDarkTheme = isDarkTheme,
                            onThemeToggle = { scope.launch { appPrefs.setDarkTheme(!isDarkTheme) } },
                            isNsfwEnabled = isNsfwEnabled,
                            onNsfwToggle = { scope.launch { appPrefs.setNsfwEnabled(!isNsfwEnabled) } },
                            isAutoPlay = isAutoPlay,
                            onAutoPlayToggle = { scope.launch { appPrefs.setAutoPlay(!isAutoPlay) } },
                            loginUsername = loginUsername,
                            loginApiKey = loginApiKey,
                            onLoginSave = { user, key -> scope.launch { appPrefs.setCredentials(user, key) } },
                            onFavoritesClick = {
                                if (loginUsername.isNotBlank()) {
                                    val favTag = "fav:$loginUsername"
                                    if (currentTag != favTag) pushTag(favTag)
                                } else {
                                    Toast.makeText(context, strings.loginFirst, Toast.LENGTH_SHORT).show()
                                }
                            },
                            onHotPostsClick = {
                                val hotTag = "#HOT#"
                                if (currentTag != hotTag) pushTag(hotTag)
                            },
                            blacklistedTags = blacklistedList,
                            onBlacklistSave = { newList -> scope.launch { appPrefs.setBlacklist(newList.toSet()) } },
                            appPrefs = appPrefs,
                            appLanguage = appLanguage,
                            onLanguageChange = { newLang -> scope.launch { appPrefs.setAppLanguage(newLang) } },
                            onOpenPoolReader = { poolId -> readerPoolId = poolId },
                            onOpenOverlay = { key -> activeOverlay = key },
                            layoutWidthDp = if (showSidePane) (screenWidthDp * GALLERY_PANE_WEIGHT).toInt() else null
                        )
                    }
                    if (showSidePane) {
                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        )
                        Box(modifier = Modifier.weight(1f - GALLERY_PANE_WEIGHT).fillMaxHeight()) {
                            key(detailPaneKey) { detailPane() }
                        }
                    }
                }
                val activePoolId = readerPoolId
                if (activePoolId != null) {
                    PoolReaderScreen(
                        poolId = activePoolId,
                        isNsfwEnabled = isNsfwEnabled,
                        blacklistedTags = blacklistedList,
                        imageLoader = gifEnabledLoader,
                        cfClearanceCookie = cfClearanceCookie,
                        downloadTreeUri = downloadTreeUri,
                        onClose = { readerPoolId = null },
                        fileNameTemplate = fileNameTemplate,
                        isDataSaverEnabled = isDataSaverEnabled
                    )
                }
                val openPostFromOverlay: (Post) -> Unit = { post ->
                    activeOverlay = null
                    val idQuery = "id:${post.id}"
                    if (currentTag != idQuery) pushTag(idQuery)
                }
                when (activeOverlay) {
                    OverlayKeys.HISTORY -> HistoryScreen(
                        imageLoader = gifEnabledLoader,
                        cfClearanceCookie = cfClearanceCookie,
                        onOpenPost = openPostFromOverlay,
                        onClose = { activeOverlay = null }
                    )
                    OverlayKeys.COLLECTIONS -> CollectionsScreen(
                        imageLoader = gifEnabledLoader,
                        cfClearanceCookie = cfClearanceCookie,
                        onOpenPost = openPostFromOverlay,
                        onClose = { activeOverlay = null }
                    )
                    OverlayKeys.VAULT -> VaultScreen(
                        imageLoader = gifEnabledLoader,
                        onClose = { activeOverlay = null }
                    )
                }
            }
        }
    }
}

private const val FOLLOW_CHECK_INTERVAL_MS = 30L * 60L * 1000L
private const val TWO_PANE_MIN_WIDTH_DP = 720
private const val GALLERY_PANE_WEIGHT = 0.45f
