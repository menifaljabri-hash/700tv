package com.tv700.player.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── 700TV Brand Colors ────────────────────────────────────────────────────────
val Navy900  = Color(0xFF050D1A)   // deepest background
val Navy800  = Color(0xFF091525)
val Navy700  = Color(0xFF0D2040)
val Navy600  = Color(0xFF0F2D5E)
val Navy500  = Color(0xFF1A3D7A)
val NavyBlue = Color(0xFF1565C0)   // primary brand blue

val Gold500  = Color(0xFFFFC107)   // logo gold
val Gold600  = Color(0xFFFFAB00)
val Gold700  = Color(0xFFFF8F00)

val OrangeAccent = Color(0xFFFF6D00)  // sunglasses orange from logo

val SharkWhite = Color(0xFFE8EDF5)
val SharkGray  = Color(0xFF8FA3BF)

// ── Dark color scheme ─────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = Gold500,
    onPrimary        = Navy900,
    primaryContainer = Navy600,
    onPrimaryContainer = Gold500,

    secondary        = OrangeAccent,
    onSecondary      = Navy900,

    background       = Navy900,
    onBackground     = SharkWhite,

    surface          = Navy800,
    onSurface        = SharkWhite,
    surfaceVariant   = Navy700,
    onSurfaceVariant = SharkGray,

    error            = Color(0xFFCF6679),
    outline          = Navy500
)

@Composable
fun TV700Theme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = TV700Typography,
        content     = content
    )
}
