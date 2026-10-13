package com.CannedF0xy.hideout.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.CannedF0xy.hideout.R
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.ui.theme.DarkSurface

@Composable
fun VpnSetupScreen(
    setupMode: String?,
    onSetupModeChange: (String?) -> Unit,
    vpnErrorMessage: String?,
    hasVpnConfig: Boolean,
    mullvadAccountInput: String,
    onMullvadAccountChange: (String) -> Unit,
    onSelectConfig: () -> Unit,
    onEnableTunnel: () -> Unit,
    onConnectMullvad: () -> Unit,
    onSkipSetup: () -> Unit
) {
    val strings = LocalStrings.current
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Image(painter = painterResource(id = R.drawable.ic_launcher_warning), contentDescription = strings.cdLogo, modifier = Modifier.size(140.dp).padding(bottom = 16.dp))
            Text(strings.hideoutSetup, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(strings.vpnWarning, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 12.dp))

            if (setupMode == null) {
                Button(
                    onClick = { onSetupModeChange("MULLVAD") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) { Text(strings.setupMullvad, color = MaterialTheme.colorScheme.primary) }

                Button(
                    onClick = { onSetupModeChange("WIREGUARD") },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)
                ) { Text(strings.setupWireguard, color = MaterialTheme.colorScheme.primary) }

                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSkipSetup,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface, contentColor = Color.Red)
                ) { Text(strings.skipSetup, fontWeight = FontWeight.Bold) }
            } else if (setupMode == "WIREGUARD") {
                if (vpnErrorMessage != null) Text(vpnErrorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { onSelectConfig() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Text(if (!hasVpnConfig) strings.selectConfig else strings.changeConfig, color = MaterialTheme.colorScheme.primary)
                    }
                    if (hasVpnConfig) {
                        Button(
                            onClick = onEnableTunnel,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text(strings.enableTunnel, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
                    }
                }
                TextButton(onClick = { onSetupModeChange(null) }, modifier = Modifier.padding(top = 16.dp)) {
                    Text(strings.cdBack, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else if (setupMode == "MULLVAD") {
                if (vpnErrorMessage != null) Text(vpnErrorMessage!!, color = Color.Red, modifier = Modifier.padding(bottom = 12.dp))

                OutlinedTextField(
                    value = mullvadAccountInput,
                    onValueChange = onMullvadAccountChange,
                    label = { Text(strings.mullvadHint) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onConnectMullvad,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp)
                ) { Text(strings.connectLabel, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }

                TextButton(onClick = { onSetupModeChange(null) }, modifier = Modifier.padding(top = 16.dp)) {
                    Text(strings.cdBack, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
