package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Prediction(
    val id: String = "",
    val userId: String = "",
    val bolaoId: String = "",
    val matchId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    /**
     * Pontos já calculados pela Cloud Function (updateMatchRankings/fullRecalculateRanking)
     * e persistidos no palpite. Null enquanto o jogo não terminou ou a function ainda não
     * processou o resultado - nesse caso o ranking "ao vivo" estima localmente em cima do
     * placar do jogo, mas troca pelo valor oficial assim que ele chega.
     */
    val points: Int? = null
)
