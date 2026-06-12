package com.lpstudio.bolaodagalera.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.lpstudio.bolaodagalera.SystemAppearance

// ── Core palette ──────────────────────────────────────────────────────────────
val Neon          = Color(0xFF00E676)
val OrangeNeon    = Color(0xFFFF9100) // Laranja Neon Vibrante
val PinkNeon      = Color(0xFFFF2D55)
val NeonDim       = Color(0xFF00B85C)
val Gold          = Color(0xFFFFB830)
val GoldDim       = Color(0xFFCC8F00)
val DeepNavy      = Color(0xFF070C18)
val Navy          = Color(0xFF0D1525)
val NavyCard      = Color(0xFF121D30)
val NavyElevated  = Color(0xFF192540)
val GlassWhite    = Color(0x1AFFFFFF)
val GlassBorder   = Color(0x33FFFFFF)
val TextMuted     = Color(0xFF8899AA)
val TextSubtle    = Color(0xFF4A6080)
val ErrorRed      = Color(0xFFFF5370)
val SuccessGreen  = Color(0xFF00E676)

// ── Gradient presets ─────────────────────────────────────────────────────────
val GradientBg = Brush.verticalGradient(listOf(Color(0xFF0A1628), Color(0xFF070C18)))
val GradientPrimary = Brush.horizontalGradient(listOf(Neon, Color(0xFF00B0FF)))
val GradientGold    = Brush.horizontalGradient(listOf(Gold, Color(0xFFFF6F00)))
val GradientCard    = Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF)))
val GradientHero    = Brush.verticalGradient(listOf(Color(0xFF0D2040), Color(0xFF0A1628)))

// ── Composition local for custom colors ──────────────────────────────────────
data class BolaoColors(
    val neon: Color          = Neon,
    val neonDim: Color       = NeonDim,
    val gold: Color          = Gold,
    val goldDim: Color       = GoldDim,
    val deepNavy: Color      = DeepNavy,
    val navyCard: Color      = NavyCard,
    val navyElevated: Color  = NavyElevated,
    val glassWhite: Color    = GlassWhite,
    val glassBorder: Color   = GlassBorder,
    val textMuted: Color     = TextMuted,
    val textSubtle: Color    = TextSubtle,
    val errorRed: Color      = ErrorRed,
)

val LocalBolaoColors = staticCompositionLocalOf { BolaoColors() }

val bolaoColors: BolaoColors
    @Composable get() = LocalBolaoColors.current

// ── Material3 color scheme ────────────────────────────────────────────────────
private val BolaoColorScheme = darkColorScheme(
    primary              = Neon,
    onPrimary            = Color(0xFF003820),
    primaryContainer     = Color(0xFF004D2A),
    onPrimaryContainer   = Color(0xFFB8FFD9),
    secondary            = Gold,
    onSecondary          = Color(0xFF3D2000),
    secondaryContainer   = Color(0xFF573100),
    onSecondaryContainer = Color(0xFFFFDDB0),
    background           = DeepNavy,
    onBackground         = Color.White,
    surface              = Navy,
    onSurface            = Color.White,
    surfaceVariant       = NavyCard,
    onSurfaceVariant     = TextMuted,
    error                = ErrorRed,
    outline              = Color(0xFF223040),
    outlineVariant       = Color(0xFF162030),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    SystemAppearance(isDark = true)
    CompositionLocalProvider(LocalBolaoColors provides BolaoColors()) {
        MaterialTheme(
            colorScheme = BolaoColorScheme,
            content = content
        )
    }
}
