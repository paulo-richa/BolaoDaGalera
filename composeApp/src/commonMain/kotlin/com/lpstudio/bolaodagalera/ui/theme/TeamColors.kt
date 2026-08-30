package com.lpstudio.bolaodagalera.ui.theme

import androidx.compose.ui.graphics.Color

data class TeamColors(val primary: Color, val secondary: Color)

val teamColorMap =
    mapOf(
        // Palmeiras
        "PAL" to TeamColors(Color(0xFF006437), Color.White),
        // Flamengo
        "FLA" to TeamColors(Color(0xFFC80000), Color.Black),
        // Corinthians
        "COR" to TeamColors(Color.White, Color.Black),
        // São Paulo
        "SAO" to TeamColors(Color.Red, Color.White),
        // Botafogo
        "BOT" to TeamColors(Color.Black, Color.White),
        // Fluminense
        "FLU" to TeamColors(Color(0xFF800020), Color(0xFF006437)),
        // Atlético-MG
        "CAM" to TeamColors(Color.Black, Color.White),
        // Grêmio
        "GRE" to TeamColors(Color(0xFF0D80BF), Color.Black),
        // Internacional
        "INT" to TeamColors(Color(0xFFE50000), Color.White),
        // Cruzeiro
        "CRU" to TeamColors(Color(0xFF003399), Color.White),
        // Vasco
        "VAS" to TeamColors(Color.Black, Color.White),
        // Bahia
        "BAH" to TeamColors(Color(0xFF003399), Color(0xFFE50000)),
        // Athletico-PR
        "CAP" to TeamColors(Color(0xFFE50000), Color.Black),
        // Fortaleza
        "FOR" to TeamColors(Color(0xFFE50000), Color(0xFF003399)),
        // Vitória
        "VIT" to TeamColors(Color(0xFFE50000), Color.Black),
        // Santos
        "SAN" to TeamColors(Color.White, Color.Black),
        // Bragantino
        "RBB" to TeamColors(Color.White, Color(0xFFE50000)),
        // Coritiba
        "CFC" to TeamColors(Color(0xFF006437), Color.White),
        // Mirassol
        "MIR" to TeamColors(Color(0xFFFFD700), Color(0xFF006437)),
        // Chapecoense
        "CHA" to TeamColors(Color(0xFF006437), Color.White),
        // Remo
        "CRE" to TeamColors(Color(0xFF001A44), Color.White)
    )
