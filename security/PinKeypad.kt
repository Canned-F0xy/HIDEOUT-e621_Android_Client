package com.CannedF0xy.hideout.security

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinDots(filled: Int, total: Int = AppLockManager.PIN_LENGTH) {
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        repeat(total) { index ->
            val isFilled = index < filled
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(if (isFilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape)
            )
        }
    }
}

@Composable
fun PinKeypad(
    onDigit: (Char) -> Unit,
    onDelete: () -> Unit,
    deleteDescription: String,
    leftKey: @Composable () -> Unit = {}
) {
    val rows = listOf("123", "456", "789")
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit -> KeypadButton(onClick = { onDigit(digit) }) { KeypadText(digit) } }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) { leftKey() }
            KeypadButton(onClick = { onDigit('0') }) { KeypadText('0') }
            KeypadButton(onClick = onDelete) {
                Icon(Icons.Default.Backspace, contentDescription = deleteDescription, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun KeypadButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun KeypadText(digit: Char) {
    Text(digit.toString(), fontSize = 26.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
}
