package com.lpstudio.bolaodagalera.presentation.theme

import androidx.compose.runtime.Composable
import com.lpstudio.bolaodagalera.SystemAppearance
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme

// Re-exportados do :designsystem (fonte única de verdade) para não quebrar
// os imports existentes em telas que ainda não migraram (Design System Fase 4).
val Neon = com.lpstudio.bolaodagalera.designsystem.theme.Neon
val OrangeNeon = com.lpstudio.bolaodagalera.designsystem.theme.OrangeNeon
val PinkNeon = com.lpstudio.bolaodagalera.designsystem.theme.PinkNeon
val NeonDim = com.lpstudio.bolaodagalera.designsystem.theme.NeonDim
val Gold = com.lpstudio.bolaodagalera.designsystem.theme.Gold
val GoldDim = com.lpstudio.bolaodagalera.designsystem.theme.GoldDim
val DeepNavy = com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
val Navy = com.lpstudio.bolaodagalera.designsystem.theme.Navy
val NavyCard = com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
val NavyElevated = com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
val GlassWhite = com.lpstudio.bolaodagalera.designsystem.theme.GlassWhite
val GlassBorder = com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
val TextMuted = com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
val TextSubtle = com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
val ErrorRed = com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
val SuccessGreen = com.lpstudio.bolaodagalera.designsystem.theme.SuccessGreen

val GradientBg = com.lpstudio.bolaodagalera.designsystem.theme.GradientBg
val GradientPrimary = com.lpstudio.bolaodagalera.designsystem.theme.GradientPrimary
val GradientGold = com.lpstudio.bolaodagalera.designsystem.theme.GradientGold
val GradientCard = com.lpstudio.bolaodagalera.designsystem.theme.GradientCard
val GradientHero = com.lpstudio.bolaodagalera.designsystem.theme.GradientHero

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    SystemAppearance(isDark = true)
    BolaoTheme(content = content)
}
