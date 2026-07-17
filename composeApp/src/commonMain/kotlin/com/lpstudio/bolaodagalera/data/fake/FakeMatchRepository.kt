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

    // Jogos já encerrados (grupos A, B, C, D e E) para testar ranking e palpites
    private val finishedScores = mapOf(
        // Grupo A
        "GS-A-1" to (2 to 1), // México 2 x 1 África do Sul
        "GS-A-2" to (1 to 0), // Coreia do Sul 1 x 0 Rep. Tcheca
        "GS-A-3" to (1 to 1), // Rep. Tcheca 1 x 1 África do Sul
        "GS-A-4" to (3 to 0), // México 3 x 0 Coreia do Sul
        // Grupo B
        "GS-B-2" to (1 to 1), // Catar 1 x 1 Suíça
        // Grupo C
        "GS-C-1" to (2 to 1), // Brasil 2 x 1 Marrocos
        "GS-C-2" to (0 to 1), // Haiti 0 x 1 Escócia
        // Grupo D
        "GS-D-1" to (2 to 0), // EUA 2 x 0 Paraguai
        "GS-D-2" to (1 to 2), // Austrália 1 x 2 Turquia
        // Grupo E
        "GS-E-1" to (4 to 0), // Alemanha 4 x 0 Curaçao
        "GS-E-2" to (1 to 1), // Costa do Marfim 1 x 1 Equador
        // Mata-Mata
        "KO-16-1" to (0 to 2), // Canadá 0 x 2 Marrocos (Encerrado)
    )

    // Jogos em andamento com diferentes estados para teste de UI
    private val liveMatchData = mapOf(
        "GS-B-1" to Triple(0, 0, "IN_PLAY"),     // Canadá 0 x 0 Bósnia (EM ANDAMENTO)
        "GS-F-1" to Triple(1, 0, "IN_PLAY"),     // Holanda 1 x 0 Japão (EM ANDAMENTO)
        "GS-F-2" to Triple(0, 2, "PAUSED"),      // Suécia 0 x 2 Tunísia (INTERVALO)
        "KO-16-2" to Triple(0, 3, "IN_PLAY"),    // Paraguai 0 x 3 França (EM ANDAMENTO)
    )

    private val _matches = MutableStateFlow(
        allMatches.map { match ->
            val finished = finishedScores[match.id]
            val live = liveMatchData[match.id]
            
            when {
                finished != null -> match.copy(
                    homeScore = finished.first, 
                    awayScore = finished.second,
                    status = "FINISHED"
                )
                live != null -> {
                    // Para simular "Em Andamento", o tempo precisa ser atual
                    match.copy(
                        homeScore = live.first, 
                        awayScore = live.second,
                        status = live.third,
                        matchDateMillis = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis() - 3600_000L // Começou faz 1 hora
                    )
                }
                else -> match
            }
        }
    )

    override fun getMatches(): Flow<List<Match>> = _matches

    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> =
        _matches.map { it.filter { m -> m.phase == phase } }

    override suspend fun getMatch(matchId: String): Match =
        _matches.value.first { it.id == matchId }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {
        _matches.update { list ->
            list.map { 
                if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore, isManual = isManual) 
                else it 
            }
        }
    }

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
