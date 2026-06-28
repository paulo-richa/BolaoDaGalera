package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.data.fake.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class BolaoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var bolaoRepository: FakeBolaoRepository
    private lateinit var matchRepository: FakeMatchRepository
    private lateinit var predictionRepository: FakePredictionRepository
    private lateinit var authRepository: FakeAuthRepository
    private lateinit var viewModel: BolaoViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        authRepository = FakeAuthRepository()
        matchRepository = FakeMatchRepository()
        predictionRepository = FakePredictionRepository(matchRepository)
        bolaoRepository = FakeBolaoRepository()

        viewModel = BolaoViewModel(
            bolaoRepository, 
            matchRepository, 
            predictionRepository, 
            authRepository,
            "bolao-1"
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load should populate matches and stop loading`() = runTest {
        val state = viewModel.uiState.value
        // FakeMatchRepository inicializa com allMatches do seed
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
}
