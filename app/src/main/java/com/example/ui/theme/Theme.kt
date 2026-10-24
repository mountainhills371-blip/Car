package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RacingDarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF003844),
    onPrimaryContainer = NeonCyan,
    secondary = NitroMagenta,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0025),
    onSecondaryContainer = Color(0xFFFFB2D1),
    tertiary = ElectricGold,
    background = DarkCarbon,
    onBackground = Color.White,
    surface = DarkSurface,
    onSurface = Color.White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color(0xFFC4D2E3)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek arcade dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RacingDarkColorScheme,
        typography = Typography,
        content = content
    )
}
