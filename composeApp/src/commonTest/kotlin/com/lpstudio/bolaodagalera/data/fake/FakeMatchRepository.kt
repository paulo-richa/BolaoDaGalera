package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeMatchRepository : MatchRepository {
    private val matchesState = MutableStateFlow<List<Match>>(emptyList())

    override fun getMatches(championshipId: String): Flow<List<Match>> =
        matchesState.map { it.filter { m -> m.championshipId == championshipId } }

    override fun getAllMatches(): Flow<List<Match>> = matchesState

    override suspend fun getMatch(championshipId: String, matchId: String): Match = matchesState.value.first { it.id == matchId }

    override suspend fun updateMatchScore(championshipId: String, matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {
        matchesState.update { list ->
            list.map {
                if (it.id == matchId) {
                    it.copy(homeScore = homeScore, awayScore = awayScore, isManual = isManual)
                } else {
                    it
                }
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
        matchesState.update { list ->
            list.map {
                if (it.id == matchId) {
                    it.copy(
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
                } else {
                    it
                }
            }
        }
    }

    override suspend fun upsertMatch(match: Match) {
        matchesState.update { list ->
            val index = list.indexOfFirst { it.id == match.id }
            if (index != -1) {
                list.toMutableList().apply { set(index, match) }
            } else {
                list + match
            }
        }
    }
}
