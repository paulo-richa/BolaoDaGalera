package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.observability.CrashReporter
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class PredictionDto(
    val userId: String = "",
    val bolaoId: String = "",
    val matchId: String = "",
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val points: Int? = null
)

private fun PredictionDto.toDomain(id: String) = Prediction(
    id = id,
    userId = userId,
    bolaoId = bolaoId,
    matchId = matchId,
    homeScore = homeScore,
    awayScore = awayScore,
    points = points
)

@Serializable
private data class RankingDto(
    val userId: String = "",
    val userName: String = "",
    val userNickname: String = "",
    val totalPoints: Int = 0,
    val totalExactScores: Int = 0,
    val totalCorrectResults: Int = 0
)

class FirebasePredictionRepository(
    private val crashReporter: CrashReporter,
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()
) : PredictionRepository {
    private val db = Firebase.firestore

    private fun getPredictionsCollection(bolaoId: String) = db.collection("boloes").document(bolaoId).collection("predictions")

    private fun getRankingsCollection(bolaoId: String) = db.collection("boloes").document(bolaoId).collection("rankings")

    override fun getUserPredictions(userId: String, bolaoId: String): Flow<List<Prediction>> = try {
        val query =
            if (bolaoId.isEmpty()) {
                db.collectionGroup("predictions").where { "userId" equalTo userId }
            } else {
                getPredictionsCollection(bolaoId).where { "userId" equalTo userId }
            }

        query.snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data<PredictionDto>().toDomain(doc.id) }
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro no getUserPredictions")
                println("BOLAOLOG: Erro no getUserPredictions: ${e.message}")
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico no getUserPredictions")
        println("BOLAOLOG: Erro crítico no getUserPredictions: ${e.message}")
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override fun getBolaoAllPredictions(bolaoId: String): Flow<List<Prediction>> = try {
        getPredictionsCollection(bolaoId)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data<PredictionDto>().toDomain(doc.id) }
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro ao observar todos os palpites do bolão $bolaoId")
                println("BOLAOLOG: Erro ao observar todos os palpites do bolão $bolaoId: ${e.message}")
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar todos os palpites do bolão $bolaoId")
        println("BOLAOLOG: Erro crítico ao observar todos os palpites do bolão $bolaoId: ${e.message}")
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    // Sem try/catch aqui de propósito: savePrediction() usa este resultado para
    // decidir entre criar ou atualizar o palpite. Engolir a exceção e devolver
    // null fazia parecer que "não existe palpite ainda" quando na real a
    // leitura só falhou por rede - e savePrediction criava um palpite
    // duplicado em vez de atualizar o existente.
    override suspend fun getUserPredictionForMatch(userId: String, bolaoId: String, matchId: String): Prediction? {
        val snapshot =
            getPredictionsCollection(bolaoId)
                .where { "userId" equalTo userId }
                .where { "matchId" equalTo matchId }
                .get()
        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.data<PredictionDto>().toDomain(doc.id)
        }
    }

    override suspend fun savePrediction(prediction: Prediction) {
        val dto =
            PredictionDto(
                userId = prediction.userId,
                bolaoId = prediction.bolaoId,
                matchId = prediction.matchId,
                homeScore = prediction.homeScore,
                awayScore = prediction.awayScore
            )
        val existing = getUserPredictionForMatch(prediction.userId, prediction.bolaoId, prediction.matchId)
        val collection = getPredictionsCollection(prediction.bolaoId)
        if (existing != null) {
            collection.document(existing.id).set(dto)
        } else {
            collection.add(dto)
        }
    }

    override suspend fun deleteUserPredictions(userId: String, bolaoId: String) {
        try {
            val collection = getPredictionsCollection(bolaoId)
            val snapshot =
                collection
                    .where { "userId" equalTo userId }
                    .get()

            snapshot.documents.forEach { doc ->
                collection.document(doc.id).delete()
            }
        } catch (e: Exception) {
        }
    }

    override fun getRanking(bolaoId: String, championshipId: String, participantIds: List<String>): Flow<List<RankingEntry>> = try {
        getRankingsCollection(bolaoId)
            .snapshots
            .map { snapshot ->
                val entries =
                    snapshot.documents.map { doc ->
                        val dto = doc.data<RankingDto>()
                        RankingEntry(
                            userId = dto.userId,
                            userName = dto.userName,
                            userNickname = dto.userNickname,
                            points = dto.totalPoints,
                            exactScores = dto.totalExactScores,
                            correctResults = dto.totalCorrectResults
                        )
                    }.toMutableList()

                // Se houver participantes que ainda não estão no ranking (ex: recém-entraram),
                // adicionamos eles com 0 pontos para aparecerem na lista.
                // O nome real será enriquecido pelo ViewModel buscando na coleção de users.
                val existingIds = entries.map { it.userId }.toSet()
                participantIds.forEach { pid ->
                    if (pid !in existingIds) {
                        entries.add(
                            RankingEntry(
                                userId = pid,
                                userName = "",
                                userNickname = "",
                                points = 0,
                                exactScores = 0,
                                correctResults = 0
                            )
                        )
                    }
                }

                entries.sortedWith(
                    compareByDescending<RankingEntry> { it.points }
                        .thenByDescending { it.exactScores }
                        .thenByDescending { it.correctResults }
                        .thenBy { it.userName.lowercase() }
                )
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro ao ler ranking remoto do bolão $bolaoId")
                println("BOLAOLOG: Erro ao ler ranking remoto do bolão $bolaoId: ${e.message}")
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao ler ranking remoto do bolão $bolaoId")
        println("BOLAOLOG: Erro crítico ao ler ranking remoto do bolão $bolaoId: ${e.message}")
        kotlinx.coroutines.flow.flowOf(emptyList())
    }
}
