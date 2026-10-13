package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.i18n.LocalStrings

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginSheet(
    loginUsername: String,
    loginApiKey: String,
    onLoginSave: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tempUser by remember { mutableStateOf(loginUsername) }
    var tempKey by remember { mutableStateOf(loginApiKey) }

    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text(strings.accountLogin, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(strings.apiKeyInfo, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(value = tempUser, onValueChange = { tempUser = it }, label = { Text(strings.usernameHint) }, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(value = tempKey, onValueChange = { tempKey = it }, label = { Text(strings.apiKeyHint) }, singleLine = true, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(32.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                if (loginUsername.isNotBlank()) {
                    TextButton(onClick = { onLoginSave("", ""); onDismiss() }, modifier = Modifier.padding(end = 8.dp)) { Text(strings.logout, color = Color.Red) }
                }
                TextButton(onClick = { onDismiss() }, modifier = Modifier.padding(end = 8.dp)) { Text(strings.cancel, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                Button(onClick = { onLoginSave(tempUser, tempKey); onDismiss() }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text(strings.save, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) }
            }
            Spacer(modifier = Modifier.height(WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()))
        }
    }
}
