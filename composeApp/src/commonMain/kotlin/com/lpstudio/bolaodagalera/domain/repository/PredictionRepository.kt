package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import kotlinx.coroutines.flow.Flow

interface PredictionRepository {
    fun getUserPredictions(userId: String, bolaoId: String): Flow<List<Prediction>>
    fun getBolaoAllPredictions(bolaoId: String): Flow<List<Prediction>>
    suspend fun getUserPredictionForMatch(userId: String, bolaoId: String, matchId: String): Prediction?
    suspend fun savePrediction(prediction: Prediction)
    suspend fun deleteUserPredictions(userId: String, bolaoId: String)
    fun getRanking(bolaoId: String, participantIds: List<String>): Flow<List<RankingEntry>>
}
