package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import com.CannedF0xy.hideout.util.MediaQuality
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.download.VaultStore
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.security.AppLockManager
import com.CannedF0xy.hideout.security.BiometricHelper
import com.CannedF0xy.hideout.security.PinSetupDialog
import kotlinx.coroutines.launch

@Composable
fun AdvancedSettingsSection(appPrefs: AppPreferences) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isAppLockEnabled by appPrefs.isAppLockEnabled.collectAsState(initial = false)
    val isBiometricEnabled by appPrefs.isBiometricEnabled.collectAsState(initial = false)
    val isSecureScreen by appPrefs.isSecureScreen.collectAsState(initial = false)
    val imageQuality by appPrefs.imageQuality.collectAsState(initial = MediaQuality.DEFAULT)
    val videoQuality by appPrefs.videoQuality.collectAsState(initial = MediaQuality.DEFAULT)
    val downloadTreeUri by appPrefs.downloadTreeUri.collectAsState(initial = "")
    val isKillSwitchEnabled by appPrefs.isKillSwitchEnabled.collectAsState(initial = false)
    val isDataSaverEnabled by appPrefs.isDataSaverEnabled.collectAsState(initial = false)
    val isAutoReconnectEnabled by appPrefs.isAutoReconnectEnabled.collectAsState(initial = true)
    var showPinSetup by remember { mutableStateOf(false) }
    val biometricAvailable = remember { BiometricHelper.isAvailable(context) }

    if (showPinSetup) {
        PinSetupDialog(
            onPinCreated = { pin ->
                showPinSetup = false
                val record = AppLockManager.createRecord(pin)
                scope.launch { appPrefs.setAppLock(true, record) }
            },
            onDismiss = { showPinSetup = false }
        )
    }

    Column {
        Spacer(Modifier.height(24.dp))
        Text(strings.mediaSettingsLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        QualitySelectorRow(
            title = strings.imageQualityTitle,
            description = strings.imageQualityDescription,
            selected = imageQuality,
            onSelect = { quality -> scope.launch { appPrefs.setImageQuality(quality) } }
        )
        QualitySelectorRow(
            title = strings.videoQualityTitle,
            description = strings.videoQualityDescription,
            selected = videoQuality,
            onSelect = { quality -> scope.launch { appPrefs.setVideoQuality(quality) } }
        )
        SettingSwitchRow(
            title = strings.dataSaverTitle,
            description = strings.dataSaverDescription,
            checked = isDataSaverEnabled,
            onToggle = { scope.launch { appPrefs.setDataSaver(!isDataSaverEnabled) } }
        )
        SettingSwitchRow(
            title = strings.vaultStorageTitle,
            description = strings.vaultStorageDescription,
            checked = downloadTreeUri == VaultStore.TARGET,
            onToggle = {
                scope.launch { appPrefs.setDownloadTreeUri(if (downloadTreeUri == VaultStore.TARGET) "" else VaultStore.TARGET) }
            }
        )

        FileNameTemplateSetting(appPrefs = appPrefs)

        Spacer(Modifier.height(16.dp))
        Text(strings.vpnSettingsLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingSwitchRow(
            title = strings.killSwitchTitle,
            description = strings.killSwitchDescription,
            checked = isKillSwitchEnabled,
            onToggle = { scope.launch { appPrefs.setKillSwitch(!isKillSwitchEnabled) } }
        )
        SettingSwitchRow(
            title = strings.autoReconnectTitle,
            description = strings.autoReconnectDescription,
            checked = isAutoReconnectEnabled,
            onToggle = { scope.launch { appPrefs.setAutoReconnect(!isAutoReconnectEnabled) } }
        )

        Spacer(Modifier.height(16.dp))
        Text(strings.securitySettingsLabel, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        SettingSwitchRow(
            title = strings.appLockTitle,
            description = strings.appLockDescription,
            checked = isAppLockEnabled,
            onToggle = {
                if (isAppLockEnabled) scope.launch { appPrefs.setAppLock(false, null) } else showPinSetup = true
            }
        )
        if (isAppLockEnabled) {
            if (biometricAvailable) {
                SettingSwitchRow(
                    title = strings.biometricTitle,
                    description = null,
                    checked = isBiometricEnabled,
                    onToggle = { scope.launch { appPrefs.setBiometricEnabled(!isBiometricEnabled) } }
                )
            }
            OutlinedButton(
                onClick = { showPinSetup = true },
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.changePin, color = MaterialTheme.colorScheme.onSurface)
            }
        }
        SettingSwitchRow(
            title = strings.secureScreenTitle,
            description = strings.secureScreenDescription,
            checked = isSecureScreen,
            onToggle = { scope.launch { appPrefs.setSecureScreen(!isSecureScreen) } }
        )
    }
}

@Composable
private fun SettingSwitchRow(title: String, description: String?, checked: Boolean, onToggle: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            if (description != null) {
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.primary,
                checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        )
    }
}

@Composable
private fun QualitySelectorRow(
    title: String,
    description: String,
    selected: MediaQuality,
    onSelect: (MediaQuality) -> Unit
) {
    val strings = LocalStrings.current
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MediaQuality.entries.forEach { quality ->
                val isSelected = quality == selected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { onSelect(quality) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        strings.qualityLabel(quality.key),
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
