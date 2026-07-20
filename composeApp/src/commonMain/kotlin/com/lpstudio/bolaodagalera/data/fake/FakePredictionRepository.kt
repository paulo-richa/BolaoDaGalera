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

    private val _predictions = MutableStateFlow<List<Prediction>>(emptyList())

    private val userNames = mapOf(
        "pauloricha" to ("Paulo George Moreira Richa" to "Paulão")
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

            participantIds.forEach { userId ->
                userStats[userId] = Triple(0, 0, 0)
            }

            predictions.forEach { prediction ->
                if (prediction.userId !in userStats) return@forEach

                val (homeScore, awayScore) = matchScores[prediction.matchId] ?: return@forEach
                if (homeScore == null || awayScore == null) return@forEach

                val points = calculatePointsUseCase(prediction, homeScore, awayScore)
                val isExact = points == 3 
                val isCorrect = points == 1

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
