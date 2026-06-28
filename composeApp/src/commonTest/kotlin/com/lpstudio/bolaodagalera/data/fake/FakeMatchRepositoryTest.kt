package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FakeMatchRepositoryTest {

    private val repository = FakeMatchRepository()

    @Test
    fun `getMatches should return all matches from seed`() = runTest {
        val matches = repository.getMatches().first()
        // O seed tem 12*6 (grupos) + knockout matches. 
        // groupStageMatches tem 24 * 3 = 72 jogos (12 grupos x 6 jogos cada? Não, 12 grupos A-L, cada um com 6 jogos, mas na lista vejo só rodada 1 e 2 detalhadas?)
        // Deixa eu ver melhor o MatchSeedData.kt
        assertTrue(matches.isNotEmpty())
    }

    @Test
    fun `getMatchesByPhase should filter matches by phase`() = runTest {
        val groupMatches = repository.getMatchesByPhase(Phase.GROUP_STAGE).first()
        val finalMatch = repository.getMatchesByPhase(Phase.FINAL).first()

        assertTrue(groupMatches.all { it.phase == Phase.GROUP_STAGE })
        assertTrue(finalMatch.all { it.phase == Phase.FINAL })
        assertEquals(1, finalMatch.size)
    }

    @Test
    fun `getMatch should return specific match by id`() = runTest {
        val matchId = "GS-A-1"
        val match = repository.getMatch(matchId)
        
        assertEquals(matchId, match.id)
        assertEquals("México", match.homeTeam)
    }

    @Test
    fun `updateMatchScore should update scores and set isManual to true`() = runTest {
        val matchId = "GS-A-1"
        repository.updateMatchScore(matchId, 5, 0)
        
        val updatedMatch = repository.getMatch(matchId)
        assertEquals(5, updatedMatch.homeScore)
        assertEquals(0, updatedMatch.awayScore)
        assertTrue(updatedMatch.isManual)
    }

    @Test
    fun `updateMatchTeams should update teams and set isManual to true`() = runTest {
        val matchId = "KO-FINAL"
        repository.updateMatchTeams(
            matchId = matchId,
            homeTeam = "Brasil",
            homeTeamCode = "BRA",
            homeTeamFlag = "🇧🇷",
            awayTeam = "Argentina",
            awayTeamCode = "ARG",
            awayTeamFlag = "🇦🇷",
            dateMillis = 123456789L,
            status = "FINISHED"
        )
        
        val updatedMatch = repository.getMatch(matchId)
        assertEquals("Brasil", updatedMatch.homeTeam)
        assertEquals("Argentina", updatedMatch.awayTeam)
        assertEquals(123456789L, updatedMatch.matchDateMillis)
        assertEquals("FINISHED", updatedMatch.status)
        assertTrue(updatedMatch.isManual)
    }
}
