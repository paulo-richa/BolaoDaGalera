package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.ErrorCategory
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.usecase.ClassifyExceptionUseCase
import com.lpstudio.bolaodagalera.domain.usecase.GenerateAvailableUsernameUseCase
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.observability.appLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Immutable
data class AuthUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
    val isAuthChecked: Boolean = false,
    val emailExists: Boolean? = null,
    val checkedEmail: String = ""
)

class AuthViewModel(
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker,
    private val generateAvailableUsernameUseCase: GenerateAvailableUsernameUseCase = GenerateAvailableUsernameUseCase(authRepository),
    private val classifyException: ClassifyExceptionUseCase = ClassifyExceptionUseCase()
) : ViewModel() {
    private val logger = appLogger("AuthViewModel")
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        authRepository.authStateFlow.onEach { user ->
            _uiState.update { it.copy(user = user, isAuthChecked = true) }
        }.launchIn(viewModelScope)
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                val user = performanceMonitor.trace("auth_login") { authRepository.signIn(email.trim(), password) }
                analyticsTracker.logEvent("login")
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao fazer login")
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e)) }
            }
        }
    }

    fun checkEmail(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Digite seu e-mail para continuar.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val exists = authRepository.isEmailInUse(email.trim())
                _uiState.update { it.copy(isLoading = false, emailExists = exists, checkedEmail = email.trim()) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao checar e-mail")
                // Verification failure (e.g. network issue or enumeration protection) is
                // inconclusive, so reset the state and surface a generic user-facing error.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        emailExists = null,
                        error = "Não foi possível verificar seu e-mail agora. Tente prosseguir normalmente ou verifique sua conexão."
                    )
                }
            }
        }
    }

    fun resetEmailCheck() {
        _uiState.update { it.copy(emailExists = null, checkedEmail = "") }
    }

    fun register(email: String, password: String, name: String, phone: String, nickname: String, username: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (authRepository.isEmailInUse(email.trim())) {
                    _uiState.update { it.copy(isLoading = false, error = "Este e-mail já está em uso.") }
                    return@launch
                }
                if (username.isNotBlank() && authRepository.isUsernameInUse(username.trim().lowercase())) {
                    _uiState.update { it.copy(isLoading = false, error = "Este ID já está em uso.") }
                    return@launch
                }
                if (phone.isNotBlank() && authRepository.isPhoneInUse(phone.trim())) {
                    _uiState.update { it.copy(isLoading = false, error = "Este telefone já está em uso.") }
                    return@launch
                }
                if (nickname.isNotBlank() && authRepository.isNicknameInUse(nickname.trim())) {
                    _uiState.update { it.copy(isLoading = false, error = "Este apelido já está em uso.") }
                    return@launch
                }

                val user =
                    performanceMonitor.trace("auth_register") {
                        authRepository.register(
                            email.trim(),
                            password,
                            name.trim(),
                            phone.trim(),
                            nickname.trim(),
                            username.trim().lowercase()
                        )
                    }
                analyticsTracker.logEvent("sign_up")
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao registrar usuário")
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e)) }
            }
        }
    }

    fun updateProfile(name: String, phone: String, nickname: String) {
        viewModelScope.launch {
            val currentUser = _uiState.value.user
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val newPhone = phone.trim()
                val newNickname = nickname.trim()
                val newName = name.trim()

                // Quick duplicate checks
                if (newPhone.isNotBlank() && newPhone != currentUser?.phone) {
                    try {
                        if (authRepository.isPhoneInUse(newPhone)) {
                            _uiState.update { it.copy(isLoading = false, error = "Este telefone já está em uso.") }
                            return@launch
                        }
                    } catch (e: Exception) {
                        // Ignore timeout on the check and proceed
                    }
                }

                if (newNickname.isNotBlank() && newNickname != currentUser?.nickname) {
                    try {
                        if (authRepository.isNicknameInUse(newNickname)) {
                            _uiState.update { it.copy(isLoading = false, error = "Este apelido já está em uso.") }
                            return@launch
                        }
                    } catch (e: Exception) {
                        // Ignore timeout on the check and proceed
                    }
                }

                authRepository.updateProfile(newName, newPhone, newNickname)
                _uiState.update { it.copy(isLoading = false, successMessage = "Perfil atualizado com sucesso!") }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao atualizar perfil")
                _uiState.update { it.copy(isLoading = false, error = friendlyError(e)) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun resetPassword(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(error = "Digite seu e-mail para recuperar a senha.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, successMessage = null) }
            try {
                authRepository.sendPasswordResetEmail(email.trim())
                _uiState.update { it.copy(isLoading = false, successMessage = "E-mail de recuperação enviado com sucesso!") }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao enviar e-mail de recuperação")
                _uiState.update { it.copy(isLoading = false, error = "Erro ao enviar e-mail. Verifique se o e-mail está correto.") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null, successMessage = null) }

    suspend fun generateAvailableUsername(fullName: String): String = generateAvailableUsernameUseCase(fullName)

    private fun friendlyError(e: Exception): String {
        logger.e(e) { "Auth Error Debug" } // Internal debug log
        return when (classifyException(e)) {
            ErrorCategory.INVALID_CREDENTIALS -> "E-mail ou senha incorretos. Verifique os dados e tente novamente."
            ErrorCategory.USER_NOT_FOUND -> "Usuário não encontrado. Crie uma conta para acessar."
            ErrorCategory.ALREADY_IN_USE -> "Este e-mail já está sendo usado em outra conta."
            ErrorCategory.NETWORK -> "Erro de conexão. Verifique sua internet e tente novamente."
            ErrorCategory.RATE_LIMITED -> "Muitas tentativas falhas. Sua conta foi temporariamente bloqueada por segurança."
            ErrorCategory.WEAK_PASSWORD -> "A senha é muito fraca. Use pelo menos 6 caracteres."
            ErrorCategory.INVALID_EMAIL -> "O formato do e-mail é inválido."
            ErrorCategory.PERMISSION -> "Aguardando autenticação para acessar dados. Tente novamente em instantes."
            ErrorCategory.UNKNOWN -> "Ocorreu um erro inesperado. Por favor, tente novamente."
        }
    }
}
