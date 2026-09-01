package com.lpstudio.bolaodagalera.presentation.match

import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class PredictionViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var predictionRepository: FakePredictionRepository
    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var viewModel: PredictionViewModel

    private val userId = "pauloricha"
    private val bolaoId = "bolao-1" // championshipId = LIBERTADORES (fake pré-existente)
    private val matchId = "match-1"

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        matchRepository = FakeMatchRepository()
        bolaoRepository = FakeBolaoRepository()
        predictionRepository = FakePredictionRepository(matchRepository)

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

        viewModel =
            PredictionViewModel(
                matchRepository,
                predictionRepository,
                bolaoRepository,
                FakeCrashReporter(),
                FakePerformanceMonitor(),
                bolaoId,
                matchId
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `load preenche jogo e bolao e nao ha palpite existente`() = runTest {
        viewModel.load(userId)

        val state = viewModel.uiState.value
        assertEquals(matchId, state.match?.id)
        assertEquals(bolaoId, state.bolao?.id)
        assertNull(state.existingPrediction)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `load com jogo inexistente mostra erro`() = runTest {
        val brokenViewModel =
            PredictionViewModel(
                matchRepository,
                predictionRepository,
                bolaoRepository,
                FakeCrashReporter(),
                FakePerformanceMonitor(),
                bolaoId,
                "match-que-nao-existe"
            )

        brokenViewModel.load(userId)

        val state = brokenViewModel.uiState.value
        assertNotNull(state.error)
        assertFalse(state.isLoading)
    }

    @Test
    fun `savePrediction cria um novo palpite`() = runTest {
        viewModel.savePrediction(userId, homeScore = 2, awayScore = 1)

        val state = viewModel.uiState.value
        assertTrue(state.isSaved)
        assertFalse(state.isLoading)
        assertNull(state.error)

        val saved = predictionRepository.getUserPredictionForMatch(userId, bolaoId, matchId)
        assertEquals(2, saved?.homeScore)
        assertEquals(1, saved?.awayScore)
    }

    @Test
    fun `savePrediction sobrescreve palpite existente em vez de duplicar`() = runTest {
        viewModel.savePrediction(userId, homeScore = 1, awayScore = 1)
        viewModel.savePrediction(userId, homeScore = 3, awayScore = 0)

        val allPredictions = predictionRepository.getUserPredictions(userId, bolaoId)
        val saved = predictionRepository.getUserPredictionForMatch(userId, bolaoId, matchId)

        assertEquals(3, saved?.homeScore)
        assertEquals(0, saved?.awayScore)
    }

    @Test
    fun `load recarrega palpite existente apos salvar`() = runTest {
        viewModel.savePrediction(userId, homeScore = 2, awayScore = 2)

        viewModel.load(userId)

        val state = viewModel.uiState.value
        assertEquals(2, state.existingPrediction?.homeScore)
        assertEquals(2, state.existingPrediction?.awayScore)
    }
}
