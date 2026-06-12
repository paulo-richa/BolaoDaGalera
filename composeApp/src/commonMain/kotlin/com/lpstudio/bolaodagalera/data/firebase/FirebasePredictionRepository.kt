package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class PredictionDto(
    val userId: String = "",
    val bolaoId: String = "",
    val matchId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0
)

private fun PredictionDto.toDomain(id: String) = Prediction(
    id = id,
    userId = userId,
    bolaoId = bolaoId,
    matchId = matchId,
    homeScore = homeScore,
    awayScore = awayScore
)

@Serializable
private data class MatchScoreDto(
    val homeScore: Int? = null,
    val awayScore: Int? = null
)

@Serializable
private data class UserNamesDto(
    val name: String = "",
    val nickname: String = ""
)

class FirebasePredictionRepository(
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()
) : PredictionRepository {

    private val db = Firebase.firestore
    private val predictionsCollection = db.collection("predictions")
    private val matchesCollection = db.collection("matches")
    private val usersCollection = db.collection("users")

    override fun getUserPredictions(userId: String, bolaoId: String): Flow<List<Prediction>> {
        return predictionsCollection
            .where { "userId" equalTo userId }
            .where { "bolaoId" equalTo bolaoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data<PredictionDto>().toDomain(doc.id) }
            }
    }

    override fun getBolaoAllPredictions(bolaoId: String): Flow<List<Prediction>> {
        return predictionsCollection
            .where { "bolaoId" equalTo bolaoId }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data<PredictionDto>().toDomain(doc.id) }
            }
    }

    override suspend fun getUserPredictionForMatch(userId: String, bolaoId: String, matchId: String): Prediction? {
        val snapshot = predictionsCollection
            .where { "userId" equalTo userId }
            .where { "bolaoId" equalTo bolaoId }
            .where { "matchId" equalTo matchId }
            .get()
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.data<PredictionDto>().toDomain(doc.id)
        }
    }

    override suspend fun savePrediction(prediction: Prediction) {
        val dto = PredictionDto(
            userId = prediction.userId,
            bolaoId = prediction.bolaoId,
            matchId = prediction.matchId,
            homeScore = prediction.homeScore,
            awayScore = prediction.awayScore
        )
        val existing = getUserPredictionForMatch(prediction.userId, prediction.bolaoId, prediction.matchId)
        if (existing != null) {
            predictionsCollection.document(existing.id).set(dto)
        } else {
            predictionsCollection.add(dto)
        }
    }

    override suspend fun deleteUserPredictions(userId: String, bolaoId: String) {
        try {
            val snapshot = predictionsCollection
                .where { "userId" equalTo userId }
                .where { "bolaoId" equalTo bolaoId }
                .get()
            
            snapshot.documents.forEach { doc ->
                predictionsCollection.document(doc.id).delete()
            }
        } catch (e: Exception) { }
    }

    override fun getRanking(bolaoId: String, participantIds: List<String>): Flow<List<RankingEntry>> {
        val predictionsFlow = getBolaoAllPredictions(bolaoId)

        val matchScoresFlow = matchesCollection.snapshots.map { snapshot ->
            snapshot.documents.associate { doc ->
                val dto = doc.data<MatchScoreDto>()
                doc.id to (dto.homeScore to dto.awayScore)
            }
        }

        val userNamesFlow = usersCollection.snapshots.map { snapshot ->
            snapshot.documents.associate { doc ->
                val dto = doc.data<UserNamesDto>()
                doc.id to (dto.name to dto.nickname)
            }
        }

        return combine(predictionsFlow, matchScoresFlow, userNamesFlow) { predictions, matchScores, userNames ->
            val userStats = mutableMapOf<String, Triple<Int, Int, Int>>() // points, exact, correct

            // Initialize all participants with 0 points
            participantIds.forEach { userId ->
                userStats[userId] = Triple(0, 0, 0)
            }

            predictions.forEach { prediction ->
                if (prediction.userId !in userStats) return@forEach

                val (homeScore, awayScore) = matchScores[prediction.matchId] ?: return@forEach
                if (homeScore == null || awayScore == null) return@forEach

                val points = calculatePointsUseCase(prediction, homeScore, awayScore)
                val isExact = points == 3 
                val isCorrectResult = points >= 1

                val current = userStats[prediction.userId] ?: Triple(0, 0, 0)
                userStats[prediction.userId] = Triple(
                    current.first + points,
                    current.second + if (isExact) 1 else 0,
                    current.third + if (isCorrectResult) 1 else 0
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
