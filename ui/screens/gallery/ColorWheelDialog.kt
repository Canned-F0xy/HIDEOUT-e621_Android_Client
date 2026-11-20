package com.CannedF0xy.hideout.ui.screens.gallery

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.CannedF0xy.hideout.i18n.LocalStrings
import com.CannedF0xy.hideout.ui.theme.ThemePresets
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

private val wheelHues = listOf(
    Color(0xFFFF0000),
    Color(0xFFFF00FF),
    Color(0xFF0000FF),
    Color(0xFF00FFFF),
    Color(0xFF00FF00),
    Color(0xFFFFFF00),
    Color(0xFFFF0000)
)

private fun hsvToArgb(hue: Float, saturation: Float, value: Float): Int =
    android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))

private fun argbToHsv(argb: Int): FloatArray {
    val hsv = FloatArray(3)
    android.graphics.Color.colorToHSV(argb or 0xFF000000.toInt(), hsv)
    return hsv
}

@Composable
fun ColorWheelDialog(
    title: String,
    initialColor: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val strings = LocalStrings.current
    val initialHsv = remember(initialColor) { argbToHsv(initialColor) }
    var hue by remember { mutableFloatStateOf(initialHsv[0]) }
    var saturation by remember { mutableFloatStateOf(initialHsv[1]) }
    var brightness by remember { mutableFloatStateOf(initialHsv[2]) }
    val currentArgb = hsvToArgb(hue, saturation, brightness)
    var hexText by remember { mutableStateOf(ThemePresets.toHex(initialColor)) }
    var hexInvalid by remember { mutableStateOf(false) }
    var lastSyncedArgb by remember { mutableStateOf(initialColor or 0xFF000000.toInt()) }

    if (currentArgb != lastSyncedArgb) {
        lastSyncedArgb = currentArgb
        hexText = ThemePresets.toHex(currentArgb)
        hexInvalid = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth().height(240.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxHeight()
                                .aspectRatio(1f)
                                .pointerInput(Unit) {
                                    fun update(position: Offset) {
                                        val radius = min(size.width, size.height) / 2f
                                        val dx = position.x - size.width / 2f
                                        val dy = position.y - size.height / 2f
                                        val angle = Math.toDegrees(atan2(-dy, dx).toDouble()).toFloat()
                                        hue = (angle + 360f) % 360f
                                        saturation = (sqrt(dx * dx + dy * dy) / radius).coerceIn(0f, 1f)
                                    }
                                    awaitEachGesture {
                                        val down = awaitFirstDown()
                                        update(down.position)
                                        down.consume()
                                        drag(down.id) { change ->
                                            update(change.position)
                                            change.consume()
                                        }
                                    }
                                }
                        ) {
                            val radius = min(size.width, size.height) / 2f
                            val center = Offset(size.width / 2f, size.height / 2f)
                            drawCircle(brush = Brush.sweepGradient(wheelHues, center), radius = radius, center = center)
                            drawCircle(
                                brush = Brush.radialGradient(listOf(Color.White, Color.White.copy(alpha = 0f)), center, radius),
                                radius = radius,
                                center = center
                            )
                            drawCircle(color = Color.Black.copy(alpha = 1f - brightness), radius = radius, center = center)

                            val rad = Math.toRadians(hue.toDouble())
                            val marker = Offset(
                                center.x + (cos(rad) * saturation * radius).toFloat(),
                                center.y - (sin(rad) * saturation * radius).toFloat()
                            )
                            drawCircle(color = Color(currentArgb), radius = 9.dp.toPx(), center = marker)
                            drawCircle(color = Color.White, radius = 9.dp.toPx(), center = marker, style = Stroke(width = 3.dp.toPx()))
                            drawCircle(color = Color.Black.copy(alpha = 0.5f), radius = 10.5.dp.toPx(), center = marker, style = Stroke(width = 1.dp.toPx()))
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Canvas(
                        modifier = Modifier
                            .width(28.dp)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                            .pointerInput(Unit) {
                                fun update(position: Offset) {
                                    brightness = (1f - position.y / size.height).coerceIn(0f, 1f)
                                }
                                awaitEachGesture {
                                    val down = awaitFirstDown()
                                    update(down.position)
                                    down.consume()
                                    drag(down.id) { change ->
                                        update(change.position)
                                        change.consume()
                                    }
                                }
                            }
                    ) {
                        drawRect(brush = Brush.verticalGradient(listOf(Color(hsvToArgb(hue, saturation, 1f)), Color.Black)))
                        val y = (1f - brightness) * size.height
                        val knobHeight = 6.dp.toPx()
                        drawRect(
                            color = Color.White,
                            topLeft = Offset(0f, (y - knobHeight / 2f).coerceIn(0f, size.height - knobHeight)),
                            size = Size(size.width, knobHeight),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.clip(RoundedCornerShape(8.dp)).border(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))) {
                        Box(modifier = Modifier.size(width = 24.dp, height = 40.dp).background(Color(initialColor or 0xFF000000.toInt())))
                        Box(modifier = Modifier.size(width = 24.dp, height = 40.dp).background(Color(currentArgb)))
                    }
                    OutlinedTextField(
                        value = hexText,
                        onValueChange = { value ->
                            val cleaned = value.uppercase().filter { it == '#' || it.isDigit() || it in 'A'..'F' }.take(7)
                            hexText = cleaned
                            val parsed = ThemePresets.parseHex(cleaned)
                            if (parsed != null) {
                                val hsv = argbToHsv(parsed)
                                hue = hsv[0]
                                saturation = hsv[1]
                                brightness = hsv[2]
                                lastSyncedArgb = hsvToArgb(hue, saturation, brightness)
                                hexInvalid = false
                            } else {
                                hexInvalid = cleaned.trimStart('#').length >= 6
                            }
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        isError = hexInvalid,
                        label = { Text(if (hexInvalid) strings.themeHexInvalid else strings.themeHexLabel, fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }

                Spacer(Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) {
                        Text(strings.themePickerCancel, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val parsed = ThemePresets.parseHex(hexText)
                            onConfirm(parsed ?: currentArgb)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(strings.themeHexApply, color = MaterialTheme.colorScheme.onPrimary)
                    }
                }
            }
        }
    }
}
