package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Escala tipográfica do Material 3 (displayLarge...labelSmall), calibrada
 * pelos tamanhos realmente usados no app hoje (levantamento do código: 11sp
 * e 14sp são os mais comuns) em vez dos tamanhos padrão do M3 - o visual do
 * app é mais denso que o default. Escala de tamanhos: só números pares (8,
 * 10, 12, 14, 16, 18, 20, 22, 24, 32, 48).
 */
val BolaoTypography =
    Typography(
        displayLarge = TextStyle(fontSize = 48.sp, fontWeight = FontWeight.Black),
        displayMedium = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
        displaySmall = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold),
        headlineLarge = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold),
        headlineMedium = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
        headlineSmall = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold),
        titleLarge = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
        titleMedium = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        titleSmall = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.SemiBold),
        bodyLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal),
        bodySmall = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Normal),
        labelLarge = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
        labelMedium = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold),
        labelSmall = TextStyle(fontSize = 8.sp, fontWeight = FontWeight.Bold)
    )
