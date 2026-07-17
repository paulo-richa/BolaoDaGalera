package com.lpstudio.bolaodagalera.ui.theme

import androidx.compose.ui.graphics.Color

data class TeamColors(
    val primary: Color,
    val secondary: Color
)

val TeamColorMap = mapOf(
    "PAL" to TeamColors(Color(0xFF006437), Color.White),        // Palmeiras
    "FLA" to TeamColors(Color(0xFFC80000), Color.Black),        // Flamengo
    "COR" to TeamColors(Color.White, Color.Black),              // Corinthians
    "SAO" to TeamColors(Color.Red, Color.White),                // São Paulo
    "BOT" to TeamColors(Color.Black, Color.White),              // Botafogo
    "FLU" to TeamColors(Color(0xFF800020), Color(0xFF006437)),  // Fluminense
    "CAM" to TeamColors(Color.Black, Color.White),              // Atlético-MG
    "GRE" to TeamColors(Color(0xFF0D80BF), Color.Black),        // Grêmio
    "INT" to TeamColors(Color(0xFFE50000), Color.White),        // Internacional
    "CRU" to TeamColors(Color(0xFF003399), Color.White),        // Cruzeiro
    "VAS" to TeamColors(Color.Black, Color.White),              // Vasco
    "BAH" to TeamColors(Color(0xFF003399), Color(0xFFE50000)),  // Bahia
    "CAP" to TeamColors(Color(0xFFE50000), Color.Black),        // Athletico-PR
    "FOR" to TeamColors(Color(0xFFE50000), Color(0xFF003399)),  // Fortaleza
    "VIT" to TeamColors(Color(0xFFE50000), Color.Black),        // Vitória
    "SAN" to TeamColors(Color.White, Color.Black),              // Santos
    "RBB" to TeamColors(Color.White, Color(0xFFE50000)),        // Bragantino
    "CFC" to TeamColors(Color(0xFF006437), Color.White),        // Coritiba
    "MIR" to TeamColors(Color(0xFFFFD700), Color(0xFF006437)),  // Mirassol
    "CHA" to TeamColors(Color(0xFF006437), Color.White),        // Chapecoense
    "CRE" to TeamColors(Color(0xFF001A44), Color.White),        // Remo
)
