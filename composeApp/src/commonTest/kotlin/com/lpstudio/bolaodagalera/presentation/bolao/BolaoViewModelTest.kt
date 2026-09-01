package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
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

        // Adiciona alguns jogos ao repositório para evitar que a UI fique vazia e falhe nos asserts
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
                FakeCrashReporter()
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

    // ---------- GESTÃO DO BOLÃO (DONO) ----------

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
        // FAKE_USER (pauloricha) é o dono do bolao-1 no fixture padrão
        viewModel.leaveBolao()

        val state = viewModel.uiState.value
        assertTrue(state.isLeaveSuccess)
        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse("pauloricha" in bolao.participants)
    }
}
