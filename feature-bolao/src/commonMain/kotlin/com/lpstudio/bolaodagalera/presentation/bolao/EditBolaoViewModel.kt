package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.ErrorReporter
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

@Immutable
data class EditBolaoUiState(
    val bolao: Bolao? = null,
    val participants: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val isKnockoutStarted: Boolean = false,
    val error: String? = null
)

class EditBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
    private val bolaoId: String,
    private val crashReporter: CrashReporter,
    private val errorReporter: ErrorReporter = ErrorReporter(crashReporter)
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditBolaoUiState())
    val uiState: StateFlow<EditBolaoUiState> = _uiState.asStateFlow()

    val currentUserId = authRepository.currentUser?.id

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                val participants = authRepository.getUsers(bolao.participants)
                _uiState.update {
                    it.copy(
                        bolao = bolao,
                        participants = participants,
                        isLoading = false
                    )
                }

                // Check knockout status for this specific championship
                matchRepository.getMatches(bolao.championshipId).collect { matches ->
                    val now = TimeSource.nowMillis()
                    val knockoutStarted =
                        matches.any {
                            it.phase != Phase.GROUP_STAGE &&
                                it.phase != Phase.FRIENDLIES &&
                                (it.isFinished || now >= it.matchDateMillis)
                        }
                    _uiState.update { it.copy(isKnockoutStarted = knockoutStarted) }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao carregar dados do bolão $bolaoId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun update(name: String, description: String, scope: BolaoScope, pointsExact: Int, pointsWinner: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                bolaoRepository.updateBolao(bolaoId, name, description, scope, pointsExact, pointsWinner)
                val updatedBolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolao = updatedBolao, isLoading = false, showSuccessMessage = true) }
                delay(SUCCESS_MESSAGE_DURATION_MILLIS)
                _uiState.update { it.copy(showSuccessMessage = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao atualizar bolão $bolaoId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withTimeout(DELETE_BOLAO_TIMEOUT_MILLIS) {
                    bolaoRepository.deleteBolao(bolaoId)
                }
                _uiState.update { it.copy(isDeleted = true, isLoading = false) }
            } catch (e: TimeoutCancellationException) {
                errorReporter.report(e, "Timeout ao excluir bolão $bolaoId")
                _uiState.update { it.copy(error = "A exclusão demorou demais. Tente novamente.", isLoading = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao excluir bolão $bolaoId")
                _uiState.update { it.copy(error = message, isLoading = false) }
            }
        }
    }

    fun removeParticipant(userId: String) {
        viewModelScope.launch {
            try {
                bolaoRepository.removeParticipant(bolaoId, userId)
                // Refresh data
                val bolao = bolaoRepository.getBolao(bolaoId)
                val participants = authRepository.getUsers(bolao.participants)
                _uiState.update { it.copy(bolao = bolao, participants = participants) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                val message = errorReporter.reportAndClassify(e, "Erro ao remover participante $userId do bolão $bolaoId")
                _uiState.update { it.copy(error = message) }
            }
        }
    }

    private companion object {
        private const val SUCCESS_MESSAGE_DURATION_MILLIS = 3000L
        private const val DELETE_BOLAO_TIMEOUT_MILLIS = 10000L
    }
}
