package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.CannedF0xy.hideout.data.AppPreferences
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.ui.theme.CustomThemeColors
import com.CannedF0xy.hideout.ui.theme.ThemePresets
import com.CannedF0xy.hideout.ui.theme.ContrastUtils
import com.CannedF0xy.hideout.ui.theme.readableAccent
import kotlinx.coroutines.launch

@Composable
fun ThemeSettingsSection(appPrefs: AppPreferences) {
    val strings = LocalStrings.current
    val scope = rememberCoroutineScope()
    val custom by appPrefs.customTheme.collectAsState(initial = CustomThemeColors())
    val current = MaterialTheme.colorScheme

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { scope.launch { appPrefs.setCustomThemeEnabled(!custom.enabled) } }
                .padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(strings.customThemeTitle, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
                Text(strings.customThemeDescription, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(
                checked = custom.enabled,
                onCheckedChange = { scope.launch { appPrefs.setCustomThemeEnabled(it) } },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            )
        }

        if (custom.enabled) {
            ColorPickerRow(
                title = strings.themeAccentTitle,
                presets = ThemePresets.accents,
                selected = custom.accent ?: current.primary.toArgb(),
                onSelect = { color -> scope.launch { appPrefs.setThemeAccent(color) } }
            )
            ColorPickerRow(
                title = strings.themeBackgroundTitle,
                presets = ThemePresets.backgrounds,
                selected = custom.background ?: current.background.toArgb(),
                onSelect = { color -> scope.launch { appPrefs.setThemeBackground(color) } }
            )
            ColorPickerRow(
                title = strings.themeSurfaceTitle,
                presets = ThemePresets.surfaces,
                selected = custom.surface ?: current.surface.toArgb(),
                onSelect = { color -> scope.launch { appPrefs.setThemeSurface(color) } }
            )
            val textSelected = custom.text ?: current.onBackground.toArgb()
            val textContrast = minOf(
                ContrastUtils.contrast(Color(textSelected), current.background),
                ContrastUtils.contrast(Color(textSelected), current.surface)
            )
            ColorPickerRow(
                title = strings.themeTextTitle,
                presets = ThemePresets.texts,
                selected = textSelected,
                onSelect = { color -> scope.launch { appPrefs.setThemeText(color) } },
                isAuto = custom.text == null,
                onAuto = { scope.launch { appPrefs.setThemeText(null) } },
                warning = if (custom.text != null && textContrast < 3f) strings.themeTextLowContrast else null
            )
            OutlinedButton(
                onClick = { scope.launch { appPrefs.resetCustomThemeColors() } },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(strings.themeReset, color = MaterialTheme.colorScheme.onSurface)
            }
        }
    }
}

@Composable
private fun ColorPickerRow(
    title: String,
    presets: List<Color>,
    selected: Int,
    onSelect: (Int) -> Unit,
    isAuto: Boolean? = null,
    onAuto: (() -> Unit)? = null,
    warning: String? = null
) {
    val strings = LocalStrings.current
    val focusManager = LocalFocusManager.current
    var hexInput by remember(selected) { mutableStateOf(ThemePresets.toHex(selected)) }
    var isInvalid by remember(selected) { mutableStateOf(false) }
    var showWheel by remember { mutableStateOf(false) }

    if (showWheel) {
        ColorWheelDialog(
            title = title,
            initialColor = selected,
            onConfirm = { color ->
                showWheel = false
                onSelect(color)
            },
            onDismiss = { showWheel = false }
        )
    }

    fun applyHex() {
        val parsed = ThemePresets.parseHex(hexInput)
        if (parsed == null) {
            isInvalid = true
        } else {
            isInvalid = false
            onSelect(parsed)
            focusManager.clearFocus()
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
        Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            OutlinedIconButton(
                onClick = { showWheel = true },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    Icons.Default.Palette,
                    contentDescription = strings.themePickerOpen,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
            if (isAuto != null && onAuto != null) {
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isAuto) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                        .border(
                            width = if (isAuto) 0.dp else 1.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable { onAuto() }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        strings.themeTextAuto,
                        color = if (isAuto) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            presets.forEach { color ->
                val argb = color.toArgb()
                val isSelected = argb == selected && isAuto != true
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            shape = CircleShape
                        )
                        .clickable { onSelect(argb) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = if (color.luminance() > 0.45f) Color.Black else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(selected))
                    .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .clickable { showWheel = true }
            )
            Spacer(Modifier.width(10.dp))
            OutlinedTextField(
                value = hexInput,
                onValueChange = { value ->
                    hexInput = value.uppercase().filter { it == '#' || it.isDigit() || it in 'A'..'F' }.take(7)
                    isInvalid = false
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                isError = isInvalid,
                label = { Text(if (isInvalid) strings.themeHexInvalid else strings.themeHexLabel, fontSize = 12.sp) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { applyHex() })
            )
            TextButton(onClick = { applyHex() }) {
                Text(strings.themeHexApply, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (warning != null) {
            Text(warning, color = readableAccent(Color(0xFFE53935), MaterialTheme.colorScheme.surface), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
        }
    }
}
