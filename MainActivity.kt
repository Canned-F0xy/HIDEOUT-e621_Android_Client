package com.CannedF0xy.hideout

import android.app.Activity
import android.app.DownloadManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.net.VpnService
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.WindowManager
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.wireguard.android.backend.GoBackend
import com.wireguard.android.backend.Tunnel
import com.wireguard.config.Config
import com.wireguard.crypto.KeyPair
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

val NeonOrange = Color(0xFFFF6B00)
val PortalGrey = Color(0xFF8A8A8A)
val DeepBlack = Color(0xFF050505)
val DarkSurface = Color(0xFF141414)
val TextWhite = Color(0xFFE0E0E0)
val LightBackground = Color(0xFFF5F5F5)

val HideoutDarkTheme = darkColorScheme(
    primary = NeonOrange, background = DeepBlack, surface = DarkSurface, surfaceVariant = DarkSurface,
    onPrimary = DeepBlack, onBackground = TextWhite, onSurface = TextWhite, onSurfaceVariant = PortalGrey,
    primaryContainer = DarkSurface, onPrimaryContainer = NeonOrange
)

val HideoutLightTheme = lightColorScheme(
    primary = NeonOrange, background = LightBackground, surface = Color.White, surfaceVariant = Color.White,
    onPrimary = Color.White, onBackground = DeepBlack, onSurface = DeepBlack, onSurfaceVariant = PortalGrey,
    primaryContainer = Color.White, onPrimaryContainer = NeonOrange
)

interface AppStrings {
    val hideoutSetup: String
    val vpnWarning: String
    val selectConfig: String
    val changeConfig: String
    val enableTunnel: String
    val skipSetup: String
    val fileReadFailed: String
    val parseFailed: String
    val configSaved: String
    val accountLogin: String
    val apiKeyInfo: String
    val usernameHint: String
    val apiKeyHint: String
    val save: String
    val logout: String
    val cancel: String
    val loginFailed: String
    val authFailed: String
    val error403: String
    val errorTooManyTags: String
    val error451: String
    fun networkError(msg: String): String
    val networkErrorOccurred: String
    val blacklistManagement: String
    val blacklistInfo: String
    val tagInputHint: String
    val appSettings: String
    val useDarkTheme: String
    val downloadLocation: String
    val defaultFolder: String
    val customFolder: String
    val devGithub: String
    val settingsSaved: String
    val close: String
    fun changeLoginInfo(user: String): String
    val accountLoginMenu: String
    val viewFavorites: String
    val weeklyHot: String
    val manageBlacklist: String
    val r18ModeOn: String
    val r18ModeOff: String
    val appSettingsMenu: String
    val disableWireGuard: String
    val enableWireGuard: String
    val searchTags: String
    val latestPosts: String
    fun searchResults(tag: String): String
    val loadFailed: String
    val retry: String
    val noResults: String
    val favRemoved: String
    fun favRemoveFailed(code: Int): String
    val favAdded: String
    val favAddFailedLogin: String
    fun favAddFailed(code: Int): String
    fun fileLabel(ext: String): String
    fun scoreLabel(score: Int): String
    val descLabel: String
    val artistLabel: String
    val copyrightLabel: String
    val characterLabel: String
    val generalLabel: String
    val sourcesLabel: String
    val cannotOpenLink: String
    val savingPost: String
    val downloadStarting: String
    fun downloadFailed(msg: String): String
    val downloadComplete: String
    val loginFirst: String
    val cloudflareCaptcha: String
    val appLanguageLabel: String
    val languageKo: String
    val languageEn: String
    val cdLogo: String
    val cdClose: String
    val cdSearch: String
    val cdAdd: String
    val cdDelete: String
    val cdOpenBrowser: String
    val cdOpenMenu: String
    val cdError: String
    val cdNoResults: String
    val cdBack: String
    val cdFavoriteToggle: String
    val cdDownload: String
    val cdFullscreen: String
    fun idLabel(id: Int): String
    val setupMullvad: String
    val setupWireguard: String
    val mullvadHint: String
    val connectLabel: String
    val loginFailedMullvad: String
    val viewAllRelated: String
    val cacheManagement: String
    val cacheSizeLabel: String
    val clearCache: String
    val cacheCleared: String
    val updateAvailable: String
    fun updateMessage(ver: String): String
    val updateButton: String
    val laterButton: String
}

object KoStrings : AppStrings {
    override val hideoutSetup = "여우굴 파기"
    override val vpnWarning = "대한민국 IP는 대한민국 법적 법령으로 인하여\ne621 접속을 위해 VPN 초기설정이 필요합니다.\n외부 VPN 사용 시 '설정 안함'을 눌러주세요."
    override val selectConfig = "설정 파일 선택"
    override val changeConfig = "설정 파일 변경"
    override val enableTunnel = "터널 활성화"
    override val skipSetup = "설정 안함 (바로 사용하기)"
    override val fileReadFailed = "파일 읽기 실패."
    override val parseFailed = "파싱 실패: WireGuard 형식 확인."
    override val configSaved = "Config 저장 완료. 터널을 활성화하세요."
    override val accountLogin = "e621 계정 로그인"
    override val apiKeyInfo = "API 키는 e621 설정 -> Manage API Keys에서 발급받을 수 있습니다."
    override val usernameHint = "Username"
    override val apiKeyHint = "API Key"
    override val save = "저장"
    override val logout = "로그아웃"
    override val cancel = "취소"
    override val loginFailed = "로그인 실패, Username이나 API Key를 확인해주세요."
    override val authFailed = "로그인 인증 실패. 정보가 초기화되었습니다."
    override val error403 = "HTTP 403 오류: 서버 접근 거부됨."
    override val errorTooManyTags = "검색 오류: 너무 많은 검색어(태그)가 포함되어 있습니다."
    override val error451 = "HTTP 451 오류: 국가 차단됨. VPN을 확인하세요."
    override fun networkError(msg: String) = "네트워크 오류: $msg"
    override val networkErrorOccurred = "네트워크 오류 발생"
    override val blacklistManagement = "블랙리스트 관리"
    override val blacklistInfo = "검색에서 제외할 혐오/기피 태그를 등록하세요."
    override val tagInputHint = "태그 입력 (예: gore)"
    override val appSettings = "앱 설정"
    override val useDarkTheme = "다크 테마 사용"
    override val downloadLocation = "다운로드 저장 위치 (터치하여 탐색기에서 변경)"
    override val defaultFolder = "📁 기본 폴더 (Pictures/HIDEOUT)"
    override val customFolder = "📁 사용자 지정 폴더"
    override val devGithub = "개발자 GitHub"
    override val settingsSaved = "설정이 저장되었습니다."
    override val close = "닫기"
    override fun changeLoginInfo(user: String) = "로그인 정보 변경 ($user)"
    override val accountLoginMenu = "계정 로그인"
    override val viewFavorites = "내 즐겨찾기 보기"
    override val weeklyHot = "🔥 주간 핫 포스트"
    override val manageBlacklist = "블랙리스트 태그 관리"
    override val r18ModeOn = "R-18 모드: ON"
    override val r18ModeOff = "R-18 모드: OFF"
    override val appSettingsMenu = "앱 설정 (저장경로 / 테마)"
    override val disableWireGuard = "VPN 해제"
    override val enableWireGuard = "VPN 설정"
    override val searchTags = "태그 검색"
    override val latestPosts = "✨ 최신 포스트"
    override fun searchResults(tag: String) = "🔍 검색 결과: $tag"
    override val loadFailed = "데이터 불러오기 실패"
    override val retry = "다시 시도"
    override val noResults = "검색 결과가 없습니다."
    override val favRemoved = "즐겨찾기에서 해제되었습니다."
    override fun favRemoveFailed(code: Int) = "해제 실패: HTTP $code"
    override val favAdded = "즐겨찾기(추천)에 추가되었습니다."
    override val favAddFailedLogin = "즐겨찾기 실패: 로그인을 확인해주세요."
    override fun favAddFailed(code: Int) = "추가 실패: HTTP $code"
    override fun fileLabel(ext: String) = "파일: $ext"
    override fun scoreLabel(score: Int) = "⭐ 추천 수: $score"
    override val descLabel = "설명"
    override val artistLabel = "🎨 작가 (Artist)"
    override val copyrightLabel = "📺 원작 (Copyright)"
    override val characterLabel = "🦊 캐릭터 (Character)"
    override val generalLabel = "🏷️ 일반 태그 (General)"
    override val sourcesLabel = "🔗 원본 링크 (Sources)"
    override val cannotOpenLink = "링크를 열 수 없습니다."
    override val savingPost = "POST 저장 중"
    override val downloadStarting = "다운로드를 시작합니다."
    override fun downloadFailed(msg: String) = "다운로드 실패: $msg"
    override val downloadComplete = "HIDEOUT / POST 저장 완료"
    override val loginFirst = "먼저 로그인 해주세요."
    override val cloudflareCaptcha = "Cloudflare 캡차 인증"
    override val appLanguageLabel = "앱 언어"
    override val languageKo = "한국어"
    override val languageEn = "English"
    override val cdLogo = "Logo"
    override val cdClose = "닫기"
    override val cdSearch = "Search"
    override val cdAdd = "추가"
    override val cdDelete = "삭제"
    override val cdOpenBrowser = "웹사이트 이동"
    override val cdOpenMenu = "메뉴 열기"
    override val cdError = "Error"
    override val cdNoResults = "No Results"
    override val cdBack = "뒤로 가기"
    override val cdFavoriteToggle = "즐겨찾기 토글"
    override val cdDownload = "다운로드"
    override val cdFullscreen = "전체화면"
    override fun idLabel(id: Int) = "ID: $id"
    override val setupMullvad = "Mullvad VPN 자동 연결"
    override val setupWireguard = "WireGuard conf 파일 업로드"
    override val mullvadHint = "Mullvad 16자리 계정 번호"
    override val connectLabel = "연결하기"
    override val loginFailedMullvad = "인증 실패: 번호나 기기 제한(5대)을 확인하세요."
    override val viewAllRelated = "모든 연관 포스트 보기"
    override val cacheManagement = "캐시 관리"
    override val cacheSizeLabel = "캐시 용량: "
    override val clearCache = "캐시 비우기"
    override val cacheCleared = "캐시가 삭제되었습니다."
    override val updateAvailable = "새로운 업데이트가 있습니다!"
    override fun updateMessage(ver: String) = "새로운 버전 $ver 이 배포되었습니다. 다운로드하시겠습니까?"
    override val updateButton = "업데이트"
    override val laterButton = "나중에"
}

object EnStrings : AppStrings {
    override val hideoutSetup = "HIDEOUT Setup"
    override val vpnWarning = "Due to South Korean legal regulations,\na VPN initial setup is required to access e621.\nIf using an external VPN, please tap 'Skip Setup'."
    override val selectConfig = "Select Config File"
    override val changeConfig = "Change Config File"
    override val enableTunnel = "Enable Tunnel"
    override val skipSetup = "Skip Setup (Use directly)"
    override val fileReadFailed = "Failed to read file."
    override val parseFailed = "Parsing failed: Check WireGuard format."
    override val configSaved = "Config saved. Please enable the tunnel."
    override val accountLogin = "e621 Account Login"
    override val apiKeyInfo = "API keys can be generated in e621 Settings -> Manage API Keys."
    override val usernameHint = "Username"
    override val apiKeyHint = "API Key"
    override val save = "Save"
    override val logout = "Logout"
    override val cancel = "Cancel"
    override val loginFailed = "Login failed. Please check Username or API Key."
    override val authFailed = "Authentication failed. Information reset."
    override val error403 = "HTTP 403 Error: Server access denied."
    override val errorTooManyTags = "Search Error: Too many tags included."
    override val error451 = "HTTP 451 Error: Country blocked. Check your VPN."
    override fun networkError(msg: String) = "Network Error: $msg"
    override val networkErrorOccurred = "A network error occurred"
    override val blacklistManagement = "Blacklist Management"
    override val blacklistInfo = "Register tags to exclude from search."
    override val tagInputHint = "Enter tag (e.g., gore)"
    override val appSettings = "App Settings"
    override val useDarkTheme = "Use Dark Theme"
    override val downloadLocation = "Download Location (Tap to change)"
    override val defaultFolder = "📁 Default Folder (Pictures/HIDEOUT)"
    override val customFolder = "📁 Custom Folder"
    override val devGithub = "Developer GitHub"
    override val settingsSaved = "Settings saved."
    override val close = "Close"
    override fun changeLoginInfo(user: String) = "Change Login Info ($user)"
    override val accountLoginMenu = "Account Login"
    override val viewFavorites = "View My Favorites"
    override val weeklyHot = "🔥 Weekly Hot Posts"
    override val manageBlacklist = "Manage Blacklisted Tags"
    override val r18ModeOn = "R-18 Mode: ON"
    override val r18ModeOff = "R-18 Mode: OFF"
    override val appSettingsMenu = "App Settings (Path / Theme)"
    override val disableWireGuard = "Disable VPN"
    override val enableWireGuard = "Enable VPN"
    override val searchTags = "Search Tags"
    override val latestPosts = "✨ Latest Posts"
    override fun searchResults(tag: String) = "🔍 Search Results: $tag"
    override val loadFailed = "Failed to load data"
    override val retry = "Retry"
    override val noResults = "No results found."
    override val favRemoved = "Removed from favorites."
    override fun favRemoveFailed(code: Int) = "Removal failed: HTTP $code"
    override val favAdded = "Added to favorites."
    override val favAddFailedLogin = "Favorite failed: Please check login."
    override fun favAddFailed(code: Int) = "Addition failed: HTTP $code"
    override fun fileLabel(ext: String) = "File: $ext"
    override fun scoreLabel(score: Int) = "⭐ Score: $score"
    override val descLabel = "Description"
    override val artistLabel = "🎨 Artist"
    override val copyrightLabel = "📺 Copyright"
    override val characterLabel = "🦊 Character"
    override val generalLabel = "🏷️ General Tags"
    override val sourcesLabel = "🔗 Sources"
    override val cannotOpenLink = "Cannot open link."
    override val savingPost = "Saving POST"
    override val downloadStarting = "Starting download."
    override fun downloadFailed(msg: String) = "Download failed: $msg"
    override val downloadComplete = "HIDEOUT / POST saved successfully"
    override val loginFirst = "Please login first."
    override val cloudflareCaptcha = "Cloudflare Captcha Verification"
    override val appLanguageLabel = "App Language"
    override val languageKo = "한국어"
    override val languageEn = "English"
    override val cdLogo = "Logo"
    override val cdClose = "Close"
    override val cdSearch = "Search"
    override val cdAdd = "Add"
    override val cdDelete = "Delete"
    override val cdOpenBrowser = "Open Browser"
    override val cdOpenMenu = "Open Menu"
    override val cdError = "Error"
    override val cdNoResults = "No Results"
    override val cdBack = "Go Back"
    override val cdFavoriteToggle = "Toggle Favorite"
    override val cdDownload = "Download"
    override val cdFullscreen = "Fullscreen"
    override fun idLabel(id: Int) = "ID: $id"
    override val setupMullvad = "Mullvad VPN Auto Connect"
    override val setupWireguard = "WireGuard conf File Upload"
    override val mullvadHint = "Mullvad 16-digit Account Number"
    override val connectLabel = "Connect"
    override val loginFailedMullvad = "Auth failed: Check account or device limit (max 5)."
    override val viewAllRelated = "View All Related Posts"
    override val cacheManagement = "Cache Management"
    override val cacheSizeLabel = "Cache Size: "
    override val clearCache = "Clear Cache"
    override val cacheCleared = "Cache cleared."
    override val updateAvailable = "New update available!"
    override fun updateMessage(ver: String) = "Version $ver has been released. Would you like to download it?"
    override val updateButton = "Update"
    override val laterButton = "Later"
}

val LocalStrings = staticCompositionLocalOf<AppStrings> { KoStrings }

fun getEncryptedPrefs(context: Context): android.content.SharedPreferences {
    val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
    return EncryptedSharedPreferences.create(
        context, "SecretHideoutPrefs", masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

object VpnManager {
    private var backend: GoBackend? = null
    var vpnState = mutableStateOf(Tunnel.State.DOWN)
        private set

    val tunnel = object : Tunnel {
        override fun getName() = "HideoutTunnel"
        override fun onStateChange(state: Tunnel.State) {
            Handler(Looper.getMainLooper()).post { vpnState.value = state }
        }
    }

    fun getBackend(context: Context): GoBackend {
        if (backend == null) {
            backend = GoBackend(context.applicationContext)
        }
        return backend!!
    }

    fun stopVpnSync(context: Context) {
        try {
            getBackend(context).setState(tunnel, Tunnel.State.DOWN, null)
        } catch (e: Exception) { e.printStackTrace() }
    }
}

class VpnCleanupService : Service() {
    override fun onBind(intent: Intent?): IBinder? = null
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_NOT_STICKY

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        Thread {
            VpnManager.stopVpnSync(applicationContext)
            stopSelf()
        }.start()
    }
}

suspend fun autoConnectMullvad(
    accountNumber: String,
    context: Context,
    prefs: android.content.SharedPreferences,
    encryptedPrefs: android.content.SharedPreferences
): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            var privKey = encryptedPrefs.getString("mullvad_priv_key", null)
            var pubKey = encryptedPrefs.getString("mullvad_pub_key", null)

            if (privKey == null || pubKey == null) {
                val keyPair = KeyPair()
                privKey = keyPair.privateKey.toBase64()
                pubKey = keyPair.publicKey.toBase64()
                encryptedPrefs.edit()
                    .putString("mullvad_priv_key", privKey)
                    .putString("mullvad_pub_key", pubKey)
                    .apply()
            }

            val cleanAccount = accountNumber.replace(Regex("[^0-9]"), "")
            val ipResponse = MullvadNetwork.api.registerKey(cleanAccount, pubKey!!)

            if (!ipResponse.isSuccessful) return@withContext false

            val tunnelAddresses = ipResponse.body()?.string()?.trim() ?: ""
            if (tunnelAddresses.isEmpty()) return@withContext false

            val relays = MullvadNetwork.api.getRelays()
            val targetServer = relays.firstOrNull { it.country_code == "jp" && it.active }
                ?: relays.first { it.active }

            val configText = """
                [Interface]
                PrivateKey = $privKey
                Address = $tunnelAddresses
                DNS = 10.64.0.1
                
                [Peer]
                PublicKey = ${targetServer.pubkey}
                Endpoint = ${targetServer.ipv4_addr_in}:51820
                AllowedIPs = 0.0.0.0/0
            """.trimIndent()

            prefs.edit().putString("vpnConfigText", configText).apply()

            val config = Config.parse(configText.byteInputStream(Charsets.UTF_8))
            val backend = VpnManager.getBackend(context)

            backend.setState(VpnManager.tunnel, Tunnel.State.UP, config)
            context.startService(Intent(context, VpnCleanupService::class.java))

            return@withContext true
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext false
        }
    }
}

fun formatDuration(seconds: Double?): String {
    if (seconds == null || seconds <= 0) return ""
    val totalSeconds = seconds.toInt()
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", m, s)
}

fun getCachedFile(context: Context, md5: String?, ext: String): File? {
    if (md5.isNullOrBlank()) return null
    val cacheDir = File(context.cacheDir, "e621_media_cache")
    if (!cacheDir.exists()) return null
    val file = File(cacheDir, "$md5.$ext")
    return if (file.exists() && file.length() > 0) file else null
}

fun getCacheDirSize(context: Context): Long {
    val cacheDir = context.cacheDir
    if (!cacheDir.exists()) return 0L
    return cacheDir.walkTopDown().filter { it.isFile }.sumOf { it.length() }
}

fun formatByteSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    val gb = mb / 1024.0
    return when {
        gb >= 1.0 -> String.format(Locale.US, "%.2f GB", gb)
        mb >= 1.0 -> String.format(Locale.US, "%.2f MB", mb)
        kb >= 1.0 -> String.format(Locale.US, "%.2f KB", kb)
        else -> "$bytes Bytes"
    }
}

class MainActivity : ComponentActivity() {

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDestroy() {
        super.onDestroy()
        Thread { VpnManager.stopVpnSync(this) }.start()
        stopService(Intent(this, VpnCleanupService::class.java))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        setContent {
            val context = LocalContext.current
            val prefs = remember { context.getSharedPreferences("HideoutPrefs", Context.MODE_PRIVATE) }
            val encryptedPrefs = remember { getEncryptedPrefs(context) }
            var isDarkTheme by remember { mutableStateOf(prefs.getBoolean("isDarkTheme", true)) }
            var appLanguage by remember { mutableStateOf(prefs.getString("appLanguage", "ko") ?: "ko") }

            val currentStrings = if (appLanguage == "en") EnStrings else KoStrings

            CompositionLocalProvider(LocalStrings provides currentStrings) {
                MaterialTheme(colorScheme = if (isDarkTheme) HideoutDarkTheme else HideoutLightTheme) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                            MainApp(
                                prefs = prefs, encryptedPrefs = encryptedPrefs,
                                isDarkTheme = isDarkTheme,
                                onThemeToggle = {
                                    isDarkTheme = !isDarkTheme
                                    prefs.edit().putBoolean("isDarkTheme", isDarkTheme).apply()
                                },
                                appLanguage = appLanguage,
                                onLanguageChange = { newLang ->
                                    appLanguage = newLang
                                    prefs.edit().putString("appLanguage", newLang).apply()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(
    prefs: android.content.SharedPreferences,
    encryptedPrefs: android.content.SharedPreferences,
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    appLanguage: String,
    onLanguageChange: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = LocalContext.current as? Activity
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    var isNsfwEnabled by remember { mutableStateOf(prefs.getBoolean("isNsfwEnabled", false)) }
    var loginUsername by remember { mutableStateOf(encryptedPrefs.getString("username", "") ?: "") }
    var loginApiKey by remember { mutableStateOf(encryptedPrefs.getString("apiKey", "") ?: "") }
    var blacklistedTags by remember { mutableStateOf(prefs.getStringSet("blacklist", emptySet())?.toList() ?: emptyList()) }
    var cfClearanceCookie by remember { mutableStateOf(encryptedPrefs.getString("cfClearance", "") ?: "") }
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
                    val latestVersion = release.tag_name.replace("v", "")
                    if (currentVersion.isNotBlank() && latestVersion != currentVersion.replace("v", "")) {
                        updateRelease = release
                    }
                } catch (e: Exception) { e.printStackTrace() }
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
    var isVpnBypassed by remember { mutableStateOf(prefs.getBoolean("isVpnBypassed", false)) }

    var setupMode by remember { mutableStateOf<String?>(null) }
    var mullvadAccountInput by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        NetworkModule.onCloudflareChallenge = {
            scope.launch(Dispatchers.Main) {
                showCaptchaDialog = true
            }
        }

        val savedConfigText = prefs.getString("vpnConfigText", null)
        if (savedConfigText != null) {
            try {
                val config = Config.parse(savedConfigText.byteInputStream(Charsets.UTF_8))
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
            } catch (e: Exception) { e.printStackTrace() }
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

    val savedStackStr = prefs.getString("searchStack", "") ?: ""
    var searchStack by remember { mutableStateOf(if (savedStackStr.isNotEmpty()) savedStackStr.split("||") else listOf("")) }

    LaunchedEffect(searchStack) {
        prefs.edit().putString("searchStack", searchStack.joinToString("||")).apply()
    }

    var searchInput by remember { mutableStateOf("") }
    val currentTag = searchStack.last()

    var posts by remember { mutableStateOf<List<Post>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }
    var page by remember { mutableStateOf(1) }
    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var isLastPage by remember { mutableStateOf(false) }

    val gridState = rememberLazyStaggeredGridState()

    if (updateRelease != null) {
        AlertDialog(
            onDismissRequest = { updateRelease = null },
            title = { Text(strings.updateAvailable, color = NeonOrange) },
            text = { Text(strings.updateMessage(updateRelease!!.tag_name), color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateRelease!!.html_url))
                        context.startActivity(intent)
                        updateRelease = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                ) { Text(strings.updateButton, color = DeepBlack, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { updateRelease = null }) { Text(strings.laterButton, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showCaptchaDialog) {
        CloudflareBypassDialog(
            onSuccess = { newCookie ->
                if (newCookie.isNotBlank()) {
                    cfClearanceCookie = newCookie
                    NetworkModule.cfClearance = newCookie
                    encryptedPrefs.edit().putString("cfClearance", newCookie).apply()
                }
                showCaptchaDialog = false
                retryTrigger++
            },
            onCancel = {
                showCaptchaDialog = false
            }
        )
    }

    BackHandler(enabled = selectedIndex == null && searchStack.size > 1) {
        searchStack = searchStack.dropLast(1)
        val prevTag = searchStack.last()
        searchInput = if (prevTag == "#HOT#") "" else prevTag
        page = 1
        isLastPage = false
        retryTrigger++
        scope.launch { gridState.scrollToItem(0) }
    }

    if (selectedIndex != null) BackHandler { selectedIndex = null }

    BackHandler(enabled = selectedIndex == null && searchStack.size == 1) {
        Thread { VpnManager.stopVpnSync(context) }.start()
        activity?.finishAffinity()
    }

    LaunchedEffect(page, currentTag, retryTrigger, vpnState, isVpnBypassed) {
        if (vpnState == Tunnel.State.UP || isVpnBypassed) {
            if (page == 1) isLastPage = false
            isLoading = true
            errorMessage = null
            try {
                var finalTag = if (currentTag == "#HOT#") "date:>=7_days_ago order:score" else currentTag.trim()

                if (!isNsfwEnabled && !finalTag.contains("rating:")) finalTag = if (finalTag.isEmpty()) "rating:safe" else "$finalTag rating:safe"
                if (blacklistedTags.isNotEmpty()) {
                    val negatedTags = blacklistedTags.joinToString(" ") { "-$it" }
                    finalTag = if (finalTag.isEmpty()) negatedTags else "$finalTag $negatedTags"
                }

                val response = NetworkModule.api!!.getPosts(tags = finalTag, page = page)

                if (response.posts.size < 20) isLastPage = true
                if (page == 1) posts = response.posts else posts = posts + response.posts

            } catch (e: Exception) {
                e.printStackTrace()
                errorMessage = when {
                    e is retrofit2.HttpException && e.code() == 401 -> {
                        loginUsername = ""
                        loginApiKey = ""
                        encryptedPrefs.edit().remove("username").remove("apiKey").apply()
                        NetworkModule.username = ""
                        NetworkModule.apiKey = ""
                        scope.launch {
                            val errorToast = Toast.makeText(context, strings.loginFailed, Toast.LENGTH_LONG)
                            errorToast.show()
                            delay(2500)
                            errorToast.show()
                        }
                        strings.authFailed
                    }
                    e is retrofit2.HttpException && e.code() == 403 -> {
                        showCaptchaDialog = true
                        strings.error403
                    }
                    e is retrofit2.HttpException && e.code() == 422 -> strings.errorTooManyTags
                    e is retrofit2.HttpException && e.code() == 451 -> strings.error451
                    else -> strings.networkError(e.localizedMessage ?: "")
                }
            } finally { isLoading = false }
        }
    }

    val vpnPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (setupMode == "MULLVAD") {
                scope.launch {
                    vpnErrorMessage = null
                    val success = autoConnectMullvad(mullvadAccountInput, context, prefs, encryptedPrefs)
                    if (success) {
                        isVpnBypassed = true
                        prefs.edit().putBoolean("isVpnBypassed", true).apply()
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
                    prefs.edit().putBoolean("isVpnBypassed", true).apply()
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
                    prefs.edit().putString("vpnConfigText", cleanText).apply()
                    vpnConfig = Config.parse(cleanText.byteInputStream(Charsets.UTF_8))
                    vpnErrorMessage = null
                    Toast.makeText(context, strings.configSaved, Toast.LENGTH_SHORT).show()
                } else { vpnErrorMessage = strings.fileReadFailed }
            } catch (e: Exception) {
                e.printStackTrace()
                vpnErrorMessage = strings.parseFailed
            }
        }
    }

    if (vpnState != Tunnel.State.UP && !isVpnBypassed) {
        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                Image(painter = painterResource(id = R.drawable.ic_launcher_warning), contentDescription = strings.cdLogo, modifier = Modifier.size(140.dp).padding(bottom = 16.dp))
                Text(strings.hideoutSetup, color = NeonOrange, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Text(strings.vpnWarning, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))

                if (setupMode == null) {
                    Button(
                        onClick = { setupMode = "MULLVAD" },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) { Text(strings.setupMullvad, color = NeonOrange) }

                    Button(
                        onClick = { setupMode = "WIREGUARD" },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) { Text(strings.setupWireguard, color = NeonOrange) }

                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            isVpnBypassed = true
                            prefs.edit().putBoolean("isVpnBypassed", true).apply()
                        },
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = Color.Red)
                    ) { Text(strings.skipSetup, fontWeight = FontWeight.Bold) }
                } else if (setupMode == "WIREGUARD") {
                    if (vpnErrorMessage != null) Text(vpnErrorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = { filePickerLauncher.launch("*/*") }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Text(if (vpnConfig == null) strings.selectConfig else strings.changeConfig, color = NeonOrange)
                        }
                        if (vpnConfig != null) {
                            Button(
                                onClick = {
                                    val intent = VpnService.prepare(context)
                                    if (intent != null) vpnPermissionLauncher.launch(intent)
                                    else {
                                        scope.launch(Dispatchers.IO) {
                                            backend.setState(tunnel, Tunnel.State.UP, vpnConfig)
                                            context.startService(Intent(context, VpnCleanupService::class.java))
                                        }
                                        isVpnBypassed = true
                                        prefs.edit().putBoolean("isVpnBypassed", true).apply()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)
                            ) { Text(strings.enableTunnel, color = DeepBlack, fontWeight = FontWeight.Bold) }
                        }
                    }
                    TextButton(onClick = { setupMode = null }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(strings.cdBack, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else if (setupMode == "MULLVAD") {
                    if (vpnErrorMessage != null) Text(vpnErrorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 12.dp))

                    OutlinedTextField(
                        value = mullvadAccountInput,
                        onValueChange = { mullvadAccountInput = it },
                        label = { Text(strings.mullvadHint) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            val intent = VpnService.prepare(context)
                            if (intent != null) vpnPermissionLauncher.launch(intent)
                            else {
                                scope.launch {
                                    vpnErrorMessage = null
                                    val success = autoConnectMullvad(mullvadAccountInput, context, prefs, encryptedPrefs)
                                    if (success) {
                                        isVpnBypassed = true
                                        prefs.edit().putBoolean("isVpnBypassed", true).apply()
                                    } else {
                                        vpnErrorMessage = strings.loginFailedMullvad
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonOrange),
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                    ) { Text(strings.connectLabel, color = DeepBlack, fontWeight = FontWeight.Bold) }

                    TextButton(onClick = { setupMode = null }, modifier = Modifier.padding(top = 16.dp)) {
                        Text(strings.cdBack, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    } else {
        if (selectedIndex != null) {
            DetailPagerScreen(
                posts = posts, initialIndex = selectedIndex!!, prefs = prefs, encryptedPrefs = encryptedPrefs,
                onClose = { selectedIndex = null }, onLoadMore = { if (!isLastPage) page++ },
                onTagClick = { clickedTag ->
                    selectedIndex = null
                    searchInput = clickedTag
                    if (searchStack.last() != clickedTag) {
                        searchStack = searchStack + clickedTag
                        page = 1
                        isLastPage = false
                        retryTrigger++
                        scope.launch { gridState.scrollToItem(0) }
                    }
                },
                onFavoriteToggle = { postId, isFav, newScore ->
                    posts = posts.map { if (it.id == postId) it.copy(is_favorited = isFav, score = it.score.copy(total = newScore)) else it }
                }
            )
        } else {
            GalleryScreen(
                posts = posts, isLoading = isLoading, errorMessage = errorMessage,
                searchInput = searchInput, currentAppliedTag = currentTag,
                gridState = gridState,
                onSearchInputChange = { searchInput = it },
                onSearch = {
                    if (searchInput != searchStack.last()) {
                        searchStack = searchStack + searchInput
                        page = 1
                        isLastPage = false
                        retryTrigger++
                        scope.launch { gridState.scrollToItem(0) }
                    }
                },
                onRetry = { retryTrigger++ },
                onLoadMore = { if (!isLastPage) page++ },
                isLastPage = isLastPage,
                isVpnActive = (vpnState == Tunnel.State.UP),
                onVpnActionClick = {
                    if (vpnState == Tunnel.State.UP) {
                        Thread { VpnManager.stopVpnSync(context) }.start()
                    } else {
                        isVpnBypassed = false
                        prefs.edit().putBoolean("isVpnBypassed", false).apply()
                    }
                },
                onPostClick = { index -> selectedIndex = index },
                isDarkTheme = isDarkTheme, onThemeToggle = onThemeToggle,
                isNsfwEnabled = isNsfwEnabled,
                onNsfwToggle = {
                    isNsfwEnabled = !isNsfwEnabled
                    prefs.edit().putBoolean("isNsfwEnabled", isNsfwEnabled).apply()
                    page = 1
                    isLastPage = false
                    retryTrigger++
                    scope.launch { gridState.scrollToItem(0) }
                },
                loginUsername = loginUsername, loginApiKey = loginApiKey,
                onLoginSave = { user, key ->
                    loginUsername = user
                    loginApiKey = key
                    encryptedPrefs.edit().putString("username", user).putString("apiKey", key).apply()
                    page = 1
                    isLastPage = false
                    retryTrigger++
                    scope.launch { gridState.scrollToItem(0) }
                },
                onFavoritesClick = {
                    if (loginUsername.isNotBlank()) {
                        val favTag = "fav:$loginUsername"
                        searchInput = favTag
                        if (searchStack.last() != favTag) {
                            searchStack = searchStack + favTag
                            page = 1
                            isLastPage = false
                            retryTrigger++
                            scope.launch { gridState.scrollToItem(0) }
                        }
                    } else {
                        Toast.makeText(context, strings.loginFirst, Toast.LENGTH_SHORT).show()
                    }
                },
                onHotPostsClick = {
                    searchInput = ""
                    val hotTag = "#HOT#"
                    if (searchStack.last() != hotTag) {
                        searchStack = searchStack + hotTag
                        page = 1
                        isLastPage = false
                        retryTrigger++
                        scope.launch { gridState.scrollToItem(0) }
                    }
                },
                blacklistedTags = blacklistedTags,
                onBlacklistSave = { newList ->
                    blacklistedTags = newList
                    prefs.edit().putStringSet("blacklist", newList.toSet()).apply()
                    page = 1
                    isLastPage = false
                    retryTrigger++
                    scope.launch { gridState.scrollToItem(0) }
                },
                prefs = prefs,
                appLanguage = appLanguage,
                onLanguageChange = onLanguageChange
            )
        }
    }
}

@Composable
fun CloudflareBypassDialog(onSuccess: (String) -> Unit, onCancel: () -> Unit) {
    val strings = LocalStrings.current
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (isChecking) {
            kotlinx.coroutines.delay(500)
            val cookies = CookieManager.getInstance().getCookie("https://e621.net") ?: ""
            if (cookies.contains("cf_clearance")) {
                val cfCookie = cookies.split(";").map { it.trim() }.find { it.startsWith("cf_clearance=") }
                if (cfCookie != null) {
                    isChecking = false
                    onSuccess(cfCookie)
                }
            }
        }
    }

    Dialog(onDismissRequest = onCancel, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Card(
            modifier = Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.8f).systemBarsPadding().imePadding().padding(16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(DarkSurface).padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(strings.cloudflareCaptcha, style = MaterialTheme.typography.titleMedium, color = NeonOrange)
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = TextWhite) }
                }
                AndroidView(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.userAgentString = NetworkModule.DEFAULT_USER_AGENT
                            val cookieManager = CookieManager.getInstance()
                            cookieManager.setAcceptCookie(true)
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView, url: String?) {
                                    super.onPageFinished(view, url)
                                    val cookies = cookieManager.getCookie("https://e621.net") ?: ""
                                    if (cookies.contains("cf_clearance")) {
                                        val cfCookie = cookies.split(";").map { it.trim() }.find { it.startsWith("cf_clearance=") }
                                        if (cfCookie != null) {
                                            isChecking = false
                                            onSuccess(cfCookie)
                                        }
                                    } else {
                                        view.evaluateJavascript("document.title") { title ->
                                            if (title != null && title.contains("e621", ignoreCase = true) && !title.contains("Just a moment", ignoreCase = true) && !title.contains("Attention", ignoreCase = true)) {
                                                isChecking = false
                                                onSuccess("bypass")
                                            }
                                        }
                                    }
                                }
                            }
                            loadUrl("https://e621.net/")
                        }
                    }
                )
            }
        }
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    posts: List<Post>, isLoading: Boolean, errorMessage: String?,
    searchInput: String, currentAppliedTag: String,
    gridState: LazyStaggeredGridState,
    onSearchInputChange: (String) -> Unit, onSearch: () -> Unit,
    onRetry: () -> Unit, onLoadMore: () -> Unit, isLastPage: Boolean,
    isVpnActive: Boolean, onVpnActionClick: () -> Unit, onPostClick: (Int) -> Unit,
    isDarkTheme: Boolean, onThemeToggle: () -> Unit,
    isNsfwEnabled: Boolean, onNsfwToggle: () -> Unit,
    loginUsername: String, loginApiKey: String, onLoginSave: (String, String) -> Unit,
    onFavoritesClick: () -> Unit, onHotPostsClick: () -> Unit,
    blacklistedTags: List<String>, onBlacklistSave: (List<String>) -> Unit,
    prefs: android.content.SharedPreferences,
    appLanguage: String, onLanguageChange: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val screenWidth = LocalConfiguration.current.screenWidthDp
    val gridColumns = when {
        screenWidth >= 840 -> 4
        screenWidth >= 600 -> 3
        else -> 2
    }

    var isSearchFocused by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }
    var showBlacklistDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(100)
        focusManager.clearFocus(force = true)
        keyboardController?.hide()
    }

    if (showLoginDialog) {
        var tempUser by remember { mutableStateOf(loginUsername) }
        var tempKey by remember { mutableStateOf(loginApiKey) }
        AlertDialog(
            onDismissRequest = { showLoginDialog = false },
            title = { Text(strings.accountLogin, color = NeonOrange) },
            text = {
                Column {
                    Text(strings.apiKeyInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tempUser, onValueChange = { tempUser = it }, label = { Text(strings.usernameHint) }, singleLine = true)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(value = tempKey, onValueChange = { tempKey = it }, label = { Text(strings.apiKeyHint) }, singleLine = true, visualTransformation = PasswordVisualTransformation())
                }
            },
            confirmButton = { TextButton(onClick = { onLoginSave(tempUser, tempKey); showLoginDialog = false }) { Text(strings.save, color = NeonOrange) } },
            dismissButton = {
                Row {
                    if (loginUsername.isNotBlank()) TextButton(onClick = { onLoginSave("", ""); showLoginDialog = false }) { Text(strings.logout, color = Color.Red) }
                    TextButton(onClick = { showLoginDialog = false }) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showBlacklistDialog) {
        var newTag by remember { mutableStateOf("") }
        var currentList by remember { mutableStateOf(blacklistedTags) }
        AlertDialog(
            onDismissRequest = { showBlacklistDialog = false },
            title = { Text(strings.blacklistManagement, color = NeonOrange) },
            text = {
                Column {
                    Text(strings.blacklistInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(value = newTag, onValueChange = { newTag = it }, modifier = Modifier.weight(1f), label = { Text(strings.tagInputHint) }, singleLine = true)
                        IconButton(onClick = {
                            if (newTag.isNotBlank() && !currentList.contains(newTag.trim())) { currentList = currentList + newTag.trim(); newTag = "" }
                        }) { Icon(Icons.Default.Add, contentDescription = strings.cdAdd, tint = NeonOrange) }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
                        items(currentList) { tag ->
                            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(tag, color = MaterialTheme.colorScheme.onBackground)
                                IconButton(onClick = { currentList = currentList - tag }) { Icon(Icons.Default.Delete, contentDescription = strings.cdDelete, tint = Color.Red) }
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { onBlacklistSave(currentList); showBlacklistDialog = false }) { Text(strings.save, color = NeonOrange) } },
            dismissButton = { TextButton(onClick = { showBlacklistDialog = false }) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) } },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showSettingsDialog) {
        val dirPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
            uri?.let {
                context.contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                prefs.edit().putString("downloadTreeUri", it.toString()).apply()
                Toast.makeText(context, strings.settingsSaved, Toast.LENGTH_SHORT).show()
            }
        }
        val savedTreeUri = prefs.getString("downloadTreeUri", "") ?: ""
        var currentCacheSize by remember(showSettingsDialog) { mutableStateOf(getCacheSizeString(context)) }

        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = { Text(strings.appSettings, color = NeonOrange) },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onThemeToggle() }.padding(vertical = 8.dp)) {
                        Text(strings.useDarkTheme, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = NeonOrange, checkedTrackColor = NeonOrange.copy(alpha = 0.5f)))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                        Text(strings.appLanguageLabel, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(strings.languageKo, fontWeight = if (appLanguage == "ko") FontWeight.Bold else FontWeight.Normal, color = if (appLanguage == "ko") NeonOrange else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onLanguageChange("ko") }.padding(8.dp))
                            Text("|", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(strings.languageEn, fontWeight = if (appLanguage == "en") FontWeight.Bold else FontWeight.Normal, color = if (appLanguage == "en") NeonOrange else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.clickable { onLanguageChange("en") }.padding(8.dp))
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text(strings.downloadLocation, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                        OutlinedButton(onClick = { dirPickerLauncher.launch(null) }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (savedTreeUri.isEmpty()) strings.defaultFolder else strings.customFolder, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    Text(strings.cacheManagement, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${strings.cacheSizeLabel}$currentCacheSize", color = MaterialTheme.colorScheme.onSurface)
                        OutlinedButton(onClick = {
                            context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                            currentCacheSize = getCacheSizeString(context)
                            Toast.makeText(context, strings.cacheCleared, Toast.LENGTH_SHORT).show()
                        }) {
                            Text(strings.clearCache, color = Color.Red)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Canned-F0xy/HIDEOUT-e621_Android_Client"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonOrange)
                    ) {
                        Icon(Icons.Default.OpenInNew, contentDescription = strings.cdOpenBrowser, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(strings.devGithub, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSettingsDialog = false }) { Text(strings.close, color = NeonOrange) }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(300.dp),
                drawerContainerColor = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp)) {
                    Image(painter = painterResource(id = R.drawable.ic_launcher2), contentDescription = strings.cdLogo, modifier = Modifier.size(64.dp).clip(RoundedCornerShape(12.dp)))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("HIDEOUT", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, letterSpacing = 2.sp), color = NeonOrange)
                    Text("Ver. 2026-07-29", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text(if (loginUsername.isNotBlank()) strings.changeLoginInfo(loginUsername) else strings.accountLoginMenu) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; showLoginDialog = true },
                    icon = { Icon(Icons.Default.Person, null, tint = NeonOrange) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(strings.viewFavorites) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onFavoritesClick() },
                    icon = { Icon(Icons.Default.Star, null, tint = NeonOrange) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(strings.weeklyHot) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onHotPostsClick() },
                    icon = { Icon(Icons.Default.TrendingUp, null, tint = NeonOrange) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(strings.manageBlacklist) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; showBlacklistDialog = true },
                    icon = { Icon(Icons.Default.Block, null, tint = NeonOrange) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                NavigationDrawerItem(
                    label = { Text(if (isNsfwEnabled) strings.r18ModeOn else strings.r18ModeOff, color = if(isNsfwEnabled) Color.Red else MaterialTheme.colorScheme.onSurface) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; onNsfwToggle() },
                    icon = { Icon(Icons.Default.Warning, null, tint = if(isNsfwEnabled) Color.Red else NeonOrange) },
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                NavigationDrawerItem(
                    label = { Text(strings.appSettingsMenu) },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() }; showSettingsDialog = true },
                    icon = { Icon(Icons.Default.Settings, null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("HIDEOUT", color = NeonOrange, fontWeight = FontWeight.Black) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, strings.cdOpenMenu, tint = NeonOrange)
                        }
                    },
                    actions = {
                        TextButton(onClick = onVpnActionClick) {
                            Text(if (isVpnActive) strings.disableWireGuard else strings.enableWireGuard, color = if (isVpnActive) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize()
                    .pointerInput(Unit) { detectTapGestures(onTap = { focusManager.clearFocus(force = true); keyboardController?.hide() }) }
                    .padding(innerPadding)
            ) {
                Box(modifier = Modifier.size(0.dp).focusRequester(focusRequester).focusable())

                var expanded by remember { mutableStateOf(false) }
                var tagSuggestions by remember { mutableStateOf<List<AutocompleteTag>>(emptyList()) }

                LaunchedEffect(searchInput, isSearchFocused) {
                    if (!isSearchFocused) { expanded = false; return@LaunchedEffect }
                    val lastWord = searchInput.split(" ").lastOrNull() ?: ""
                    if (lastWord.length >= 2) {
                        delay(300)
                        try {
                            tagSuggestions = NetworkModule.api!!.getAutocomplete(lastWord)
                            expanded = tagSuggestions.isNotEmpty()
                        } catch(e: Exception) { expanded = false }
                    } else { expanded = false }
                }

                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    TextField(
                        value = searchInput, onValueChange = onSearchInputChange,
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).onFocusChanged { focusState -> isSearchFocused = focusState.isFocused },
                        placeholder = { Text(strings.searchTags, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = strings.cdSearch, tint = NeonOrange) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { expanded = false; onSearch(); focusManager.clearFocus(force = true); keyboardController?.hide() }),
                        colors = TextFieldDefaults.colors(focusedIndicatorColor = Color.Transparent, unfocusedIndicatorColor = Color.Transparent, focusedContainerColor = MaterialTheme.colorScheme.surface, unfocusedContainerColor = MaterialTheme.colorScheme.surface)
                    )

                    if (expanded && tagSuggestions.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            LazyColumn(modifier = Modifier.heightIn(max = 240.dp)) {
                                items(tagSuggestions) { tag ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            val words = searchInput.split(" ").dropLast(1).toMutableList()
                                            if (words.isNotEmpty() && words[0].isBlank()) words.clear()
                                            words.add(tag.name)
                                            onSearchInputChange(words.joinToString(" ") + " ")
                                            expanded = false
                                        }.padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(tag.name, color = MaterialTheme.colorScheme.onBackground)
                                        Text("${tag.post_count}", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                                    }
                                    if (tag != tagSuggestions.last()) Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
                                }
                            }
                        }
                    }
                }

                val titleText = when {
                    currentAppliedTag == "#HOT#" -> strings.weeklyHot
                    currentAppliedTag.isBlank() -> strings.latestPosts
                    else -> strings.searchResults(currentAppliedTag)
                }
                Text(text = titleText, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(start = 20.dp, end = 20.dp, bottom = 8.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(gridColumns),
                        state = gridState,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalItemSpacing = 12.dp,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        itemsIndexed(posts, key = { _, post -> post.id }) { index, post ->
                            if (index >= posts.size - 1 && !isLoading && errorMessage == null && !isLastPage) LaunchedEffect(Unit) { onLoadMore() }
                            val cachedFile = getCachedFile(context, post.file.md5, post.file.ext)
                            val imageModel = cachedFile ?: post.preview.url

                            imageModel?.let { imageUrl ->
                                val imageRequest = remember(imageUrl) {
                                    ImageRequest.Builder(context).data(imageUrl).crossfade(200).build()
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { focusManager.clearFocus(force = true); keyboardController?.hide(); onPostClick(index) },
                                    shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(0.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                ) {
                                    Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp)) {
                                        AsyncImage(model = imageRequest, contentDescription = "${strings.idLabel(post.id)}", modifier = Modifier.fillMaxWidth(), contentScale = ContentScale.FillWidth)
                                        Box(modifier = Modifier.align(Alignment.BottomStart).padding(6.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                            Text(post.file.ext.uppercase(), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                        if ((post.file.ext == "webm" || post.file.ext == "mp4") && post.duration != null && post.duration > 0) {
                                            Box(modifier = Modifier.align(Alignment.BottomEnd).padding(6.dp).background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                                                Text(formatDuration(post.duration), color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (isLoading && posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = NeonOrange, modifier = Modifier.size(48.dp))
                        }
                    } else if (errorMessage != null && posts.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = strings.cdError, tint = Color.Red, modifier = Modifier.size(48.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(strings.loadFailed, color = NeonOrange, style = MaterialTheme.typography.titleMedium)
                                Text(errorMessage!!, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))
                                Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = NeonOrange)) { Text(strings.retry, color = DeepBlack, fontWeight = FontWeight.Bold) }
                            }
                        }
                    } else if (posts.isEmpty() && !isLoading) {
                        Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.Warning, contentDescription = strings.cdNoResults, tint = NeonOrange, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(strings.noResults, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailPagerScreen(posts: List<Post>, initialIndex: Int, prefs: android.content.SharedPreferences, encryptedPrefs: android.content.SharedPreferences, onClose: () -> Unit, onLoadMore: () -> Unit, onTagClick: (String) -> Unit, onFavoriteToggle: (Int, Boolean, Int) -> Unit) {
    val pagerState = rememberPagerState(initialPage = initialIndex, pageCount = { posts.size })
    LaunchedEffect(pagerState.currentPage) { if (pagerState.currentPage >= posts.size - 3) onLoadMore() }
    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { pageIndex ->
        val isActive = pagerState.currentPage == pageIndex
        DetailScreen(post = posts[pageIndex], isActivePage = isActive, prefs = prefs, encryptedPrefs = encryptedPrefs, onClose = onClose, onTagClick = onTagClick, onFavoriteToggle = onFavoriteToggle)
    }
}

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(post: Post, isActivePage: Boolean, prefs: android.content.SharedPreferences, encryptedPrefs: android.content.SharedPreferences, onClose: () -> Unit, onTagClick: (String) -> Unit, onFavoriteToggle: (Int, Boolean, Int) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val strings = LocalStrings.current
    var isFullScreen by remember { mutableStateOf(false) }
    var isFavorited by remember(post.id, post.is_favorited) { mutableStateOf(post.is_favorited) }
    var currentScore by remember(post.id, post.score.total) { mutableIntStateOf(post.score.total) }

    val cachedFile = getCachedFile(context, post.file.md5, post.file.ext)

    val gifEnabledLoader = remember { ImageLoader.Builder(context).okHttpClient(NetworkModule.client).components { if (SDK_INT >= 28) add(ImageDecoderDecoder.Factory()) else add(GifDecoder.Factory()) }.build() }

    val exoPlayer = remember(post.id) {
        if ((post.file.ext == "webm" || post.file.ext == "mp4") && post.file.url != null) {
            ExoPlayer.Builder(context).build().apply {
                val mediaUri = if (cachedFile != null) Uri.fromFile(cachedFile) else Uri.parse(post.file.url)
                setMediaItem(MediaItem.fromUri(mediaUri))
                prepare()
                playWhenReady = false
                repeatMode = Player.REPEAT_MODE_ALL
            }
        } else null
    }

    LaunchedEffect(isActivePage) {
        if (!isActivePage) {
            exoPlayer?.pause()
            isFullScreen = false
        }
    }

    DisposableEffect(exoPlayer) {
        onDispose { exoPlayer?.release() }
    }

    if (isFullScreen && post.file.url != null) {
        Dialog(onDismissRequest = { isFullScreen = false }, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
            if (post.file.ext == "webm" || post.file.ext == "mp4") {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                    if (exoPlayer != null) {
                        VideoPlayer(exoPlayer = exoPlayer)
                    }
                    IconButton(onClick = { isFullScreen = false }, modifier = Modifier.align(Alignment.TopEnd).systemBarsPadding().padding(16.dp)) {
                        Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = Color.White)
                    }
                }
            } else {
                ZoomableImage(
                    imageUrl = if (cachedFile != null) cachedFile.absolutePath else post.file.url,
                    imageLoader = gifEnabledLoader,
                    onClose = { isFullScreen = false }
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings.idLabel(post.id), style = MaterialTheme.typography.titleMedium, color = NeonOrange) },
                navigationIcon = { IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, contentDescription = strings.cdBack, tint = MaterialTheme.colorScheme.onBackground) } },
                actions = {
                    IconButton(onClick = {
                        scope.launch {
                            try {
                                if (isFavorited) {
                                    val response = NetworkModule.api!!.removeFavorite(post.id)
                                    if (response.isSuccessful || response.code() == 404) {
                                        isFavorited = false
                                        currentScore -= 1
                                        onFavoriteToggle(post.id, false, currentScore)
                                        Toast.makeText(context, strings.favRemoved, Toast.LENGTH_SHORT).show()
                                    } else { Toast.makeText(context, strings.favRemoveFailed(response.code()), Toast.LENGTH_SHORT).show() }
                                } else {
                                    val response = NetworkModule.api!!.addFavorite(post.id)
                                    if (response.isSuccessful || response.code() == 422) {
                                        isFavorited = true
                                        currentScore += 1
                                        onFavoriteToggle(post.id, true, currentScore)
                                        Toast.makeText(context, strings.favAdded, Toast.LENGTH_SHORT).show()
                                    } else if (response.code() == 401 || response.code() == 403) { Toast.makeText(context, strings.favAddFailedLogin, Toast.LENGTH_SHORT).show() } else { Toast.makeText(context, strings.favAddFailed(response.code()), Toast.LENGTH_SHORT).show() }
                                }
                            } catch (e: Exception) { Toast.makeText(context, strings.networkErrorOccurred, Toast.LENGTH_SHORT).show() }
                        }
                    }) { Icon(imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = strings.cdFavoriteToggle, tint = NeonOrange) }
                    if (post.file.url != null) {
                        IconButton(onClick = {
                            val treeUriStr = prefs.getString("downloadTreeUri", "") ?: ""
                            downloadMediaWithRange(context, post.file.url, "Hideout_${post.id}.${post.file.ext}", post.file.md5, post.file.ext, treeUriStr, encryptedPrefs, strings)
                        }) { Icon(Icons.Default.Download, contentDescription = strings.cdDownload, tint = NeonOrange) }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 300.dp, max = 600.dp).background(Color.Black), contentAlignment = Alignment.Center) {
                if (post.file.ext == "webm" || post.file.ext == "mp4") {
                    if (post.file.url != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (!isFullScreen && exoPlayer != null) {
                                VideoPlayer(exoPlayer = exoPlayer)
                            }
                            IconButton(onClick = { isFullScreen = true }, modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)) {
                                Icon(Icons.Default.Fullscreen, contentDescription = strings.cdFullscreen, tint = Color.White, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                } else if (post.file.url != null) {
                    val detailImageRequest = remember(cachedFile, post.file.url) {
                        ImageRequest.Builder(context).data(cachedFile ?: post.file.url).crossfade(200).build()
                    }
                    AsyncImage(
                        model = detailImageRequest,
                        imageLoader = gifEnabledLoader,
                        contentDescription = "Full Image",
                        modifier = Modifier.fillMaxSize().clickable { isFullScreen = true },
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(strings.fileLabel(post.file.ext.uppercase()), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(strings.scoreLabel(currentScore), style = MaterialTheme.typography.titleMedium, color = NeonOrange)
                }
                Spacer(modifier = Modifier.height(16.dp))

                post.getAllRelatedIdsQuery()?.let { query ->
                    Button(
                        onClick = { onTagClick(query) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    ) { Text(strings.viewAllRelated, color = NeonOrange) }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!post.description.isNullOrBlank()) {
                    Text(strings.descLabel, style = MaterialTheme.typography.titleSmall, color = NeonOrange)
                    Text(post.description, color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.padding(vertical = 4.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                }
                TagSection(title = strings.artistLabel, tags = post.tags.artist, color = Color(0xFFFF5252), onTagClick = onTagClick)
                TagSection(title = strings.copyrightLabel, tags = post.tags.copyright, color = Color(0xFFE040FB), onTagClick = onTagClick)
                TagSection(title = strings.characterLabel, tags = post.tags.character, color = Color(0xFF69F0AE), onTagClick = onTagClick)
                TagSection(title = strings.generalLabel, tags = post.tags.general, color = Color(0xFF40C4FF), onTagClick = onTagClick)

                if (post.sources.isNotEmpty()) {
                    Text(strings.sourcesLabel, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        post.sources.forEach { sourceUrl ->
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(Color(0xFF40C4FF).copy(alpha = 0.15f)).clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(sourceUrl))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, strings.cannotOpenLink, Toast.LENGTH_SHORT).show()
                                }
                            }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(sourceUrl, color = Color(0xFF40C4FF), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun ZoomableImage(imageUrl: String, imageLoader: ImageLoader, onClose: () -> Unit) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val strings = LocalStrings.current

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
            .pointerInput(Unit) { detectTapGestures(onDoubleTap = { scale = if (scale > 1f) 1f else 2.5f; offset = Offset.Zero }) }
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = (scale * zoom).coerceIn(1f, 5f)
                    if (scale > 1f) offset += pan else offset = Offset.Zero
                }
            }
    ) {
        AsyncImage(model = if (imageUrl.startsWith("/")) File(imageUrl) else imageUrl, imageLoader = imageLoader, contentDescription = "Zoomable Image", modifier = Modifier.fillMaxSize().graphicsLayer(scaleX = scale, scaleY = scale, translationX = offset.x, translationY = offset.y), contentScale = ContentScale.Fit)
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopEnd).systemBarsPadding().padding(16.dp)) { Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = Color.White) }
    }
}

@kotlin.OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagSection(title: String, tags: List<String>, color: Color, onTagClick: (String) -> Unit) {
    if (tags.isNotEmpty()) {
        Text(title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onBackground)
        FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            tags.forEach { tag ->
                Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.15f)).clickable { onTagClick(tag) }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                    Text(tag, color = color, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(exoPlayer: ExoPlayer) {
    val context = LocalContext.current
    val playerView = remember {
        androidx.media3.ui.PlayerView(context).apply {
            setBackgroundColor(android.graphics.Color.BLACK)
            useController = true
        }
    }

    DisposableEffect(exoPlayer) {
        playerView.player = exoPlayer
        onDispose {
            playerView.player = null
        }
    }

    AndroidView(
        factory = { playerView },
        update = { view ->
            view.post {
                view.requestLayout()
                view.invalidate()
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun downloadMediaWithRange(context: Context, url: String, fileName: String, md5: String?, ext: String, treeUriStr: String, encryptedPrefs: android.content.SharedPreferences, strings: AppStrings) {
    val cfClearanceCookie = encryptedPrefs.getString("cfClearance", "") ?: ""

    if (!md5.isNullOrBlank()) {
        val cached = getCachedFile(context, md5, ext)
        if (cached != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    if (treeUriStr.isEmpty()) {
                        val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val hideoutDir = File(targetDir, "HIDEOUT")
                        if (!hideoutDir.exists()) hideoutDir.mkdirs()
                        val destFile = File(hideoutDir, fileName)
                        cached.copyTo(destFile, overwrite = true)
                        val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                        scanIntent.data = Uri.fromFile(destFile)
                        context.sendBroadcast(scanIntent)
                    } else {
                        val treeUri = Uri.parse(treeUriStr)
                        val documentId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
                        val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                        val newDocUri = android.provider.DocumentsContract.createDocument(context.contentResolver, docUri, "*/*", fileName)
                        if (newDocUri != null) {
                            context.contentResolver.openOutputStream(newDocUri)?.use { out ->
                                cached.inputStream().use { it.copyTo(out) }
                            }
                        }
                    }
                    withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadComplete, Toast.LENGTH_SHORT).show() }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadFailed(e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show() }
                }
            }
            return
        }
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadStarting, Toast.LENGTH_SHORT).show() }

            val tempFile = File(context.cacheDir, "temp_$fileName")
            var downloadedBytes = if (tempFile.exists()) tempFile.length() else 0L

            val connection = URL(url).openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", NetworkModule.DEFAULT_USER_AGENT)
            if (cfClearanceCookie.isNotBlank() && cfClearanceCookie != "bypass") {
                connection.setRequestProperty("Cookie", cfClearanceCookie)
            }
            if (downloadedBytes > 0) {
                connection.setRequestProperty("Range", "bytes=$downloadedBytes-")
            }
            connection.connect()

            val responseCode = connection.responseCode
            val inputStream = if (responseCode == HttpURLConnection.HTTP_PARTIAL) {
                connection.inputStream
            } else {
                downloadedBytes = 0L
                tempFile.delete()
                connection.inputStream
            }

            val outputStream = FileOutputStream(tempFile, downloadedBytes > 0)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()

            if (treeUriStr.isEmpty()) {
                val targetDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val hideoutDir = File(targetDir, "HIDEOUT")
                if (!hideoutDir.exists()) hideoutDir.mkdirs()
                val destFile = File(hideoutDir, fileName)
                tempFile.copyTo(destFile, overwrite = true)
                val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                scanIntent.data = Uri.fromFile(destFile)
                context.sendBroadcast(scanIntent)
            } else {
                val treeUri = Uri.parse(treeUriStr)
                val documentId = android.provider.DocumentsContract.getTreeDocumentId(treeUri)
                val docUri = android.provider.DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
                val newDocUri = android.provider.DocumentsContract.createDocument(context.contentResolver, docUri, "*/*", fileName)
                if (newDocUri != null) {
                    context.contentResolver.openOutputStream(newDocUri)?.use { out ->
                        tempFile.inputStream().use { it.copyTo(out) }
                    }
                }
            }

            if (!md5.isNullOrBlank()) {
                val cacheDir = File(context.cacheDir, "e621_media_cache")
                if (!cacheDir.exists()) cacheDir.mkdirs()
                val cacheFile = File(cacheDir, "$md5.$ext")
                tempFile.copyTo(cacheFile, overwrite = true)
            }

            withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadComplete, Toast.LENGTH_SHORT).show() }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { Toast.makeText(context, strings.downloadFailed(e.localizedMessage ?: ""), Toast.LENGTH_SHORT).show() }
        }
    }
}

fun getCacheSizeString(context: Context): String {
    return formatByteSize(getCacheDirSize(context))
}
