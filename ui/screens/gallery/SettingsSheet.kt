package com.CannedF0xy.hideout.ui.screens.gallery

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.download.VaultStore
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.util.getCacheSizeString

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    isDarkTheme: Boolean,
    onThemeToggle: () -> Unit,
    isAutoPlay: Boolean,
    onAutoPlayToggle: () -> Unit,
    appLanguage: String,
    onLanguageChange: (String) -> Unit,
    savedTreeUri: String,
    onPickDownloadDir: () -> Unit,
    onExportSettings: () -> Unit,
    onImportSettings: () -> Unit,
    onDismiss: () -> Unit,
    appPrefs: AppPreferences? = null
) {
    val context = LocalContext.current
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var currentCacheSize by remember { mutableStateOf(getCacheSizeString(context)) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp).verticalScroll(rememberScrollState())) {
            Text(strings.appSettings, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onThemeToggle() }.padding(vertical = 8.dp)) {
                Text(strings.useDarkTheme, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Switch(checked = isDarkTheme, onCheckedChange = { onThemeToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            }

            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onAutoPlayToggle() }.padding(vertical = 8.dp)) {
                Text(strings.autoPlayVideo, modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Switch(checked = isAutoPlay, onCheckedChange = { onAutoPlayToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary, checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)))
            }

            Spacer(Modifier.height(16.dp))
            Text(strings.appLanguageLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Card(modifier = Modifier.weight(1f).clickable { onLanguageChange("ko") }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (appLanguage == "ko") MaterialTheme.colorScheme.primary.copy(alpha=0.1f) else MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(strings.languageKo, textAlign = TextAlign.Center, fontWeight = if (appLanguage == "ko") FontWeight.Bold else FontWeight.Normal, color = if (appLanguage == "ko") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(16.dp))
                }
                Spacer(Modifier.width(12.dp))
                Card(modifier = Modifier.weight(1f).clickable { onLanguageChange("en") }, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = if (appLanguage == "en") MaterialTheme.colorScheme.primary.copy(alpha=0.1f) else MaterialTheme.colorScheme.surfaceVariant)) {
                    Text(strings.languageEn, textAlign = TextAlign.Center, fontWeight = if (appLanguage == "en") FontWeight.Bold else FontWeight.Normal, color = if (appLanguage == "en") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.fillMaxWidth().padding(16.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(strings.downloadLocation, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Box(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                OutlinedButton(onClick = { onPickDownloadDir() }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                    Text(if (savedTreeUri.isEmpty()) strings.defaultFolder else if (savedTreeUri == VaultStore.TARGET) strings.vaultMenu else strings.customFolder, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(24.dp))
            Text(strings.cacheManagement, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("${strings.cacheSizeLabel}$currentCacheSize", color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Button(onClick = {
                    context.cacheDir.listFiles()?.forEach { it.deleteRecursively() }
                    currentCacheSize = getCacheSizeString(context)
                    Toast.makeText(context, strings.cacheCleared, Toast.LENGTH_SHORT).show()
                }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha=0.15f)), shape = RoundedCornerShape(12.dp)) {
                    Text(strings.clearCache, color = Color.Red, fontWeight = FontWeight.Bold)
                }
            }

            if (appPrefs != null) {
                AdvancedSettingsSection(appPrefs = appPrefs)
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = { onExportSettings() }, modifier = Modifier.weight(1f).padding(end = 6.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(strings.exportSettings, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                }
                OutlinedButton(onClick = { onImportSettings() }, modifier = Modifier.weight(1f).padding(start = 6.dp), shape = RoundedCornerShape(12.dp)) {
                    Text(strings.importSettings, color = MaterialTheme.colorScheme.primary, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 4.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Canned-F0xy"))) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(Icons.Default.OpenInNew, contentDescription = strings.cdOpenBrowser, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(strings.devGithub, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 4.dp))
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}
