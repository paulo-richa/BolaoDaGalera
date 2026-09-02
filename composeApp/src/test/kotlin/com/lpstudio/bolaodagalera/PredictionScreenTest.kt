package com.lpstudio.bolaodagalera

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.presentation.match.PredictionScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/**
 * Testes de UI da tela de Palpite via Robolectric. Usa o bolão fake "bolao-1"
 * (championshipId = LIBERTADORES) e um jogo fake futuro entre Palmeiras e
 * Flamengo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class PredictionScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var predictionRepository: FakePredictionRepository
    private val matchId = "match-1"

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        stopKoin()

        matchRepository = FakeMatchRepository()
        predictionRepository = FakePredictionRepository(matchRepository)
        runBlocking {
            matchRepository.upsertMatch(
                Match(
                    id = matchId,
                    homeTeam = "Palmeiras",
                    awayTeam = "Flamengo",
                    homeTeamCode = "PAL",
                    awayTeamCode = "FLA",
                    homeTeamFlag = "",
                    awayTeamFlag = "",
                    matchDateMillis = System.currentTimeMillis() + 3_600_000L,
                    phase = Phase.GROUP_STAGE,
                    championshipId = "LIBERTADORES"
                )
            )
        }

        val authRepository = FakeAuthRepository()
        authRepository.setUser(FAKE_USER)

        startKoin {
            modules(
                fakeAppModule,
                module {
                    single<AuthRepository> { authRepository }
                    single<MatchRepository> { matchRepository }
                    single<PredictionRepository> { predictionRepository }
                }
            )
        }
    }

    @After
    fun tearDown() {
        stopKoin()
        Dispatchers.resetMain()
    }

    private fun setContent(onSaved: () -> Unit = {}) {
        composeTestRule.setContent {
            AppTheme {
                PredictionScreen(
                    bolaoId = "bolao-1",
                    matchId = matchId,
                    onSaved = onSaved,
                    onNavigateBack = {}
                )
            }
        }
    }

    @Test
    fun tela_mostra_nomes_dos_times_e_botao_salvar() {
        setContent()

        composeTestRule.onAllNodesWithText("Palmeiras")[0].assertIsDisplayed()
        composeTestRule.onAllNodesWithText("Flamengo")[0].assertIsDisplayed()
        composeTestRule.onNodeWithText("Salvar palpite").assertIsDisplayed()
    }

    @Test
    fun incrementar_placar_e_salvar_grava_o_palpite_correto() {
        setContent()

        // 2 clicks on the home team's "+" (index 0), 1 click on the away team's "+" (index 1)
        composeTestRule.onAllNodesWithText("+")[0].performClick()
        composeTestRule.onAllNodesWithText("+")[0].performClick()
        composeTestRule.onAllNodesWithText("+")[1].performClick()

        composeTestRule.onNodeWithText("Salvar palpite").performClick()
        composeTestRule.waitForIdle()

        val saved = runBlocking { predictionRepository.getUserPredictionForMatch(FAKE_USER.id, "bolao-1", matchId) }
        assert(saved?.homeScore == 2 && saved?.awayScore == 1) {
            "Esperava placar 2x1, veio ${saved?.homeScore}x${saved?.awayScore}"
        }
    }

    @Test
    fun palpite_existente_pre_preenche_placar_e_mostra_atualizar() {
        runBlocking {
            predictionRepository.savePrediction(
                com.lpstudio.bolaodagalera.domain.model.Prediction(
                    userId = FAKE_USER.id,
                    bolaoId = "bolao-1",
                    matchId = matchId,
                    homeScore = 3,
                    awayScore = 1
                )
            )
        }

        setContent()

        composeTestRule.onNodeWithText("Atualizar palpite").assertIsDisplayed()
        composeTestRule.onNodeWithText("3").assertIsDisplayed()
        composeTestRule.onNodeWithText("1").assertIsDisplayed()
    }
}
