package com.CannedF0xy.hideout.security

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.CannedF0xy.hideout.R
import com.CannedF0xy.hideout.i18n.LocalStrings
import kotlinx.coroutines.delay

@Composable
fun LockScreen(pinRecord: String, isBiometricEnabled: Boolean) {
    val strings = LocalStrings.current
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    var input by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }
    var cooldownSeconds by remember { mutableIntStateOf(0) }
    val canUseBiometric = isBiometricEnabled && activity != null && BiometricHelper.isAvailable(context)

    fun startBiometric() {
        if (activity != null) {
            BiometricHelper.authenticate(activity, strings.lockBiometricTitle, strings.lockUsePin) { AppLockManager.unlock() }
        }
    }

    LaunchedEffect(Unit) {
        if (canUseBiometric) startBiometric()
    }

    LaunchedEffect(AppLockManager.cooldownUntil) {
        while (AppLockManager.remainingCooldownMs() > 0) {
            cooldownSeconds = ((AppLockManager.remainingCooldownMs() + 999) / 1000).toInt()
            delay(500)
        }
        cooldownSeconds = 0
    }

    BackHandler { activity?.moveTaskToBack(true) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
            .systemBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Image(
                painter = painterResource(id = R.drawable.ic_launcher2),
                contentDescription = strings.cdLogo,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(18.dp))
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("HIDEOUT", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(strings.lockEnterPin, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            PinDots(filled = input.length)
            Spacer(modifier = Modifier.height(12.dp))
            val message = if (cooldownSeconds > 0) strings.lockCooldown(cooldownSeconds) else errorText
            Text(message ?: " ", color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 16.dp))
            PinKeypad(
                onDigit = { digit ->
                    if (cooldownSeconds == 0 && input.length < AppLockManager.PIN_LENGTH) {
                        input += digit
                        if (input.length == AppLockManager.PIN_LENGTH) {
                            if (!AppLockManager.verify(input, pinRecord)) {
                                errorText = strings.lockWrongPin
                            }
                            input = ""
                        }
                    }
                },
                onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                deleteDescription = strings.cdDelete,
                leftKey = {
                    if (canUseBiometric) {
                        IconButton(onClick = { startBiometric() }) {
                            Icon(Icons.Default.Fingerprint, contentDescription = strings.lockBiometricTitle, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                        }
                    }
                }
            )
        }
    }
}
