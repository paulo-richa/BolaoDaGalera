package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User

/**
 * Monta o ranking "ao vivo" de um bolão: para cada palpite já pontuado pela
 * Cloud Function (Prediction.points != null), usa o valor oficial do
 * servidor; para palpites de jogos já finalizados que a function ainda não
 * processou, estima localmente com a mesma regra (CalculatePointsUseCase) até
 * o valor oficial chegar. Evita que o ranking mostrado dependa só da
 * pontuação recalculada no cliente (que podia divergir de scoring.js no
 * servidor) sem sacrificar a atualização quase-instantânea ao editar um
 * placar manual.
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
