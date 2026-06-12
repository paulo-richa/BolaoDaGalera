package com.lpstudio.bolaodagalera.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PredictionUiState(
    val match: Match? = null,
    val bolao: Bolao? = null,
    val existingPrediction: Prediction? = null,
    val isLoading: Boolean = true,
    val isSaved: Boolean = false,
    val error: String? = null
)

class PredictionViewModel(
    private val matchRepository: MatchRepository,
    private val predictionRepository: PredictionRepository,
    private val bolaoRepository: BolaoRepository,
    private val bolaoId: String,
    private val matchId: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            try {
                val match = matchRepository.getMatch(matchId)
                val bolao = bolaoRepository.getBolao(bolaoId)
                val prediction = predictionRepository.getUserPredictionForMatch(userId, bolaoId, matchId)
                _uiState.update { 
                    it.copy(
                        match = match, 
                        bolao = bolao,
                        existingPrediction = prediction, 
                        isLoading = false
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun savePrediction(userId: String, homeScore: Int, awayScore: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val prediction = Prediction(
                    userId = userId,
                    bolaoId = bolaoId,
                    matchId = matchId,
                    homeScore = homeScore,
                    awayScore = awayScore
                )
                predictionRepository.savePrediction(prediction)
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erro ao salvar palpite", isLoading = false) }
            }
        }
    }
}
