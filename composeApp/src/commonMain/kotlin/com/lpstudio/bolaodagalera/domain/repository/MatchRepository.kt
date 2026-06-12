package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getMatches(): Flow<List<Match>>
    fun getMatchesByPhase(phase: Phase): Flow<List<Match>>
    suspend fun getMatch(matchId: String): Match
    suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int)
    suspend fun seedMatchesIfNeeded()
}
