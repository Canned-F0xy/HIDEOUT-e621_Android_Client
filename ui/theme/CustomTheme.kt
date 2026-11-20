package com.CannedF0xy.hideout.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance

data class CustomThemeColors(
    val enabled: Boolean = false,
    val accent: Int? = null,
    val background: Int? = null,
    val surface: Int? = null,
    val text: Int? = null
)

object ThemePresets {
    val accents: List<Color> = listOf(
        Color(0xFFFF6B00), Color(0xFFE53935), Color(0xFFEC407A), Color(0xFFAB47BC),
        Color(0xFF5C6BC0), Color(0xFF2979FF), Color(0xFF00BCD4), Color(0xFF43A047),
        Color(0xFFFFC107)
    )
    val backgrounds: List<Color> = listOf(
        Color(0xFF050505), Color(0xFF000000), Color(0xFF0D1117), Color(0xFF1A1025),
        Color(0xFF101A14), Color(0xFFF5F5F5), Color(0xFFFFFFFF), Color(0xFFFFF8E7)
    )
    val texts: List<Color> = listOf(
        Color(0xFFE0E0E0), Color(0xFFFFFFFF), Color(0xFFBDBDBD), Color(0xFFF5E6C8),
        Color(0xFF050505), Color(0xFF000000), Color(0xFF424242), Color(0xFF1A237E)
    )
    val surfaces: List<Color> = listOf(
        Color(0xFF141414), Color(0xFF0A0A0A), Color(0xFF161B22), Color(0xFF241733),
        Color(0xFF17261D), Color(0xFFFFFFFF), Color(0xFFEEEEEE), Color(0xFFFFF1D6)
    )

    fun parseHex(input: String): Int? {
        val hex = input.trim().removePrefix("#")
        if (hex.length != 6 || !hex.all { it.isDigit() || it.lowercaseChar() in 'a'..'f' }) return null
        return (0xFF000000.toInt()) or hex.toInt(16)
    }

    fun toHex(color: Int): String = String.format("#%06X", color and 0xFFFFFF)
}

val LocalCustomThemeEnabled = staticCompositionLocalOf { false }

object ContrastUtils {
    fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        val light = maxOf(la, lb)
        val dark = minOf(la, lb)
        return (light + 0.05f) / (dark + 0.05f)
    }

    fun ensureContrast(foreground: Color, backgrounds: List<Color>, minContrast: Float): Color {
        val opaque = foreground.copy(alpha = 1f)
        if (backgrounds.all { contrast(opaque, it) >= minContrast }) return foreground
        val averageLuminance = backgrounds.map { it.luminance() }.average().toFloat()
        val target = if (averageLuminance > 0.4f) Color.Black else Color.White
        var low = 0f
        var high = 1f
        var best = lerp(opaque, target, 1f)
        repeat(12) {
            val mid = (low + high) / 2f
            val candidate = lerp(opaque, target, mid)
            if (backgrounds.all { contrast(candidate, it) >= minContrast }) {
                best = candidate
                high = mid
            } else {
                low = mid
            }
        }
        return best.copy(alpha = foreground.alpha)
    }

    fun readableOn(color: Color): Color =
        if (contrast(DeepBlack, color) >= contrast(TextWhite, color)) DeepBlack else TextWhite
}

@Composable
@ReadOnlyComposable
fun readableAccent(color: Color, background: Color = MaterialTheme.colorScheme.background, minContrast: Float = 4.5f): Color {
    if (!LocalCustomThemeEnabled.current) return color
    val effectiveBackground = background.compositeOver(MaterialTheme.colorScheme.background)
    return ContrastUtils.ensureContrast(color, listOf(effectiveBackground), minContrast)
}

object HideoutThemeBuilder {
    fun build(isDark: Boolean, custom: CustomThemeColors): ColorScheme {
        val base = if (isDark) HideoutDarkTheme else HideoutLightTheme
        if (!custom.enabled) return base

        val background = custom.background?.let { Color(it) } ?: base.background
        val surface = custom.surface?.let { Color(it) } ?: base.surface
        val rawAccent = custom.accent?.let { Color(it) } ?: base.primary
        val accent = ContrastUtils.ensureContrast(rawAccent, listOf(background, surface), 3f)

        val customText = custom.text?.let { Color(it) }
        val onBackground = customText ?: ContrastUtils.readableOn(background)
        val onSurface = customText ?: ContrastUtils.readableOn(surface)
        val onSurfaceVariant = if (customText != null) lerp(customText, surface, 0.3f) else ContrastUtils.ensureContrast(lerp(onSurface, surface, 0.35f), listOf(surface, background), 4.5f)
        val isLightSurface = surface.luminance() > 0.4f
        val shiftTarget = if (isLightSurface) Color.Black else Color.White

        fun container(step: Float): Color = lerp(surface, shiftTarget, step)

        val accentContainer = accent.copy(alpha = 0.22f).compositeOver(surface)
        val outline = lerp(onSurface, surface, 0.55f)
        val outlineVariant = lerp(onSurface, surface, 0.8f)

        return base.copy(
            primary = accent,
            onPrimary = ContrastUtils.readableOn(accent),
            primaryContainer = accentContainer,
            onPrimaryContainer = ContrastUtils.ensureContrast(accent, listOf(accentContainer), 4.5f),
            inversePrimary = accent,
            secondary = accent,
            onSecondary = ContrastUtils.readableOn(accent),
            secondaryContainer = accentContainer,
            onSecondaryContainer = ContrastUtils.readableOn(accentContainer),
            tertiary = accent,
            onTertiary = ContrastUtils.readableOn(accent),
            tertiaryContainer = accentContainer,
            onTertiaryContainer = ContrastUtils.readableOn(accentContainer),
            background = background,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surface,
            onSurfaceVariant = onSurfaceVariant,
            surfaceTint = accent,
            inverseSurface = onSurface,
            inverseOnSurface = surface,
            outline = outline,
            outlineVariant = outlineVariant,
            surfaceBright = container(0.06f),
            surfaceDim = lerp(surface, Color.Black, 0.06f),
            surfaceContainerLowest = surface,
            surfaceContainerLow = container(0.02f),
            surfaceContainer = container(0.04f),
            surfaceContainerHigh = container(0.06f),
            surfaceContainerHighest = container(0.08f)
        )
    }
}
