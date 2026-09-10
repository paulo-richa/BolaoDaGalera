package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoDetailScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.core.context.GlobalContext

private const val FAR_FUTURE_MILLIS = 4_000_000_000_000L // year ~2096, never "today"/finished

class BolaoDetailUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        Championship.setCache(
            listOf(
                Championship(
                    id = "LIBERTADORES",
                    displayName = "Libertadores",
                    emoji = "🏆",
                    apiCode = "CLI",
                    isGroupsAndKnockout = true,
                    isAvailable = true
                )
            )
        )

        val matchRepository = GlobalContext.get().get<MatchRepository>()
        runBlocking {
            matchRepository.upsertMatch(
                Match(
                    id = "match-R1-A1",
                    homeTeam = "Palmeiras",
                    awayTeam = "Boca Juniors",
                    homeTeamCode = "PAL",
                    awayTeamCode = "BOC",
                    homeTeamFlag = "🐷",
                    awayTeamFlag = "🔵",
                    matchDateMillis = FAR_FUTURE_MILLIS,
                    phase = Phase.GROUP_STAGE,
                    group = "A",
                    championshipId = "LIBERTADORES"
                )
            )
            matchRepository.upsertMatch(
                Match(
                    id = "match-R2-A1",
                    homeTeam = "Palmeiras",
                    awayTeam = "River Plate",
                    homeTeamCode = "PAL",
                    awayTeamCode = "RIV",
                    homeTeamFlag = "🐷",
                    awayTeamFlag = "⚪",
                    matchDateMillis = FAR_FUTURE_MILLIS + 1_000_000L,
                    phase = Phase.GROUP_STAGE,
                    group = "A",
                    championshipId = "LIBERTADORES"
                )
            )
        }
    }

    @Test
    fun groupStage_autoExpandsNearestGroupAndFiltersByRound() {
        composeTestRule.setContent {
            AppTheme {
                BolaoDetailScreen(
                    bolaoId = "bolao-1",
                    onNavigateToPrediction = { },
                    onNavigateToAllPredictions = { },
                    onNavigateToEdit = { },
                    onNavigateToAddParticipants = { },
                    onNavigateToHelp = { },
                    onNavigateBack = { }
                )
            }
        }

        // The single group with the nearest upcoming match auto-expands on load.
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Grupo A", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("Palmeiras", substring = true).performScrollTo().assertIsDisplayed()

        // Selecting "Rodada 2" filters matches down to that round - River Plate (round 2)
        // replaces Boca Juniors (round 1) once the round switch takes effect.
        composeTestRule.onNodeWithText("Rodada 2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("River Plate", substring = true).performScrollTo().assertIsDisplayed()
    }
}
