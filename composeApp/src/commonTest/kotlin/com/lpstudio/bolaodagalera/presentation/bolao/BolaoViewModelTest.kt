package com.lpstudio.bolaodagalera.presentation.bolao

import com.lpstudio.bolaodagalera.domain.model.*
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class BolaoViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    
    private lateinit var bolaoRepository: BolaoRepository
    private lateinit var matchRepository: MatchRepository
    private lateinit var predictionRepository: PredictionRepository
    private lateinit var viewModel: BolaoViewModel

    private val fakeMatches = listOf(
        Match(
            id = "m1", 
            homeTeam = "Time A", 
            awayTeam = "Time B",
            homeTeamCode = "TMA",
            awayTeamCode = "TMB",
            homeTeamFlag = "🇧🇷",
            awayTeamFlag = "🇦🇷",
            matchDateMillis = 0L,
            phase = Phase.GROUP_STAGE
        )
    )
    private val fakeBolao = Bolao(id = "b1", name = "Teste")

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        bolaoRepository = object : BolaoRepository {
            override fun getUserBoloes(userId: String) = flowOf(emptyList<Bolao>())
            override suspend fun getBolao(bolaoId: String) = fakeBolao
            override suspend fun createBolao(name: String, description: String, ownerId: String) = fakeBolao
            override suspend fun joinBolao(code: String, userId: String) = fakeBolao
            override suspend fun leaveBolao(bolaoId: String, userId: String) {}
            override suspend fun updateBolao(bolaoId: String, name: String, description: String, pointsExactScore: Int, pointsWinnerOrDraw: Int) {}
            override suspend fun deleteBolao(bolaoId: String) {}
            override suspend fun removeParticipant(bolaoId: String, userId: String) {}
        }

        matchRepository = object : MatchRepository {
            private val flow = MutableStateFlow(fakeMatches)
            override fun getMatches() = flow
            override fun getMatchesByPhase(phase: Phase) = flowOf(fakeMatches)
            override suspend fun getMatch(matchId: String) = fakeMatches.first()
            override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {}
            override suspend fun seedMatchesIfNeeded() {}
        }

        predictionRepository = object : PredictionRepository {
            override fun getUserPredictions(userId: String, bolaoId: String) = flowOf(emptyList<Prediction>())
            override fun getBolaoAllPredictions(bolaoId: String) = flowOf(emptyList<Prediction>())
            override suspend fun getUserPredictionForMatch(userId: String, bolaoId: String, matchId: String) = null
            override suspend fun savePrediction(prediction: Prediction) {}
            override fun getRanking(bolaoId: String, participantIds: List<String>): Flow<List<RankingEntry>> = flowOf(emptyList())
        }

        viewModel = BolaoViewModel(bolaoRepository, matchRepository, predictionRepository, "b1")
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial load should populate matches and stop loading`() = runTest {
        val state = viewModel.uiState.value
        assertEquals(fakeMatches, state.matches)
        assertEquals(fakeBolao, state.bolao)
        assertFalse(state.isLoading)
    }

    @Test
    fun `setting user id should refresh predictions but maintain matches`() = runTest {
        viewModel.setUserId("user123")
        val state = viewModel.uiState.value
        assertEquals(fakeMatches, state.matches)
        assertFalse(state.isLoading)
    }
}
