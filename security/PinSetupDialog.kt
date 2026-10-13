package com.CannedF0xy.hideout.security

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.CannedF0xy.hideout.i18n.LocalStrings

@Composable
fun PinSetupDialog(onPinCreated: (String) -> Unit, onDismiss: () -> Unit) {
    val strings = LocalStrings.current
    var firstPin by remember { mutableStateOf<String?>(null) }
    var input by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    if (firstPin == null) strings.pinSetupTitle else strings.pinConfirmTitle,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(20.dp))
                PinDots(filled = input.length)
                Spacer(modifier = Modifier.height(8.dp))
                Text(errorText ?: " ", color = Color.Red, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                PinKeypad(
                    onDigit = { digit ->
                        if (input.length < AppLockManager.PIN_LENGTH) {
                            input += digit
                            if (input.length == AppLockManager.PIN_LENGTH) {
                                val first = firstPin
                                if (first == null) {
                                    firstPin = input
                                    errorText = null
                                } else if (first == input) {
                                    onPinCreated(input)
                                } else {
                                    firstPin = null
                                    errorText = strings.pinMismatch
                                }
                                input = ""
                            }
                        }
                    },
                    onDelete = { if (input.isNotEmpty()) input = input.dropLast(1) },
                    deleteDescription = strings.cdDelete
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}
