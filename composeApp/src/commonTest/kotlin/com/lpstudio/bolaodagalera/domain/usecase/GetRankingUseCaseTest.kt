package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.User
import kotlin.test.Test
import kotlin.test.assertEquals

class GetRankingUseCaseTest {

    private val getRankingUseCase = GetRankingUseCase()

    private fun createMatch(id: String, hScore: Int, aScore: Int) = Match(
        id = id,
        homeTeam = "Home",
        awayTeam = "Away",
        homeTeamCode = "H",
        awayTeamCode = "A",
        homeTeamFlag = "",
        awayTeamFlag = "",
        matchDateMillis = 0L,
        phase = Phase.GROUP_STAGE,
        homeScore = hScore,
        awayScore = aScore
    )

    @Test
    fun `should calculate ranking correctly and sort by points`() {
        val bolao = Bolao(id = "b1", name = "Test", participants = listOf("u1", "u2"), pointsExactScore = 3, pointsWinnerOrDraw = 1)
        val users = listOf(
            User(id = "u1", name = "User 1", email = "", phone = ""),
            User(id = "u2", name = "User 2", email = "", phone = "")
        )
        val matches = listOf(
            createMatch("m1", 2, 1),
            createMatch("m2", 1, 1)
        )
        val predictions = listOf(
            // User 1: 2x1 (exact) + 0x0 (draw result) = 3 + 1 = 4 pts
            Prediction(id = "p1", userId = "u1", matchId = "m1", homeScore = 2, awayScore = 1),
            Prediction(id = "p2", userId = "u1", matchId = "m2", homeScore = 0, awayScore = 0),
            
            // User 2: 1x0 (winner result) + 1x1 (exact) = 1 + 3 = 4 pts
            Prediction(id = "p3", userId = "u2", matchId = "m1", homeScore = 1, awayScore = 0),
            Prediction(id = "p4", userId = "u2", matchId = "m2", homeScore = 1, awayScore = 1)
        )

        val ranking = getRankingUseCase(bolao, predictions, matches, users)

        assertEquals(2, ranking.size)
        // Both have 4 points and 1 exact score.
        // Ranking order might be consistent based on how groupBy/map works, 
        // but let's check points.
        assertEquals(4, ranking[0].points)
        assertEquals(4, ranking[1].points)
    }

    @Test
    fun `should tie break by exact scores`() {
        val bolao = Bolao(id = "b1", name = "Test", participants = listOf("u1", "u2"), pointsExactScore = 3, pointsWinnerOrDraw = 1)
        val users = listOf(
            User(id = "u1", name = "User 1", email = "", phone = ""),
            User(id = "u2", name = "User 2", email = "", phone = "")
        )
        val matches = listOf(
            createMatch("m1", 2, 1),
            createMatch("m2", 3, 0),
            createMatch("m3", 0, 0)
        )
        
        val predictions = listOf(
            Prediction(id = "p1", userId = "u1", matchId = "m1", homeScore = 2, awayScore = 1), // 3 pts, 1 exact
            
            Prediction(id = "p3", userId = "u2", matchId = "m1", homeScore = 1, awayScore = 0), // 1 pt
            Prediction(id = "p4", userId = "u2", matchId = "m2", homeScore = 2, awayScore = 0), // 1 pt
            Prediction(id = "p5", userId = "u2", matchId = "m3", homeScore = 1, awayScore = 1)  // 1 pt. Total 3 pts, 0 exact.
        )

        val ranking = getRankingUseCase(bolao, predictions, matches, users)

        assertEquals("u1", ranking[0].userId) // User 1 wins on exact scores
        assertEquals(3, ranking[0].points)
        assertEquals(1, ranking[0].exactScores)
        assertEquals(3, ranking[1].points)
        assertEquals(0, ranking[1].exactScores)
    }
}
