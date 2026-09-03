package com.lpstudio.bolaodagalera.presentation.help

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class HelpUiState(val isSending: Boolean = false, val showSuccess: Boolean = false, val showError: Boolean = false)

class HelpViewModel(private val supportRepository: SupportRepository, private val authRepository: AuthRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HelpUiState())
    val uiState: StateFlow<HelpUiState> = _uiState.asStateFlow()

    fun sendSupportMessage(message: String) {
        if (message.isBlank() || _uiState.value.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, showError = false) }
            try {
                val user = authRepository.currentUser
                supportRepository.sendSupportTicket(
                    userId = user?.id ?: "anonymous",
                    userEmail = user?.email ?: "no-email",
                    message = message
                )
                _uiState.update { it.copy(isSending = false, showSuccess = true) }
                delay(SUCCESS_MESSAGE_DURATION_MILLIS)
                _uiState.update { it.copy(showSuccess = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isSending = false, showError = true) }
            }
        }
    }

    fun dismissError() = _uiState.update { it.copy(showError = false) }

    private companion object {
        private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3000L
    }
}
