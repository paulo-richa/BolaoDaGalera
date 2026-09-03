package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

enum class ParticipantInputType { EMAIL, PHONE, USER }

enum class AddParticipantsError { USER_NOT_FOUND, SEND_FAILED }

@Immutable
data class AddParticipantsUiState(
    val bolaoName: String = "",
    val bolaoCode: String = "",
    val isLoading: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val error: AddParticipantsError? = null
)

class AddParticipantsViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val invitationRepository: InvitationRepository,
    private val crashReporter: CrashReporter,
    private val bolaoId: String,
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val _uiState = MutableStateFlow(AddParticipantsUiState())
    val uiState: StateFlow<AddParticipantsUiState> = _uiState.asStateFlow()

    init {
        loadBolaoInfo()
    }

    private fun loadBolaoInfo() {
        viewModelScope.launch {
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolaoName = bolao.name, bolaoCode = bolao.code) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorReporter.report(e, "Falha ao carregar dados do bolão $bolaoId para convite")
            }
        }
    }

    /** Infers the input type from its shape, to route validation and keyboard type in the UI. */
    fun detectInputType(identifier: String): ParticipantInputType {
        val trimmed = identifier.trim()
        return when {
            trimmed.contains("@") && trimmed.contains(".") -> ParticipantInputType.EMAIL
            trimmed.filter { it.isDigit() }.length >= MIN_PHONE_DIGITS -> ParticipantInputType.PHONE
            else -> ParticipantInputType.USER
        }
    }

    fun sendInvite(identifier: String) {
        val inputType = detectInputType(identifier)
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val trimmedId = identifier.trim()
                val inviterName = authRepository.currentUser?.name ?: "Alguém"

                // 1. Check the user exists in the database before inviting
                val userExists =
                    when (inputType) {
                        ParticipantInputType.EMAIL -> authRepository.isEmailInUse(trimmedId.lowercase())
                        ParticipantInputType.PHONE -> authRepository.isPhoneInUse(trimmedId.filter { it.isDigit() })
                        ParticipantInputType.USER -> authRepository.isUsernameInUse(trimmedId.lowercase())
                    }

                if (!userExists) {
                    _uiState.update { it.copy(isLoading = false, error = AddParticipantsError.USER_NOT_FOUND) }
                    return@launch
                }

                // 2. Send the in-app invitation
                val inviteeIdentifier =
                    when (inputType) {
                        ParticipantInputType.EMAIL -> trimmedId.lowercase()
                        ParticipantInputType.PHONE -> trimmedId.filter { it.isDigit() }
                        ParticipantInputType.USER -> trimmedId.lowercase()
                    }

                try {
                    withTimeout(INVITATION_SEND_TIMEOUT_MILLIS) {
                        invitationRepository.sendInvitation(
                            bolaoId = bolaoId,
                            bolaoName = _uiState.value.bolaoName,
                            inviterName = inviterName,
                            inviteeIdentifier = inviteeIdentifier
                        )
                    }
                } catch (e: TimeoutCancellationException) {
                    // Best-effort: shown as success below regardless, since a Cloud Function
                    // retry path exists - but still tracked in Crashlytics so a systematic
                    // failure here (not just a slow one-off) is visible.
                    errorReporter.report(e, "Envio de convite in-app expirou (queued for later delivery)")
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    errorReporter.report(e, "Falha ao enviar convite in-app (queued for later delivery)")
                }

                _uiState.update { it.copy(isLoading = false, showSuccessMessage = true) }
                delay(SUCCESS_MESSAGE_DURATION_MILLIS)
                _uiState.update { it.copy(showSuccessMessage = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorReporter.report(e, "Falha ao enviar convite")
                _uiState.update { it.copy(isLoading = false, error = AddParticipantsError.SEND_FAILED) }
            }
        }
    }

    private companion object {
        private const val MIN_PHONE_DIGITS = 8
        private const val INVITATION_SEND_TIMEOUT_MILLIS = 3000L
        private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3000L
    }
}
