package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAnalyticsTracker
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
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
class EditBolaoViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var viewModel: EditBolaoViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = FakeAuthRepository()
        authRepository.setUser(FAKE_USER) // dono do bolao-1
        bolaoRepository = FakeBolaoRepository()
        matchRepository = FakeMatchRepository()

        viewModel =
            EditBolaoViewModel(
                bolaoRepository,
                authRepository,
                matchRepository,
                "bolao-1",
                FakeCrashReporter(),
                FakePerformanceMonitor(),
                FakeAnalyticsTracker()
            )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `carrega o bolao e os participantes ao iniciar`() = runTest {
        val state = viewModel.uiState.value
        assertEquals("bolao-1", state.bolao?.id)
        assertFalse(state.isLoading)
        assertTrue(state.participants.isNotEmpty())
    }

    @Test
    fun `update altera nome descricao e pontuacao do bolao`() = runTest {
        viewModel.update(
            name = "Novo Nome do Bolão",
            description = "Nova descrição",
            scope = BolaoScope.ONLY_KNOCKOUT,
            pointsExact = 5,
            pointsWinner = 2
        )

        val state = viewModel.uiState.value
        assertEquals("Novo Nome do Bolão", state.bolao?.name)
        assertEquals("Nova descrição", state.bolao?.description)
        assertEquals(5, state.bolao?.pointsExactScore)
        assertEquals(2, state.bolao?.pointsWinnerOrDraw)
        assertTrue(state.showSuccessMessage)
        assertFalse(state.isLoading)
    }

    @Test
    fun `delete marca o bolao como excluido`() = runTest {
        viewModel.delete()

        val state = viewModel.uiState.value
        assertTrue(state.isDeleted)
        assertFalse(state.isLoading)
    }

    @Test
    fun `removeParticipant tira o usuario da lista de participantes`() = runTest {
        bolaoRepository.requestJoinBolao("LIB026", "membro-removido")
        bolaoRepository.approveJoinRequest("bolao-1", "membro-removido", approve = true)

        viewModel.removeParticipant("membro-removido")

        val bolao = bolaoRepository.getBolao("bolao-1")
        assertFalse("membro-removido" in bolao.participants)
    }
}
