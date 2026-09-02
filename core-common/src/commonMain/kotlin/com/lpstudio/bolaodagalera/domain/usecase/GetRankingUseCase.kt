package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User

/**
 * Builds the "live" ranking for a pool: for each prediction already scored by
 * the Cloud Function (Prediction.points != null), uses the server's official
 * value; for predictions on already-finished matches the function hasn't
 * processed yet, estimates locally with the same rule (CalculatePointsUseCase)
 * until the official value arrives. This avoids the displayed ranking
 * depending solely on client-recalculated points (which could diverge from
 * scoring.js on the server) without sacrificing near-instant updates when a
 * manual score is edited.
 */
class GetRankingUseCase(private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()) {
    operator fun invoke(bolao: Bolao, predictions: List<Prediction>, matches: List<Match>, users: List<User>): List<RankingEntry> {
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
                    val pts =
                        pred.points ?: calculatePointsUseCase(
                            prediction = pred,
                            actualHome = match.homeScore,
                            actualAway = match.awayScore,
                            pointsExact = bolao.pointsExactScore,
                            pointsWinnerOrDraw = bolao.pointsWinnerOrDraw
                        )

                    totalPoints += pts

                    if (pts == bolao.pointsExactScore) {
                        exactScores++
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
