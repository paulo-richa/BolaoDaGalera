package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class FakeMatchRepositoryTest {
    private val repository = FakeMatchRepository()

    @Test
    fun `getMatches should return list of matches`() = runTest {
        val matches = repository.getMatches("LIBERTADORES").first()
        assertTrue(matches.isEmpty()) // Currently the seed is empty
    }

    @Test
    fun `updateMatchScore should update scores and set isManual to true`() = runTest {
        val match =
            Match(
                id = "M1",
                homeTeam = "Palmeiras",
                awayTeam = "Flamengo",
                homeTeamCode = "PAL",
                awayTeamCode = "FLA",
                homeTeamFlag = "🐷",
                awayTeamFlag = "🔴",
                matchDateMillis = 0L,
                phase = Phase.GROUP_STAGE,
                championshipId = "LIBERTADORES"
            )
        repository.upsertMatch(match)

        repository.updateMatchScore("LIBERTADORES", "M1", 5, 0)

        val updatedMatch = repository.getMatch("LIBERTADORES", "M1")
        assertEquals(5, updatedMatch.homeScore)
        assertEquals(0, updatedMatch.awayScore)
        assertTrue(updatedMatch.isManual)
    }

    @Test
    fun `updateMatchTeams should update teams and set isManual to true`() = runTest {
        val match =
            Match(
                id = "M2",
                homeTeam = "TBD",
                awayTeam = "TBD",
                homeTeamCode = "TBD",
                awayTeamCode = "TBD",
                homeTeamFlag = "🏳️",
                awayTeamFlag = "🏳️",
                matchDateMillis = 0L,
                phase = Phase.FINAL,
                championshipId = "LIBERTADORES"
            )
        repository.upsertMatch(match)

        repository.updateMatchTeams(
            championshipId = "LIBERTADORES",
            matchId = "M2",
            homeTeam = "River Plate",
            homeTeamCode = "RIV",
            homeTeamFlag = "🇦🇷",
            awayTeam = "Boca Juniors",
            awayTeamCode = "BOC",
            awayTeamFlag = "🟦",
            dateMillis = 123456789L,
            status = "FINISHED"
        )

        val updatedMatch = repository.getMatch("LIBERTADORES", "M2")
        assertEquals("River Plate", updatedMatch.homeTeam)
        assertEquals("Boca Juniors", updatedMatch.awayTeam)
        assertEquals(123456789L, updatedMatch.matchDateMillis)
        assertEquals("FINISHED", updatedMatch.status)
        assertTrue(updatedMatch.isManual)
    }
}
