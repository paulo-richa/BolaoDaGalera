package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class RankingEntry(
    val userId: String,
    val userName: String,
    val userNickname: String = "",
    val points: Int,
    val exactScores: Int,
    val correctResults: Int
)
