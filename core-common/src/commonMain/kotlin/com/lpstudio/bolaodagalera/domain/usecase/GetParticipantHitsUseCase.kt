package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.ParticipantHit
import com.lpstudio.bolaodagalera.domain.model.Prediction

/**
 * Builds a participant's scored-prediction history (their "hits") for the ranking
 * detail view: only predictions on already-finished matches that scored points,
 * newest first.
 */
class GetParticipantHitsUseCase(private val calculatePointsUseCase: CalculatePointsUseCase = CalculatePointsUseCase()) {
    operator fun invoke(userId: String, allPredictions: List<Prediction>, allMatches: List<Match>, bolao: Bolao): List<ParticipantHit> =
        allPredictions
            .filter { it.userId == userId }
            .mapNotNull { pred ->
                val match = allMatches.find { it.id == pred.matchId }
                if (match != null && match.homeScore != null && match.awayScore != null) {
                    val points =
                        pred.points ?: calculatePointsUseCase(
                            pred,
                            match.homeScore,
                            match.awayScore,
                            bolao.pointsExactScore,
                            bolao.pointsWinnerOrDraw
                        )
                    if (points > 0) ParticipantHit(match, pred, points) else null
                } else {
                    null
                }
            }
            .sortedByDescending { it.match.matchDateMillis }
}
