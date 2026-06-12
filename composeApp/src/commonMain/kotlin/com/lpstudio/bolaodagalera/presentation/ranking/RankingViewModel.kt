package com.lpstudio.bolaodagalera.presentation.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class RankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val currentUserId: String = "",
    val isLoading: Boolean = true,
    val error: String? = null
)

class RankingViewModel(
    private val predictionRepository: PredictionRepository,
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val bolaoId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(RankingUiState())
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    init {
        val userId = authRepository.currentUser?.id ?: ""
        _uiState.update { it.copy(currentUserId = userId) }
        loadRanking()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun loadRanking() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                predictionRepository.getRanking(bolaoId, bolao.participants)
                    .onEach { entries ->
                        _uiState.update { it.copy(entries = entries, isLoading = false) }
                    }
                    .catch { e ->
                        _uiState.update { it.copy(error = e.message, isLoading = false) }
                    }
                    .launchIn(viewModelScope)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }
}
