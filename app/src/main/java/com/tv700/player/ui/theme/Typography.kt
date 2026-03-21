package com.tv700.player.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Using downloadable Google Fonts via Compose (add to build.gradle if needed)
// Barlow Condensed — tight, bold, sporty — matches the 700TV logo energy
val TV700Typography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize   = 57.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize   = 45.sp,
        letterSpacing = 0.sp
    ),
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 32.sp,
        letterSpacing = 0.5.sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize   = 28.sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 22.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 16.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        letterSpacing = 0.5.sp
    )
)
