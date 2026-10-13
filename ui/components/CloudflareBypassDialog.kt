package com.CannedF0xy.hideout.ui.components

import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.NetworkModule
import com.CannedF0xy.hideout.ui.theme.DarkSurface
import com.CannedF0xy.hideout.ui.theme.TextWhite
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map

@Composable
fun CloudflareBypassDialog(onSuccess: (String) -> Unit, onCancel: () -> Unit) {
    val strings = LocalStrings.current
    var isChecking by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        while (isChecking) {
            delay(500)
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
                    Text(strings.cloudflareCaptcha, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                    IconButton(onClick = onCancel) { Icon(Icons.Default.Close, contentDescription = strings.cdClose, tint = TextWhite) }
                }
                AndroidView(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.userAgentString = NetworkModule.DEFAULT_USER_AGENT
                            settings.allowFileAccess = false
                            settings.allowContentAccess = false
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
                                            if (title != null && title.contains("e621", ignoreCase = true) &&
                                                !title.contains("Just a moment", ignoreCase = true) &&
                                                !title.contains("Attention", ignoreCase = true)
                                            ) {
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
