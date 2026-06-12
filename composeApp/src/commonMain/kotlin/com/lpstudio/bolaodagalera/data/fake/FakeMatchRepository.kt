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

    // Jogos já encerrados (grupos A, B e C) para testar ranking e palpites
    private val finishedScores = mapOf(
        // Grupo A
        "GS-A-1" to (2 to 1), // México 2 x 1 África do Sul
        "GS-A-2" to (1 to 0), // Coreia do Sul 1 x 0 Rep. Tcheca
        "GS-A-3" to (1 to 1), // Rep. Tcheca 1 x 1 África do Sul
        "GS-A-4" to (3 to 0), // México 3 x 0 Coreia do Sul
        // Grupo B
        "GS-B-2" to (1 to 1), // Catar 1 x 1 Suíça
        // Grupo C
        "GS-C-1" to (2 to 1), // Brasil 2 x 1 Marrocos  ⭐
        "GS-C-2" to (0 to 1), // Haiti 0 x 1 Escócia
    )

    // Jogos em andamento (Sem placar final, apenas scores parciais)
    private val inProgressScores = mapOf(
        "GS-B-1" to (0 to 0) // Canadá 0 x 0 Bósnia (EM ANDAMENTO)
    )

    private val _matches = MutableStateFlow(
        allMatches.map { match ->
            val finished = finishedScores[match.id]
            val inProgress = inProgressScores[match.id]
            
            when {
                finished != null -> match.copy(homeScore = finished.first, awayScore = finished.second)
                inProgress != null -> {
                    // Para simular "Em Andamento", o tempo precisa ser atual
                    match.copy(
                        homeScore = inProgress.first, 
                        awayScore = inProgress.second,
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

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        _matches.update { list ->
            list.map { if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore) else it }
        }
    }

    override suspend fun seedMatchesIfNeeded() { /* no-op para fake */ }
}
