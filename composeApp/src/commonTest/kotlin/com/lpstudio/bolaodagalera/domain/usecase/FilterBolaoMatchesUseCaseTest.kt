package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FilterBolaoMatchesUseCaseTest {
    private val useCase = FilterBolaoMatchesUseCase()

    private fun match(
        id: String,
        phase: Phase = Phase.GROUP_STAGE,
        homeTeamCode: String = "PAL",
        awayTeamCode: String = "FLA",
        homeTeam: String = "Palmeiras",
        awayTeam: String = "Flamengo",
        matchDateMillis: Long = 1_000_000L,
        status: String? = null,
        homeScore: Int? = null,
        matchOrder: Int = 0
    ) = Match(
        id = id,
        homeTeam = homeTeam,
        awayTeam = awayTeam,
        homeTeamCode = homeTeamCode,
        awayTeamCode = awayTeamCode,
        homeTeamFlag = "🐷",
        awayTeamFlag = "🔴",
        matchDateMillis = matchDateMillis,
        phase = phase,
        status = status,
        homeScore = homeScore,
        matchOrder = matchOrder
    )

    @AfterTest
    fun tearDown() {
        Championship.setCache(emptyList())
    }

    @Test
    fun `escopo ONLY_GROUPS remove jogos de mata-mata`() {
        val bolao = Bolao(scope = BolaoScope.ONLY_GROUPS)
        val matches = listOf(
            match("G1", phase = Phase.GROUP_STAGE),
            match("K1", phase = Phase.QUARTERFINALS)
        )

        val result = useCase(bolao, matches)

        assertEquals(listOf("G1"), result.map { it.id })
    }

    @Test
    fun `escopo ONLY_KNOCKOUT remove jogos de fase de grupos`() {
        val bolao = Bolao(scope = BolaoScope.ONLY_KNOCKOUT)
        val matches = listOf(
            match("G1", phase = Phase.GROUP_STAGE),
            match("K1", phase = Phase.QUARTERFINALS)
        )

        val result = useCase(bolao, matches)

        assertEquals(listOf("K1"), result.map { it.id })
    }

    @Test
    fun `specificMatchId restringe a um unico jogo mesmo com outro escopo`() {
        val bolao = Bolao(scope = BolaoScope.FULL, specificMatchId = "G1")
        val matches = listOf(
            match("G1", phase = Phase.GROUP_STAGE),
            match("K1", phase = Phase.QUARTERFINALS)
        )

        val result = useCase(bolao, matches)

        assertEquals(listOf("G1"), result.map { it.id })
    }

    @Test
    fun `dedupe de fase de grupos mantem apenas o jogo mais avancado do mesmo confronto`() {
        val bolao = Bolao()
        val matches = listOf(
            // "GS-A-1" and "GS-A-2" fall into the same round (groupRound() maps 1,2 -> 1),
            // simulating the same matchup reappearing with a different id.
            match("GS-A-1", phase = Phase.GROUP_STAGE, homeTeamCode = "PAL", awayTeamCode = "FLA", status = null),
            match("GS-A-2", phase = Phase.GROUP_STAGE, homeTeamCode = "PAL", awayTeamCode = "FLA", status = "FINISHED")
        )

        val result = useCase(bolao, matches)

        assertEquals(listOf("GS-A-2"), result.map { it.id })
    }

    @Test
    fun `dedupe de mata-mata agrupa por times e mantem a perna correta separada`() {
        val bolao = Bolao()
        val matches = listOf(
            match(
                "QF1-L1",
                phase = Phase.QUARTERFINALS,
                homeTeamCode = "PAL",
                awayTeamCode = "FLA",
                homeTeam = "Palmeiras",
                awayTeam = "Flamengo"
            ),
            match(
                "QF1-L1-dup",
                phase = Phase.QUARTERFINALS,
                homeTeamCode = "PAL",
                awayTeamCode = "FLA",
                homeTeam = "Palmeiras",
                awayTeam = "Flamengo",
                status = "FINISHED"
            ),
            match(
                "QF1-L2",
                phase = Phase.QUARTERFINALS,
                homeTeamCode = "FLA",
                awayTeamCode = "PAL",
                homeTeam = "Flamengo",
                awayTeam = "Palmeiras"
            )
        )

        val result = useCase(bolao, matches).map { it.id }.toSet()

        assertEquals(setOf("QF1-L1-dup", "QF1-L2"), result)
    }

    @Test
    fun `mata-mata com times TBD nao colapsa confrontos distintos`() {
        val bolao = Bolao()
        val matches = listOf(
            match("QF1", phase = Phase.QUARTERFINALS, homeTeamCode = "TBD", awayTeamCode = "TBD", matchOrder = 1),
            match("QF2", phase = Phase.QUARTERFINALS, homeTeamCode = "TBD", awayTeamCode = "TBD", matchOrder = 2)
        )

        val result = useCase(bolao, matches).map { it.id }.toSet()

        assertEquals(setOf("QF1", "QF2"), result)
    }

    @Test
    fun `campeonato pontos corridos corta rodadas ja quase todas encerradas antes da criacao do bolao`() {
        Championship.setCache(listOf(Championship(id = "BRASILEIRAO", isPointsBased = true)))
        val bolao = Bolao(championshipId = "BRASILEIRAO", createdAtMillis = 100_000L)
        val matches = listOf(
            // Round 1: both matches already happened before the bolao was created (>50% finished).
            match("M-R1-1", matchDateMillis = 1_000L),
            match("M-R1-2", matchDateMillis = 2_000L),
            // Round 2: not started yet.
            match("M-R2-1", matchDateMillis = 200_000L)
        )

        val result = useCase(bolao, matches)

        assertEquals(listOf("M-R2-1"), result.map { it.id })
    }

    @Test
    fun `campeonato que nao e pontos corridos ignora o corte de rodada`() {
        Championship.setCache(listOf(Championship(id = "LIBERTADORES", isPointsBased = false)))
        val bolao = Bolao(championshipId = "LIBERTADORES", createdAtMillis = 100_000L)
        val matches = listOf(match("M1", matchDateMillis = 1_000L))

        val result = useCase(bolao, matches)

        assertEquals(listOf("M1"), result.map { it.id })
    }
}
