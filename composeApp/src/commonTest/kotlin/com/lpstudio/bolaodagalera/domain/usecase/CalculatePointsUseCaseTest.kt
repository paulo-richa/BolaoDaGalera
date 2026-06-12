package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Prediction
import kotlin.test.Test
import kotlin.test.assertEquals

class CalculatePointsUseCaseTest {

    private val calculatePointsUseCase = CalculatePointsUseCase()

    @Test
    fun `should return exact points when score matches perfectly`() {
        val prediction = Prediction(id = "1", userId = "u1", matchId = "m1", homeScore = 2, awayScore = 1)
        val result = calculatePointsUseCase(prediction, actualHome = 2, actualAway = 1)
        assertEquals(3, result)
    }

    @Test
    fun `should return winner points when result matches but not exact score`() {
        val prediction = Prediction(id = "1", userId = "u1", matchId = "m1", homeScore = 1, awayScore = 0)
        val result = calculatePointsUseCase(prediction, actualHome = 2, actualAway = 1)
        assertEquals(1, result)
    }

    @Test
    fun `should return winner points when draw matches but not exact score`() {
        val prediction = Prediction(id = "1", userId = "u1", matchId = "m1", homeScore = 1, awayScore = 1)
        val result = calculatePointsUseCase(prediction, actualHome = 2, actualAway = 2)
        assertEquals(1, result)
    }

    @Test
    fun `should return zero points when result is completely wrong`() {
        val prediction = Prediction(id = "1", userId = "u1", matchId = "m1", homeScore = 0, awayScore = 1)
        val result = calculatePointsUseCase(prediction, actualHome = 2, actualAway = 0)
        assertEquals(0, result)
    }

    @Test
    fun `should use custom points if provided`() {
        val prediction = Prediction(id = "1", userId = "u1", matchId = "m1", homeScore = 2, awayScore = 1)
        val result = calculatePointsUseCase(prediction, actualHome = 2, actualAway = 1, pointsExact = 5, pointsWinnerOrDraw = 2)
        assertEquals(5, result)

        val prediction2 = Prediction(id = "2", userId = "u1", matchId = "m2", homeScore = 1, awayScore = 0)
        val result2 = calculatePointsUseCase(prediction2, actualHome = 2, actualAway = 1, pointsExact = 5, pointsWinnerOrDraw = 2)
        assertEquals(2, result2)
    }
}
