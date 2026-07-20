package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatches(championshipId: String): Flow<List<Match>>
    fun getMatchesByPhase(championshipId: String, phase: Phase): Flow<List<Match>>
    suspend fun getMatch(championshipId: String, matchId: String): Match
    suspend fun updateMatchScore(championshipId: String, matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean = true)
    suspend fun updateMatchTeams(
        championshipId: String,
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
    
    // Para compatibilidade ou fluxos globais (opcional)
    fun getAllMatches(): Flow<List<Match>> = kotlinx.coroutines.flow.flowOf(emptyList())
}
