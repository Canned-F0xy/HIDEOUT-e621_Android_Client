package com.CannedF0xy.hideout.ui.theme

import androidx.compose.foundation.background
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color

val NeonOrange = Color(0xFFFF6B00)
val PortalGrey = Color(0xFF8A8A8A)
val DeepBlack = Color(0xFF050505)
val DarkSurface = Color(0xFF141414)
val TextWhite = Color(0xFFE0E0E0)
val LightBackground = Color(0xFFF5F5F5)

val HideoutDarkTheme = darkColorScheme(
    primary = NeonOrange, background = DeepBlack, surface = DarkSurface, surfaceVariant = DarkSurface,
    onPrimary = DeepBlack, onBackground = TextWhite, onSurface = TextWhite, onSurfaceVariant = PortalGrey,
    primaryContainer = DarkSurface, onPrimaryContainer = NeonOrange
)

val HideoutLightTheme = lightColorScheme(
    primary = NeonOrange, background = LightBackground, surface = Color.White, surfaceVariant = Color.White,
    onPrimary = Color.White, onBackground = DeepBlack, onSurface = DeepBlack, onSurfaceVariant = PortalGrey,
    primaryContainer = Color.White, onPrimaryContainer = NeonOrange
)
