package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BolaoUiState(
    val bolao: Bolao? = null,
    val matches: List<Match> = emptyList(),
    val userPredictions: Map<String, Prediction> = emptyMap(), // matchId -> prediction
    val participants: List<RankingEntry> = emptyList(),
    val allPredictions: List<Prediction> = emptyList(),
    val isLoading: Boolean = true,
    val isLeaveSuccess: Boolean = false,
    val error: String? = null
)

class BolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val matchRepository: MatchRepository,
    private val predictionRepository: PredictionRepository,
    private val authRepository: AuthRepository,
    private val bolaoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(BolaoUiState())
    val uiState: StateFlow<BolaoUiState> = _uiState.asStateFlow()

    private val _userId = MutableStateFlow(authRepository.currentUser?.id ?: "")

    init {
        loadBolao()
        observeMatchesPredictionsAndRanking()
    }

    private fun loadBolao() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolao = bolao) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private fun observeMatchesPredictionsAndRanking() {
        // Observa o Bolão em tempo real para atualizar participantes e dados gerais
        val bolaoFlow = bolaoRepository.getBolaoFlow(bolaoId)
            .onEach { bolao -> _uiState.update { it.copy(bolao = bolao) } }

        val participantsFlow = bolaoFlow
            .map { it.participants }
            .distinctUntilChanged()
        
        _userId.filter { it.isNotBlank() }
            .flatMapLatest { currentUserId ->
                combine(
                    matchRepository.getMatches(),
                    predictionRepository.getUserPredictions(currentUserId, bolaoId),
                    predictionRepository.getBolaoAllPredictions(bolaoId),
                    participantsFlow.flatMapLatest { participants ->
                        predictionRepository.getRanking(bolaoId, participants)
                    }
                ) { matches, predictions, allPredictions, ranking ->
                    val predictionMap = predictions.associateBy { it.matchId }
                    _uiState.update { it.copy(
                        matches = matches, 
                        userPredictions = predictionMap,
                        allPredictions = allPredictions,
                        participants = ranking,
                        isLoading = false
                    ) }
                }
            }.launchIn(viewModelScope)
    }

    fun setUserId(id: String) {
        if (id.isNotBlank()) {
            _userId.value = id
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }

    fun updateMatchScore(matchId: String, home: Int, away: Int) {
        viewModelScope.launch {
            matchRepository.updateMatchScore(matchId, home, away)
        }
    }

    fun approveParticipant(userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
                loadBolao() // Atualiza os dados locais
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun leaveBolao() {
        val currentUserId = _userId.value
        if (currentUserId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                bolaoRepository.leaveBolao(bolaoId, currentUserId)
                _uiState.update { it.copy(isLeaveSuccess = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
