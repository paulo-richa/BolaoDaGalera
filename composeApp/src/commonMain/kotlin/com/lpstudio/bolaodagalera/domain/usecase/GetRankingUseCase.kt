package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User

class GetRankingUseCase(
    private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()
) {
    operator fun invoke(
        bolao: Bolao,
        predictions: List<Prediction>,
        matches: List<Match>,
        users: List<User>
    ): List<RankingEntry> {
        val userMap = users.associateBy { it.id }
        
        // Group predictions by user
        val userPredictions = predictions.groupBy { it.userId }
        val matchMap = matches.associateBy { it.id }

        return bolao.participants.map { userId ->
            val user = userMap[userId]
            val preds = userPredictions[userId] ?: emptyList()
            
            var totalPoints = 0
            var exactScores = 0
            var correctResults = 0

            preds.forEach { pred ->
                val match = matchMap[pred.matchId]
                if (match != null && match.homeScore != null && match.awayScore != null) {
                    val pts = calculatePointsUseCase(
                        prediction = pred,
                        actualHome = match.homeScore,
                        actualAway = match.awayScore,
                        pointsExact = bolao.pointsExactScore,
                        pointsWinnerOrDraw = bolao.pointsWinnerOrDraw
                    )
                    
                    totalPoints += pts
                    
                    if (pts == bolao.pointsExactScore) {
                        exactScores++
                        correctResults++ // If exact, it's also a correct result
                    } else if (pts == bolao.pointsWinnerOrDraw) {
                        correctResults++
                    }
                }
            }

            RankingEntry(
                userId = userId,
                userName = user?.name ?: "Usuário",
                userNickname = user?.nickname ?: "",
                points = totalPoints,
                exactScores = exactScores,
                correctResults = correctResults
            )
        }.sortedWith(
            compareByDescending<RankingEntry> { it.points }
                .thenByDescending { it.exactScores }
                .thenByDescending { it.correctResults }
                .thenBy { it.userName.lowercase() }
        )
    }
}
