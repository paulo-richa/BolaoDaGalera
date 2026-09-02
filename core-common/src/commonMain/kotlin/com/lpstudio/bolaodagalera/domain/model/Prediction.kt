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
     * Points already calculated by the Cloud Function (updateMatchRankings/fullRecalculateRanking)
     * and persisted on the prediction. Null while the match hasn't finished or the function
     * hasn't processed the result yet - in that case the "live" ranking estimates locally from
     * the match score, then switches to the official value once it arrives.
     */
    val points: Int? = null
)
