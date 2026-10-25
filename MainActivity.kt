package com.CannedF0xy.hideout

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.data.ViewHistoryStore
import com.CannedF0xy.hideout.i18n.EnStrings
import com.CannedF0xy.hideout.i18n.KoStrings
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.notification.NotificationHelper
import com.CannedF0xy.hideout.security.AppLockManager
import com.CannedF0xy.hideout.security.LockScreen
import com.CannedF0xy.hideout.ui.MainApp
import com.CannedF0xy.hideout.ui.navigation.PendingNavigation
import com.CannedF0xy.hideout.ui.theme.HideoutDarkTheme
import com.CannedF0xy.hideout.ui.theme.HideoutLightTheme
import com.CannedF0xy.hideout.vpn.VpnCleanupService
import com.CannedF0xy.hideout.vpn.VpnManager
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : FragmentActivity() {

    @Volatile
    private var incognitoActive = false

    private fun clearCacheInBackground() {
        val dir = cacheDir
        Thread { dir.listFiles()?.forEach { it.deleteRecursively() } }.start()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onDestroy() {
        super.onDestroy()
        if (incognitoActive && isFinishing) clearCacheInBackground()
        Thread { VpnManager.stopVpnSync(this) }.start()
        stopService(Intent(this, VpnCleanupService::class.java))
    }

    override fun onStart() {
        super.onStart()
        AppLockManager.onAppForegrounded()
    }

    override fun onStop() {
        super.onStop()
        ViewHistoryStore.flush()
        AppLockManager.onAppBackgrounded()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleOpenTagIntent(intent)
    }

    private fun handleOpenTagIntent(intent: Intent?) {
        val tag = intent?.getStringExtra(NotificationHelper.EXTRA_OPEN_TAG)
        if (!tag.isNullOrBlank()) {
            PendingNavigation.openTag = tag
            intent?.removeExtra(NotificationHelper.EXTRA_OPEN_TAG)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val startupPrefs = AppPreferences(applicationContext)
        val snapshot = runBlocking { startupPrefs.readStartupSnapshot() }
        val initialLockEnabled = snapshot.isAppLockEnabled
        val initialPinRecord = snapshot.pinRecord
        val initialSecureScreen = snapshot.isSecureScreen
        incognitoActive = snapshot.isIncognito
        if (incognitoActive && savedInstanceState == null) clearCacheInBackground()
        val initialStrings = if (snapshot.appLanguage == "en") EnStrings else KoStrings

        if (initialSecureScreen) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        AppLockManager.isEnabled = initialLockEnabled && initialPinRecord.isNotBlank()
        if (savedInstanceState == null) AppLockManager.lockNow()

        NotificationHelper.ensureChannels(this, initialStrings.notificationChannelDownloads, initialStrings.notificationChannelFollow)
        handleOpenTagIntent(intent)

        setContent {
            val context = LocalContext.current
            val appPrefs = remember { AppPreferences(context) }

            val isDarkTheme by appPrefs.isDarkTheme.collectAsState(initial = snapshot.isDarkTheme)
            val appLanguage by appPrefs.appLanguage.collectAsState(initial = snapshot.appLanguage)
            val isAppLockEnabled by appPrefs.isAppLockEnabled.collectAsState(initial = initialLockEnabled)
            val pinRecord by appPrefs.pinRecord.collectAsState(initial = initialPinRecord)
            val isBiometricEnabled by appPrefs.isBiometricEnabled.collectAsState(initial = false)
            val isSecureScreen by appPrefs.isSecureScreen.collectAsState(initial = initialSecureScreen)
            val isIncognito by appPrefs.isIncognito.collectAsState(initial = incognitoActive)

            LaunchedEffect(isIncognito) { incognitoActive = isIncognito }

            LaunchedEffect(isAppLockEnabled, pinRecord) {
                AppLockManager.isEnabled = isAppLockEnabled && pinRecord.isNotBlank()
                if (!AppLockManager.isEnabled) AppLockManager.unlock()
            }

            LaunchedEffect(isSecureScreen) {
                if (isSecureScreen) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
            }

            val currentStrings = if (appLanguage == "en") EnStrings else KoStrings
            val colorScheme = if (isDarkTheme) HideoutDarkTheme else HideoutLightTheme

            CompositionLocalProvider(LocalStrings provides currentStrings) {
                MaterialTheme(colorScheme = colorScheme) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Box(modifier = Modifier.fillMaxSize().systemBarsPadding().imePadding()) {
                                MainApp(
                                    appPrefs = appPrefs,
                                    isDarkTheme = isDarkTheme,
                                    appLanguage = appLanguage
                                )
                            }
                            if (AppLockManager.isLocked && isAppLockEnabled && pinRecord.isNotBlank()) {
                                LockScreen(pinRecord = pinRecord, isBiometricEnabled = isBiometricEnabled)
                            }
                        }
                    }
                }
            }
        }
    }
}
