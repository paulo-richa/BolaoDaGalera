package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Prediction(
    val id: String = "",
    val userId: String = "",
    val bolaoId: String = "",
    val matchId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0
)
