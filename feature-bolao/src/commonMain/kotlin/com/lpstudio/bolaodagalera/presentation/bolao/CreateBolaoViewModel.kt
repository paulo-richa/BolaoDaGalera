package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.usecase.CheckKnockoutAvailabilityUseCase
import com.lpstudio.bolaodagalera.domain.usecase.CheckPhaseAvailabilityUseCase
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
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class CreateBolaoUiState(
    val isLoading: Boolean = false,
    val createdBolao: Bolao? = null,
    val error: String? = null,
    val allMatches: List<Match> = emptyList()
)

class CreateBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker,
    private val checkPhaseAvailability: CheckPhaseAvailabilityUseCase = CheckPhaseAvailabilityUseCase(),
    private val checkKnockoutAvailability: CheckKnockoutAvailabilityUseCase = CheckKnockoutAvailabilityUseCase(),
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateBolaoUiState())
    val uiState: StateFlow<CreateBolaoUiState> = _uiState.asStateFlow()

    init {
        loadMatchesData()
    }

    private fun loadMatchesData() {
        viewModelScope.launch {
            matchRepository.getAllMatches()
                .catch { e ->
                    if (e is CancellationException) throw e
                    errorReporter.report(e, "Erro ao carregar partidas para novo bolão")
                }
                .collect { matches ->
                    _uiState.update { it.copy(allMatches = matches) }
                }
        }
    }

    fun isPhaseAvailable(championshipId: String, phase: Phase): Boolean =
        checkPhaseAvailability(_uiState.value.allMatches, championshipId, phase)

    fun isKnockoutAvailable(championshipId: String): Boolean = checkKnockoutAvailability(_uiState.value.allMatches, championshipId)

    fun create(
        name: String,
        description: String,
        championshipId: String,
        scope: BolaoScope,
        specificMatchId: String?,
        pointsExact: Int,
        pointsWinner: Int
    ) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bolao =
                    performanceMonitor.trace(PerformanceTraces.BOLAO_CREATE) {
                        bolaoRepository.createBolao(
                            name.trim(),
                            description.trim(),
                            userId,
                            championshipId,
                            scope = scope,
                            specificMatchId = specificMatchId,
                            pointsExactScore = pointsExact,
                            pointsWinnerOrDraw = pointsWinner
                        )
                    }
                analyticsTracker.logEvent(
                    AnalyticsEvents.BOLAO_CREATE,
                    mapOf("bolao_id" to bolao.id, "championship_id" to championshipId, "scope" to scope.name)
                )
                _uiState.update { it.copy(createdBolao = bolao, isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao criar bolão")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }
}
