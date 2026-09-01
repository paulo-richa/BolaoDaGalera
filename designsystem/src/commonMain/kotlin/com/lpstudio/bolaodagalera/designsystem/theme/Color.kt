package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ── Core palette ──────────────────────────────────────────────────────────────
val Neon = Color(0xFF00E676)
val OrangeNeon = Color(0xFFFF9100) // Laranja Neon Vibrante
val PinkNeon = Color(0xFFFF2D55)
val NeonDim = Color(0xFF00B85C)
val Gold = Color(0xFFFFB830)
val GoldDim = Color(0xFFCC8F00)
val DeepNavy = Color(0xFF070C18)
val Navy = Color(0xFF0D1525)
val NavyCard = Color(0xFF121D30)
val NavyElevated = Color(0xFF192540)
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)
val TextMuted = Color(0xFF8899AA)
val TextSubtle = Color(0xFF4A6080)
val ErrorRed = Color(0xFFFF5370)
val SuccessGreen = Color(0xFF00E676)

// ── Gradient presets ─────────────────────────────────────────────────────────
val GradientBg = Brush.verticalGradient(listOf(Color(0xFF0A1628), Color(0xFF070C18)))
val GradientPrimary = Brush.horizontalGradient(listOf(Neon, Color(0xFF00B0FF)))
val GradientGold = Brush.horizontalGradient(listOf(Gold, Color(0xFFFF6F00)))
val GradientCard = Brush.linearGradient(listOf(Color(0x22FFFFFF), Color(0x08FFFFFF)))
val GradientHero = Brush.verticalGradient(listOf(Color(0xFF0D2040), Color(0xFF0A1628)))
