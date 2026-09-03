package com.lpstudio.bolaodagalera.presentation.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.observability.AnalyticsEvents
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.observability.PerformanceTraces
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class PredictionLoadResult(val bolao: Bolao, val match: Match, val allMatches: List<Match>, val prediction: Prediction?)

data class PredictionUiState(
    val match: Match? = null,
    val allMatches: List<Match> = emptyList(),
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
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker,
    private val interstitialAdCounter: InterstitialAdCounter,
    private val bolaoId: String,
    private val matchId: String
) : ViewModel() {
    private val errorReporter = ErrorReporter(crashReporter)
    private val _uiState = MutableStateFlow(PredictionUiState())
    val uiState: StateFlow<PredictionUiState> = _uiState.asStateFlow()

    fun load(userId: String) {
        viewModelScope.launch {
            try {
                val loaded =
                    performanceMonitor.trace(PerformanceTraces.PREDICTION_LOAD) {
                        val bolao = bolaoRepository.getBolao(bolaoId)
                        val championshipId = bolao.championshipId

                        val match = matchRepository.getMatch(championshipId, matchId)
                        val allMatches =
                            try {
                                matchRepository.getMatches(championshipId).first()
                            } catch (e: CancellationException) {
                                throw e
                            } catch (e: Exception) {
                                errorReporter.report(e, "Erro ao carregar allMatches para comparação de data")
                                emptyList()
                            }
                        val prediction = predictionRepository.getUserPredictionForMatch(userId, bolaoId, matchId)
                        PredictionLoadResult(bolao, match, allMatches, prediction)
                    }

                _uiState.update {
                    it.copy(
                        match = loaded.match,
                        allMatches = loaded.allMatches,
                        bolao = loaded.bolao,
                        existingPrediction = loaded.prediction,
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao carregar palpite $matchId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun savePrediction(userId: String, homeScore: Int, awayScore: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val prediction =
                    Prediction(
                        userId = userId,
                        bolaoId = bolaoId,
                        matchId = matchId,
                        homeScore = homeScore,
                        awayScore = awayScore
                    )
                performanceMonitor.trace(PerformanceTraces.PREDICTION_SAVE) { predictionRepository.savePrediction(prediction) }
                analyticsTracker.logEvent(
                    AnalyticsEvents.PREDICTION_SAVE,
                    mapOf("bolao_id" to bolaoId, "match_id" to matchId)
                )
                interstitialAdCounter.incrementAndShowIfNecessary()
                _uiState.update { it.copy(isSaved = true, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao salvar palpite $matchId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }
}
