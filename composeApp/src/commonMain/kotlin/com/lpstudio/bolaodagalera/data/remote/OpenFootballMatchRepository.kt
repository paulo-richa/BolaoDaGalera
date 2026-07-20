package com.lpstudio.bolaodagalera.data.remote

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import kotlinx.coroutines.flow.*

/**
 * Repositório legado.
 * @deprecated Utilizar FootballDataMatchRepository para campeonatos ativos.
 */
class OpenFootballMatchRepository : MatchRepository {
    private val _matches = MutableStateFlow<List<Match>>(emptyList())

    override fun getMatches(): Flow<List<Match>> = _matches
    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = _matches.map { it.filter { m -> m.phase == phase } }
    override suspend fun getMatch(matchId: String): Match = _matches.value.first { it.id == matchId }
    
    override suspend fun updateMatchScore(matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {}
    override suspend fun updateMatchTeams(
        matchId: String,
        homeTeam: String,
        homeTeamCode: String,
        homeTeamFlag: String,
        awayTeam: String,
        awayTeamCode: String,
        awayTeamFlag: String,
        dateMillis: Long?,
        status: String?,
        isManual: Boolean
    ) {}
    override suspend fun upsertMatch(match: Match) {}
    override suspend fun seedMatchesIfNeeded() {}
}
