package com.lpstudio.bolaodagalera.presentation.help

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import com.lpstudio.bolaodagalera.observability.AnalyticsEvents
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.observability.PerformanceTraces
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class HelpUiState(val isSending: Boolean = false, val showSuccess: Boolean = false, val showError: Boolean = false)

class HelpViewModel(
    private val supportRepository: SupportRepository,
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker,
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    fun sendSupportMessage(message: String) {
        if (message.isBlank() || _uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, showError = false) }
            try {
                val user = authRepository.currentUser
                performanceMonitor.trace(PerformanceTraces.SUPPORT_SEND) {
                    supportRepository.sendSupportTicket(
                        userId = user?.id ?: "anonymous",
                        userEmail = user?.email ?: "no-email",
                        message = message
                    )
                }
                analyticsTracker.logEvent(AnalyticsEvents.SUPPORT_SEND)
                _uiState.update { it.copy(isSending = false, showSuccess = true) }
                delay(SUCCESS_MESSAGE_DURATION_MILLIS)
                _uiState.update { it.copy(showSuccess = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorReporter.report(e, "Falha ao enviar mensagem de suporte")
                _uiState.update { it.copy(isSending = false, showError = true) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(showError = false) }

    private companion object {
        private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3000L
    }
}
