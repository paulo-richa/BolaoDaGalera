package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BolaoColorScheme =
    darkColorScheme(
        primary = Neon,
        onPrimary = Color(0xFF003820),
        primaryContainer = Color(0xFF004D2A),
        onPrimaryContainer = Color(0xFFB8FFD9),
        secondary = Gold,
        onSecondary = Color(0xFF3D2000),
        secondaryContainer = Color(0xFF573100),
        onSecondaryContainer = Color(0xFFFFDDB0),
        background = DeepNavy,
        onBackground = Color.White,
        surface = Navy,
        onSurface = Color.White,
        surfaceVariant = NavyCard,
        onSurfaceVariant = TextMuted,
        error = ErrorRed,
        outline = Color(0xFF223040),
        outlineVariant = Color(0xFF162030)
    )

/**
 * The app's Material 3 theme: ColorScheme + Typography + Shapes, the 3
 * pillars recommended by Google. Dark theme only for now (the app forces
 * dark mode). Does not handle platform-specific behavior (e.g. status bar
 * color) - that is left to whoever consumes this composable in the app.
 */
@Composable
fun BolaoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = BolaoColorScheme,
        typography = BolaoTypography,
        shapes = BolaoShapes,
        content = content
    )
}
