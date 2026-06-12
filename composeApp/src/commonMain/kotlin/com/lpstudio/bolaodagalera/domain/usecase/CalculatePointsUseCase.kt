package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Prediction

class CalculatePointsUseCase {
    operator fun invoke(
        prediction: Prediction,
        actualHome: Int,
        actualAway: Int,
        pointsExact: Int = 3,
        pointsWinnerOrDraw: Int = 1
    ): Int {
        val isExact = prediction.homeScore == actualHome && prediction.awayScore == actualAway
        if (isExact) return pointsExact

        val predictedResult = MatchResult.fromScores(prediction.homeScore, prediction.awayScore)
        val actualResult = MatchResult.fromScores(actualHome, actualAway)

        return if (predictedResult == actualResult) pointsWinnerOrDraw else 0
    }
}

enum class MatchResult {
    HOME_WIN, AWAY_WIN, DRAW;

    companion object {
        fun fromScores(home: Int, away: Int) = when {
            home > away -> HOME_WIN
            home < away -> AWAY_WIN
            else -> DRAW
        }
    }
}
