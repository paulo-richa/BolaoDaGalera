package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
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
class CreateBolaoViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var viewModel: CreateBolaoViewModel

    private val now = System.currentTimeMillis()
    private val futureMillis = now + 7 * 24 * 3_600_000L
    private val pastMillis = now - 7 * 24 * 3_600_000L

    private fun match(id: String, championshipId: String, phase: Phase, dateMillis: Long) = Match(
        id = id,
        homeTeam = "Time A",
        awayTeam = "Time B",
        homeTeamCode = "TMA",
        awayTeamCode = "TMB",
        homeTeamFlag = "",
        awayTeamFlag = "",
        matchDateMillis = dateMillis,
        phase = phase,
        championshipId = championshipId
    )

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        bolaoRepository = FakeBolaoRepository()
        matchRepository = FakeMatchRepository()
        viewModel =
            CreateBolaoViewModel(bolaoRepository, authRepository, matchRepository, FakeCrashReporter(), FakePerformanceMonitor())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ---------- DISPONIBILIDADE DE FASES ----------

    @Test
    fun `isPhaseAvailable retorna false quando nao ha jogos da fase`() {
        assertFalse(viewModel.isPhaseAvailable("LIBERTADORES", Phase.GROUP_STAGE))
    }

    @Test
    fun `isPhaseAvailable retorna true quando todos os jogos da fase sao futuros`() = runTest {
        matchRepository.upsertMatch(match("m1", "LIBERTADORES", Phase.GROUP_STAGE, futureMillis))
        matchRepository.upsertMatch(match("m2", "LIBERTADORES", Phase.GROUP_STAGE, futureMillis))

        assertTrue(viewModel.isPhaseAvailable("LIBERTADORES", Phase.GROUP_STAGE))
    }

    @Test
    fun `isPhaseAvailable retorna false quando algum jogo da fase ja comecou`() = runTest {
        matchRepository.upsertMatch(match("m1", "LIBERTADORES", Phase.GROUP_STAGE, futureMillis))
        matchRepository.upsertMatch(match("m2", "LIBERTADORES", Phase.GROUP_STAGE, pastMillis))

        assertFalse(viewModel.isPhaseAvailable("LIBERTADORES", Phase.GROUP_STAGE))
    }

    @Test
    fun `isKnockoutAvailable ignora jogos de fase de grupos e amistosos`() = runTest {
        // Grupos já começaram (passado), mas o mata-mata ainda não
        matchRepository.upsertMatch(match("m1", "LIBERTADORES", Phase.GROUP_STAGE, pastMillis))
        matchRepository.upsertMatch(match("m2", "LIBERTADORES", Phase.ROUND_OF_16, futureMillis))

        assertTrue(viewModel.isKnockoutAvailable("LIBERTADORES"))
    }

    @Test
    fun `isKnockoutAvailable retorna false quando mata-mata ja comecou`() = runTest {
        matchRepository.upsertMatch(match("m1", "LIBERTADORES", Phase.ROUND_OF_16, pastMillis))

        assertFalse(viewModel.isKnockoutAvailable("LIBERTADORES"))
    }

    // ---------- CRIAÇÃO DO BOLÃO ----------

    @Test
    fun `create com sucesso preenche createdBolao com o codigo gerado`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.create(
            name = "Bolão dos Amigos",
            description = "Só para galera fechada",
            championshipId = "LIBERTADORES",
            scope = BolaoScope.FULL,
            specificMatchId = null,
            pointsExact = 3,
            pointsWinner = 1
        )

        val state = viewModel.uiState.value
        assertNotNull(state.createdBolao)
        assertEquals("Bolão dos Amigos", state.createdBolao.name)
        assertEquals(FAKE_USER.id, state.createdBolao.ownerId)
        assertTrue(state.createdBolao.code.isNotBlank())
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    @Test
    fun `create sem usuario logado nao faz nada`() = runTest {
        authRepository.setUser(null)

        viewModel.create(
            name = "Bolão dos Amigos",
            description = "",
            championshipId = "LIBERTADORES",
            scope = BolaoScope.FULL,
            specificMatchId = null,
            pointsExact = 3,
            pointsWinner = 1
        )

        assertNull(viewModel.uiState.value.createdBolao)
    }

    @Test
    fun `create com falha no repositorio mostra mensagem de erro`() = runTest {
        authRepository.setUser(FAKE_USER)
        bolaoRepository.createBolaoException = Exception("Falha ao salvar")

        viewModel.create(
            name = "Bolão dos Amigos",
            description = "",
            championshipId = "LIBERTADORES",
            scope = BolaoScope.FULL,
            specificMatchId = null,
            pointsExact = 3,
            pointsWinner = 1
        )

        val state = viewModel.uiState.value
        assertNull(state.createdBolao)
        assertEquals("Falha ao salvar", state.error)
        assertFalse(state.isLoading)
    }
}
