package com.CannedF0xy.hideout.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.network.GithubRelease
import com.CannedF0xy.hideout.network.UpdateInstaller
import java.io.File
import kotlinx.coroutines.launch

@Composable
fun UpdateDialog(release: GithubRelease, onDismiss: () -> Unit) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val apkAsset = remember(release) { release.apkAsset() }
    var isDownloading by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    var downloadedApk by remember { mutableStateOf<File?>(null) }

    fun openInBrowser() {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(release.html_url)))
        onDismiss()
    }

    fun installDownloaded(apk: File) {
        if (!UpdateInstaller.canInstall(context)) {
            Toast.makeText(context, strings.updateInstallPermission, Toast.LENGTH_LONG).show()
            UpdateInstaller.openInstallPermissionSettings(context)
            return
        }
        UpdateInstaller.install(context, apk)
    }

    AlertDialog(
        onDismissRequest = { if (!isDownloading) onDismiss() },
        title = { Text(strings.updateAvailable, color = MaterialTheme.colorScheme.primary) },
        text = {
            Column {
                Text(strings.updateMessage(release.tag_name), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (isDownloading) {
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(strings.updateDownloading((progress * 100).toInt()), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else if (apkAsset == null) {
                    Spacer(Modifier.height(8.dp))
                    Text(strings.updateNoApk, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !isDownloading,
                onClick = {
                    val ready = downloadedApk
                    val url = apkAsset?.browser_download_url
                    when {
                        ready != null -> installDownloaded(ready)
                        url == null -> openInBrowser()
                        else -> {
                            isDownloading = true
                            progress = 0f
                            scope.launch {
                                try {
                                    val apk = UpdateInstaller.download(context, url) { progress = it }
                                    downloadedApk = apk
                                    isDownloading = false
                                    installDownloaded(apk)
                                } catch (e: Exception) {
                                    isDownloading = false
                                    Toast.makeText(context, strings.updateDownloadFailed, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    if (downloadedApk != null) strings.updateInstallButton else strings.updateButton,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isDownloading) {
                if (apkAsset != null) {
                    TextButton(onClick = { openInBrowser() }) { Text(strings.updateOpenBrowser, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                TextButton(onClick = { onDismiss() }) { Text(strings.laterButton, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}
