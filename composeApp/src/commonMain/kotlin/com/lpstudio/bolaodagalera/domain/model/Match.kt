package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Match(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val homeTeamFlag: String,
    val awayTeamFlag: String,
    val matchDateMillis: Long,
    val phase: Phase,
    val group: String? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val isManual: Boolean = false
) {
    val isFinished: Boolean get() = homeScore != null && awayScore != null
    val isUpcoming: Boolean get() = !isFinished
}
