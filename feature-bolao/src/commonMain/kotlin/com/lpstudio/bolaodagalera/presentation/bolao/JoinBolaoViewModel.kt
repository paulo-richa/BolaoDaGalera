package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.ErrorCategory
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class JoinBolaoUiState(
    val isLoading: Boolean = false,
    val joinedBolao: Bolao? = null,
    val requestSent: Boolean = false,
    val alreadyMemberBolaoId: String? = null,
    val error: String? = null
)

class JoinBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker,
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinBolaoUiState())
    val uiState: StateFlow<JoinBolaoUiState> = _uiState.asStateFlow()

    fun join(code: String) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, requestSent = false) }
            try {
                // Uses requestJoinBolao so the owner must approve the request
                val bolao =
                    performanceMonitor.trace(PerformanceTraces.BOLAO_JOIN_REQUEST) {
                        bolaoRepository.requestJoinBolao(code.trim().uppercase(), userId)
                    }

                if (userId in bolao.participants) {
                    // Rule 4: already a member, signal to navigate directly
                    _uiState.update { it.copy(alreadyMemberBolaoId = bolao.id, isLoading = false) }
                } else {
                    // Rule 3: new join request sent
                    analyticsTracker.logEvent(AnalyticsEvents.BOLAO_JOIN_REQUEST, mapOf("bolao_id" to bolao.id))
                    _uiState.update { it.copy(joinedBolao = bolao, requestSent = true, isLoading = false) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message =
                    errorReporter.reportAndClassify(e, "Erro ao entrar no bolão") { category ->
                        if (category == ErrorCategory.NOT_FOUND) "Código inválido." else null
                    }
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }
}
