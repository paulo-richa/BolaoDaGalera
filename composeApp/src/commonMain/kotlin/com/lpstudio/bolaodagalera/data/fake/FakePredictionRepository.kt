package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakePredictionRepository(
    private val matchRepository: MatchRepository,
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()
) : PredictionRepository {

    private val _predictions = MutableStateFlow(
        listOf(
            // ── Jogador Teste (pauloricha) ──────────────────────────────────
            // Grupo A: México 2x1 África do Sul → palpite exato ✅ +3
            Prediction("p1",  "pauloricha", "bolao-1", "GS-A-1", homeScore = 2, awayScore = 1),
            // Grupo A: Coreia 1x0 Tcheca → palpite exato ✅ +3
            Prediction("p2",  "pauloricha", "bolao-1", "GS-A-2", homeScore = 1, awayScore = 0),
            // Grupo A: Tcheca 1x1 África do Sul → palpite 0x0 (empate certo) 🟡 +1
            Prediction("p3",  "pauloricha", "bolao-1", "GS-A-3", homeScore = 0, awayScore = 0),
            // Grupo B: Canadá 2x0 Bósnia → palpite 1x0 (vencedor certo) 🟡 +1
            Prediction("p4",  "pauloricha", "bolao-1", "GS-B-1", homeScore = 1, awayScore = 0),
            // Grupo B: Catar 1x1 Suíça → palpite 0x0 (empate certo) 🟡 +1
            Prediction("p27", "pauloricha", "bolao-1", "GS-B-2", homeScore = 0, awayScore = 0),
            // Grupo C: Brasil 2x1 Marrocos → palpite 3x1 (vencedor certo) 🟡 +1
            Prediction("p5",  "pauloricha", "bolao-1", "GS-C-1", homeScore = 3, awayScore = 1),
            // ── Maria Silva (fake-user-2) ────────────────────────────────────
            // Grupo A: México 2x1 → palpite 1x0 (resultado certo) 🟡 +1
            Prediction("p6",  "fake-user-2", "bolao-1", "GS-A-1", homeScore = 1, awayScore = 0),
            // Grupo A: Coreia 1x0 → palpite exato ✅ +3
            Prediction("p7",  "fake-user-2", "bolao-1", "GS-A-2", homeScore = 1, awayScore = 0),
            // Grupo B: Canadá 2x0 → palpite exato ✅ +3
            Prediction("p8",  "fake-user-2", "bolao-1", "GS-B-1", homeScore = 2, awayScore = 0),
            // Grupo C: Brasil 2x1 → palpite 2x0 (resultado certo) 🟡 +1
            Prediction("p9",  "fake-user-2", "bolao-1", "GS-C-1", homeScore = 2, awayScore = 0),
            // ── Carlos Souza (fake-user-3) ───────────────────────────────────
            // Grupo A: México 2x1 → palpite errado ❌ +0
            Prediction("p10", "fake-user-3", "bolao-1", "GS-A-1", homeScore = 0, awayScore = 0),
            // Grupo B: Catar 1x1 Suíça → palpite exato ✅ +3
            Prediction("p11", "fake-user-3", "bolao-1", "GS-B-2", homeScore = 1, awayScore = 1),
            // Grupo C: Brasil 2x1 → palpite errado ❌ +0
            Prediction("p12", "fake-user-3", "bolao-1", "GS-C-1", homeScore = 0, awayScore = 1),

            // Novos Participantes
            Prediction("p13", "u3", "bolao-1", "GS-A-1", 2, 1), // Rick +3
            Prediction("p14", "u4", "bolao-1", "GS-A-1", 1, 0), // Bia +1
            Prediction("p15", "u5", "bolao-1", "GS-A-1", 3, 1), // Fernandão +1
            Prediction("p16", "u6", "bolao-1", "GS-A-2", 1, 0), // Ju +3
            Prediction("p17", "u7", "bolao-1", "GS-B-1", 2, 0), // Tchelo +3
            Prediction("p20", "u3", "bolao-1", "GS-B-1", 1, 0), // Rick +1
            Prediction("p21", "u4", "bolao-1", "GS-B-1", 2, 1), // Bia +1
            Prediction("p22", "u5", "bolao-1", "GS-B-1", 0, 0), // Fernandão 0
            Prediction("p23", "u6", "bolao-1", "GS-B-1", 3, 0), // Ju +1
            Prediction("p24", "u8", "bolao-1", "GS-B-1", 2, 0), // Paty +3
            Prediction("p25", "u9", "bolao-1", "GS-B-1", 1, 2), // Guga 0
            Prediction("p26", "fake-user-3", "bolao-1", "GS-B-1", 1, 1), // Carlos 0
            Prediction("p18", "u8", "bolao-1", "GS-C-1", 2, 1), // Paty +3
            Prediction("p19", "u9", "bolao-1", "GS-D-1", 1, 1), // Guga 0
        )
    )

    private val userNames = mapOf(
        "pauloricha" to ("Paulo George Moreira Richa" to "Paulão"),
        "livialima" to ("Lívia Cristina de Lima" to "Lívia"),
        "u3" to ("Ricardo Oliveira" to "Rick"),
        "u4" to ("Ana Beatriz" to "Bia"),
        "u5" to ("Fernando Costa" to "Fernandão"),
        "u6" to ("Juliana Mendes" to "Ju"),
        "u7" to ("Marcelo Santos" to "Tchelo"),
        "u8" to ("Patrícia Lima" to "Paty"),
        "u9" to ("Gustavo Lima" to "Guga")
    )

    override fun getUserPredictions(userId: String, bolaoId: String): Flow<List<Prediction>> =
        _predictions.map { it.filter { p -> p.userId == userId && p.bolaoId == bolaoId } }

    override fun getBolaoAllPredictions(bolaoId: String): Flow<List<Prediction>> =
        _predictions.map { it.filter { p -> p.bolaoId == bolaoId } }

    override suspend fun getUserPredictionForMatch(userId: String, bolaoId: String, matchId: String): Prediction? =
        _predictions.value.firstOrNull { it.userId == userId && it.bolaoId == bolaoId && it.matchId == matchId }

    override suspend fun savePrediction(prediction: Prediction) {
        _predictions.update { list ->
            val existing = list.firstOrNull {
                it.userId == prediction.userId && it.bolaoId == prediction.bolaoId && it.matchId == prediction.matchId
            }
            if (existing != null) {
                list.map { if (it.id == existing.id) prediction.copy(id = existing.id) else it }
            } else {
                list + prediction.copy(id = "p${list.size + 1}")
            }
        }
    }

    override suspend fun deleteUserPredictions(userId: String, bolaoId: String) {
        _predictions.update { list ->
            list.filterNot { it.userId == userId && it.bolaoId == bolaoId }
        }
    }

    override fun getRanking(bolaoId: String, participantIds: List<String>): Flow<List<RankingEntry>> {
        return combine(
            getBolaoAllPredictions(bolaoId),
            matchRepository.getMatches()
        ) { predictions, matches ->
            val matchScores = matches.associate { it.id to (it.homeScore to it.awayScore) }
            val userStats = mutableMapOf<String, Triple<Int, Int, Int>>() // points, exact, correct

            // Initialize all participants with 0 points
            participantIds.forEach { userId ->
                userStats[userId] = Triple(0, 0, 0)
            }

            predictions.forEach { prediction ->
                if (prediction.userId !in userStats) return@forEach // Only participants in this bolao

                val (homeScore, awayScore) = matchScores[prediction.matchId] ?: return@forEach
                if (homeScore == null || awayScore == null) return@forEach

                val points = calculatePointsUseCase(prediction, homeScore, awayScore)
                val isExact = points == 3 
                val isCorrect = points >= 1

                val current = userStats[prediction.userId] ?: Triple(0, 0, 0)
                userStats[prediction.userId] = Triple(
                    current.first + points,
                    current.second + if (isExact) 1 else 0,
                    current.third + if (isCorrect) 1 else 0
                )
            }

            userStats.map { (userId, stats) ->
                val (name, nick) = userNames[userId] ?: ("Usuário" to "")
                RankingEntry(
                    userId = userId,
                    userName = name,
                    userNickname = nick,
                    points = stats.first,
                    exactScores = stats.second,
                    correctResults = stats.third
                )
            }.sortedWith(
                compareByDescending<RankingEntry> { it.points }
                    .thenByDescending { it.exactScores }
                    .thenByDescending { it.correctResults }
                    .thenBy { it.userName.lowercase() }
            )
        }
    }
}
