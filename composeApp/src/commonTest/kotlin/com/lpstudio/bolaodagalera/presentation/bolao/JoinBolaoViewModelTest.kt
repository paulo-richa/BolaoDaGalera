package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.data.fake.FAKE_FRIEND
import com.lpstudio.bolaodagalera.data.fake.FAKE_USER
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class JoinBolaoViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: JoinBolaoViewModel

    // O bolão fake "bolao-1" tem código "LIB026" e já inclui FAKE_USER (pauloricha) como participante.
    private val existingBolaoCode = "LIB026"

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        bolaoRepository = FakeBolaoRepository()
        authRepository = FakeAuthRepository()
        viewModel = JoinBolaoViewModel(bolaoRepository, authRepository, FakeCrashReporter(), FakePerformanceMonitor())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `join quando ja e participante sinaliza alreadyMemberBolaoId`() = runTest {
        authRepository.setUser(FAKE_USER) // já está em bolao-1

        viewModel.join(existingBolaoCode)

        val state = viewModel.uiState.value
        assertEquals("bolao-1", state.alreadyMemberBolaoId)
        assertNull(state.error)
        assertTrue(!state.requestSent)
    }

    @Test
    fun `join com codigo valido para usuario novo envia pedido`() = runTest {
        authRepository.setUser(FAKE_FRIEND) // não está em bolao-1

        viewModel.join(existingBolaoCode)

        val state = viewModel.uiState.value
        assertTrue(state.requestSent)
        assertEquals("bolao-1", state.joinedBolao?.id)
        assertNull(state.alreadyMemberBolaoId)
        assertNull(state.error)
    }

    @Test
    fun `join com codigo invalido mostra mensagem de erro`() = runTest {
        authRepository.setUser(FAKE_USER)

        viewModel.join("ZZZZZZ")

        val state = viewModel.uiState.value
        assertNull(state.joinedBolao)
        assertNull(state.alreadyMemberBolaoId)
        assertTrue(state.error?.contains("não encontrado") == true)
    }

    @Test
    fun `join normaliza codigo para maiusculas e sem espacos`() = runTest {
        authRepository.setUser(FAKE_FRIEND)

        viewModel.join("  lib026  ")

        assertEquals("bolao-1", viewModel.uiState.value.joinedBolao?.id)
    }

    @Test
    fun `join sem usuario logado nao faz nada`() = runTest {
        authRepository.setUser(null)

        viewModel.join(existingBolaoCode)

        val state = viewModel.uiState.value
        assertNull(state.joinedBolao)
        assertNull(state.alreadyMemberBolaoId)
        assertNull(state.error)
    }
}
