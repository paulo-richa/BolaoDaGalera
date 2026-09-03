package com.lpstudio.bolaodagalera.presentation.bolao

import app.cash.turbine.test
import com.lpstudio.bolaodagalera.data.fake.FakeAnalyticsTracker
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.observability.Telemetry
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class BolaoViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var predictionRepository: FakePredictionRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: BolaoViewModel

    @BeforeTest
    fun setup() = runTest {
        Dispatchers.setMain(testDispatcher)

        authRepository = FakeAuthRepository()
        matchRepository = FakeMatchRepository()
        predictionRepository = FakePredictionRepository(matchRepository)
        bolaoRepository = FakeBolaoRepository()

        // Seed a few matches into the repository so the UI isn't empty and assertions don't fail
        matchRepository.upsertMatch(
            Match(
                id = "M1",
                homeTeam = "Palmeiras",
                awayTeam = "Flamengo",
                homeTeamCode = "PAL",
                awayTeamCode = "FLA",
                homeTeamFlag = "🐷",
                awayTeamFlag = "🔴",
                matchDateMillis = 1781136000000L + 10000,
                phase = Phase.GROUP_STAGE,
                championshipId = "LIBERTADORES"
            )
        )

        viewModel =
            BolaoViewModel(
                bolaoRepository,
                matchRepository,
                predictionRepository,
                authRepository,
                "bolao-1",
                FakeCrashReporter(),
                Telemetry(FakePerformanceMonitor(), FakeAnalyticsTracker())
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load should populate matches and stop loading`() = runTest {
        val state = viewModel.uiState.value
        assertFalse(state.matches.isEmpty())
        assertEquals("bolao-1", state.bolao?.id)
        assertFalse(state.isLoading)
    }

    @Test
    fun `setting user id should refresh predictions but maintain matches`() = runTest {
        viewModel.setUserId("pauloricha")
        val state = viewModel.uiState.value
        assertFalse(state.matches.isEmpty())
        assertFalse(state.isLoading)
    }

    // ---------- POOL MANAGEMENT (OWNER) ----------

    @Test
    fun `aprovar pedido de entrada adiciona o usuario aos participantes`() = runTest {
        bolaoRepository.requestJoinBolao("LIB026", "novo-user")

        viewModel.approveParticipant("novo-user", approve = true)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertTrue("novo-user" in bolao.participants)
        assertFalse("novo-user" in bolao.pendingParticipants)
    }

    @Test
    fun `rejeitar pedido de entrada nao adiciona aos participantes`() = runTest {
        bolaoRepository.requestJoinBolao("LIB026", "novo-user")

        viewModel.approveParticipant("novo-user", approve = false)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse("novo-user" in bolao.participants)
        assertFalse("novo-user" in bolao.pendingParticipants)
    }

    @Test
    fun `aprovar pedido de saida remove o usuario dos participantes`() = runTest {
        bolaoRepository.requestJoinBolao("LIB026", "sairemos")
        bolaoRepository.approveJoinRequest("bolao-1", "sairemos", approve = true)
        bolaoRepository.requestLeaveBolao("bolao-1", "sairemos")

        viewModel.approveLeaveRequest("sairemos", approve = true)

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse("sairemos" in bolao.participants)
    }

    @Test
    fun `dono sai do bolao usando leaveBolao em vez de requestLeaveBolao`() = runTest {
        // FAKE_USER (pauloricha) is the owner of bolao-1 in the default fixture
        viewModel.leaveBolao()

        val state = viewModel.uiState.value
        assertTrue(state.isLeaveSuccess)
        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse("pauloricha" in bolao.participants)
    }

    @Test
    fun `uma nova partida no repositorio reflete reativamente no uiState, sem chamar o viewmodel`() = runTest {
        viewModel.uiState.test {
            assertEquals(1, awaitItem().allMatches.size)

            // Simulates a Cloud Function syncing a new fixture - the ViewModel never calls
            // into the repository itself, it only observes matchRepository.getMatches().
            matchRepository.upsertMatch(
                Match(
                    id = "m2",
                    homeTeam = "River Plate",
                    awayTeam = "Boca Juniors",
                    homeTeamCode = "RIV",
                    awayTeamCode = "BOC",
                    homeTeamFlag = "",
                    awayTeamFlag = "",
                    matchDateMillis = 1_700_100_000_000L,
                    phase = Phase.GROUP_STAGE,
                    championshipId = "LIBERTADORES"
                )
            )
            val updated = awaitItem()
            assertEquals(2, updated.allMatches.size)
            assertTrue(updated.allMatches.any { it.id == "m2" })

            cancelAndIgnoreRemainingEvents()
        }
    }
}
