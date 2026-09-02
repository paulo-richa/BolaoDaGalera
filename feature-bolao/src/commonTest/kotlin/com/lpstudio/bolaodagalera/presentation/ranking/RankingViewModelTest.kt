package com.lpstudio.bolaodagalera.presentation.ranking

import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var predictionRepository: FakePredictionRepository
    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: RankingViewModel

    private val matchId = "m1"

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        authRepository.setUser(FAKE_USER)
        matchRepository = FakeMatchRepository()
        bolaoRepository = FakeBolaoRepository()
        predictionRepository = FakePredictionRepository(matchRepository)

        // bolao-1 (fixture padrão): championshipId=LIBERTADORES, pointsExactScore=3, pointsWinnerOrDraw=1
        matchRepository.upsertMatch(
            Match(
                id = matchId,
                homeTeam = "Palmeiras",
                awayTeam = "Flamengo",
                homeTeamCode = "PAL",
                awayTeamCode = "FLA",
                homeTeamFlag = "",
                awayTeamFlag = "",
                matchDateMillis = 1_700_000_000_000L,
                phase = Phase.GROUP_STAGE,
                championshipId = "LIBERTADORES",
                homeScore = 2,
                awayScore = 1,
                status = "FINISHED"
            )
        )

        // pauloricha acerta o placar exato (3 pts)
        predictionRepository.savePrediction(
            Prediction(userId = "pauloricha", bolaoId = "bolao-1", matchId = matchId, homeScore = 2, awayScore = 1)
        )
        // u3 acerta só o vencedor (1 pt)
        predictionRepository.savePrediction(
            Prediction(userId = "u3", bolaoId = "bolao-1", matchId = matchId, homeScore = 1, awayScore = 0)
        )
        // u4 erra tudo (0 pts)
        predictionRepository.savePrediction(
            Prediction(userId = "u4", bolaoId = "bolao-1", matchId = matchId, homeScore = 0, awayScore = 0)
        )

        viewModel =
            RankingViewModel(
                predictionRepository = predictionRepository,
                bolaoRepository = bolaoRepository,
                matchRepository = matchRepository,
                authRepository = authRepository,
                crashReporter = FakeCrashReporter(),
                bolaoId = "bolao-1"
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `ranking ordena por pontos do maior para o menor`() = runTest {
        val entries = viewModel.uiState.value.entries
        val ordered = entries.map { it.userId to it.points }

        val posPaulo = entries.indexOfFirst { it.userId == "pauloricha" }
        val posU3 = entries.indexOfFirst { it.userId == "u3" }
        val posU4 = entries.indexOfFirst { it.userId == "u4" }

        assertEquals(3, entries.find { it.userId == "pauloricha" }?.points)
        assertEquals(1, entries.find { it.userId == "u3" }?.points)
        assertEquals(0, entries.find { it.userId == "u4" }?.points)
        assertTrue(posPaulo < posU3, "pauloricha (3 pts) deveria vir antes de u3 (1 pt): $ordered")
        assertTrue(posU3 < posU4, "u3 (1 pt) deveria vir antes de u4 (0 pts): $ordered")
    }

    @Test
    fun `selectParticipant retorna apenas os palpites que pontuaram`() = runTest {
        val entry = viewModel.uiState.value.entries.find { it.userId == "pauloricha" }!!

        viewModel.selectParticipant(entry)

        val hits = viewModel.uiState.value.selectedParticipantHits
        assertEquals(1, hits.size)
        assertEquals(matchId, hits.first().match.id)
        assertEquals(3, hits.first().points)
    }

    @Test
    fun `selectParticipant nao retorna palpites de quem nao pontuou`() = runTest {
        val entry = viewModel.uiState.value.entries.find { it.userId == "u4" }!!

        viewModel.selectParticipant(entry)

        assertEquals(0, viewModel.uiState.value.selectedParticipantHits.size)
    }

    @Test
    fun `clearSelectedParticipant limpa a selecao`() = runTest {
        val entry = viewModel.uiState.value.entries.find { it.userId == "pauloricha" }!!
        viewModel.selectParticipant(entry)

        viewModel.clearSelectedParticipant()

        val state = viewModel.uiState.value
        assertEquals(0, state.selectedParticipantHits.size)
        assertEquals("", state.selectedParticipantName)
    }
}
