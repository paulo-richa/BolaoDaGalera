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
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateBolaoUiState())
    val uiState: StateFlow<CreateBolaoUiState> = _uiState.asStateFlow()

    init {
        loadMatchesData()
    }

    private fun loadMatchesData() {
        viewModelScope.launch {
            matchRepository.getAllMatches().collect { matches ->
                _uiState.update { it.copy(allMatches = matches) }
            }
        }
    }

    fun isPhaseAvailable(championshipId: String, phase: Phase): Boolean {
        val matches = _uiState.value.allMatches.filter { it.championshipId == championshipId && it.phase == phase }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        // The phase is available only if none of its matches have kicked off yet
        return matches.all { it.matchDateMillis > now }
    }

    fun isKnockoutAvailable(championshipId: String): Boolean {
        val matches =
            _uiState.value.allMatches.filter {
                it.championshipId == championshipId &&
                    it.phase != Phase.GROUP_STAGE &&
                    it.phase != Phase.FRIENDLIES
            }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        // The knockout stage is available only if none of its matches have kicked off yet
        return matches.all { it.matchDateMillis > now }
    }

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
                    performanceMonitor.trace("create_bolao") {
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
                analyticsTracker.logEvent("bolao_created", mapOf("championship_id" to championshipId, "scope" to scope.name))
                _uiState.update { it.copy(createdBolao = bolao, isLoading = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao criar bolão")
                _uiState.update { it.copy(error = e.message ?: "Erro ao criar bolão", isLoading = false) }
            }
        }
    }
}
