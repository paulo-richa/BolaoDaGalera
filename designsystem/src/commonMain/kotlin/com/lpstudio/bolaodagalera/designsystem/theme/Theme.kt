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
 * Tema Material 3 do app: ColorScheme + Typography + Shapes, os 3 pilares
 * recomendados pelo Google. Só tema escuro por enquanto (o app força dark).
 * Não lida com comportamento específico de plataforma (ex: cor da status
 * bar) - isso fica por conta de quem consome este composable no app.
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
