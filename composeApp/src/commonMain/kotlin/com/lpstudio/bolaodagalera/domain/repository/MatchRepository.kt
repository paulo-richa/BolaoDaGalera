package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatches(): Flow<List<Match>>
    fun getMatchesByPhase(phase: Phase): Flow<List<Match>>
    suspend fun getMatch(matchId: String): Match
    suspend fun updateMatchScore(matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean = true)
    suspend fun updateMatchTeams(
        matchId: String,
        homeTeam: String,
        homeTeamCode: String,
        homeTeamFlag: String,
        awayTeam: String,
        awayTeamCode: String,
        awayTeamFlag: String,
        dateMillis: Long? = null,
        status: String? = null,
        isManual: Boolean = true
    )
    suspend fun upsertMatch(match: Match)
    suspend fun seedMatchesIfNeeded()
}
