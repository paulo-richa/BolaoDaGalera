package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMatchRepository : MatchRepository {

    private val _matches = MutableStateFlow(allMatches)

    override fun getMatches(championshipId: String): Flow<List<Match>> = 
        _matches.map { it.filter { m -> m.championshipId == championshipId } }

    override fun getMatchesByPhase(championshipId: String, phase: Phase): Flow<List<Match>> =
        _matches.map { it.filter { m -> m.championshipId == championshipId && m.phase == phase } }

    override suspend fun getMatch(championshipId: String, matchId: String): Match =
        _matches.value.first { it.id == matchId }

    override suspend fun updateMatchScore(championshipId: String, matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {
        _matches.update { list ->
            list.map { 
                if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore, isManual = isManual) 
                else it 
            }
        }
    }

    override suspend fun updateMatchTeams(
        championshipId: String,
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
    ) {
        _matches.update { list ->
            list.map {
                if (it.id == matchId) it.copy(
                    homeTeam = homeTeam,
                    homeTeamCode = homeTeamCode,
                    homeTeamFlag = homeTeamFlag,
                    awayTeam = awayTeam,
                    awayTeamCode = awayTeamCode,
                    awayTeamFlag = awayTeamFlag,
                    matchDateMillis = dateMillis ?: it.matchDateMillis,
                    status = status ?: it.status,
                    isManual = isManual
                )
                else it
            }
        }
    }

    override suspend fun upsertMatch(match: Match) {
        _matches.update { list ->
            val index = list.indexOfFirst { it.id == match.id }
            if (index != -1) list.toMutableList().apply { set(index, match) }
            else list + match
        }
    }

    override suspend fun seedMatchesIfNeeded() { /* no-op para fake */ }
}
