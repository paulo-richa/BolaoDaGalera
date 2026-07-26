package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
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

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

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
                val user = authRepository.signIn(email.trim(), password)
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
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
                // Se der erro de verificação (ex: rede ou proteção de enumeração), 
                // não podemos afirmar nada. Resetamos o estado e mostramos um erro amigável.
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

                val user = authRepository.register(email.trim(), password, name.trim(), phone.trim(), nickname.trim(), username.trim().lowercase())
                _uiState.update { it.copy(user = user, isLoading = false) }
            } catch (e: Exception) {
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

                // Verificações de duplicidade rápidas
                if (newPhone.isNotBlank() && newPhone != currentUser?.phone) {
                    try {
                        if (authRepository.isPhoneInUse(newPhone)) {
                            _uiState.update { it.copy(isLoading = false, error = "Este telefone já está em uso.") }
                            return@launch
                        }
                    } catch (e: Exception) { /* Segue se der timeout na checagem */ }
                }
                
                if (newNickname.isNotBlank() && newNickname != currentUser?.nickname) {
                    try {
                        if (authRepository.isNicknameInUse(newNickname)) {
                            _uiState.update { it.copy(isLoading = false, error = "Este apelido já está em uso.") }
                            return@launch
                        }
                    } catch (e: Exception) { /* Segue se der timeout na checagem */ }
                }

                authRepository.updateProfile(newName, newPhone, newNickname)
                _uiState.update { it.copy(isLoading = false, successMessage = "Perfil atualizado com sucesso!") }
            } catch (e: Exception) {
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
                _uiState.update { it.copy(isLoading = false, error = "Erro ao enviar e-mail. Verifique se o e-mail está correto.") }
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null, successMessage = null) }

    suspend fun generateAvailableUsername(fullName: String): String {
        val parts = fullName.trim().lowercase()
            .replace(Regex("[^a-z\\s]"), "") // Remove acentos e símbolos simplificadamente
            .split(" ")
            .filter { it.length >= 2 }
        
        if (parts.isEmpty()) return ""

        val firstName = parts[0]
        val secondName = parts.getOrNull(1) ?: ""
        val lastPart = parts.last()

        val candidates = mutableListOf<String>()
        if (secondName.isNotEmpty()) {
            candidates.add(firstName + secondName)
            candidates.add(firstName + "." + secondName)
            candidates.add(firstName.take(1) + secondName)
        }
        candidates.add(firstName)
        if (parts.size >= 3) {
            candidates.add(firstName.take(1) + secondName.take(1) + lastPart)
        }

        // Tenta os candidatos pré-definidos
        for (candidate in candidates) {
            try {
                if (!authRepository.isUsernameInUse(candidate)) return candidate
            } catch (e: Exception) {
                // Se der erro de permissão por estar deslogado, retorna o primeiro como sugestão
                return candidates.firstOrNull() ?: firstName
            }
        }

        // Fallback com números aleatórios caso nenhum esteja livre
        return try {
            var finalCandidate: String
            var attempts = 0
            do {
                finalCandidate = firstName + kotlin.random.Random.nextInt(100, 999).toString()
                attempts++
            } while (attempts < 5 && authRepository.isUsernameInUse(finalCandidate))
            finalCandidate
        } catch (e: Exception) {
            firstName + kotlin.random.Random.nextInt(100, 999).toString()
        }
    }

    private fun friendlyError(e: Exception): String {
        val msg = e.message?.lowercase() ?: ""
        println("Auth Error Debug: $msg") // Log para debug interno
        return when {
            msg.contains("incorrect") || msg.contains("invalid-credential") || msg.contains("password") || msg.contains("wrong") ->
                "E-mail ou senha incorretos. Verifique os dados e tente novamente."
            msg.contains("user-not-found") || msg.contains("no user") -> 
                "Usuário não encontrado. Crie uma conta para acessar."
            msg.contains("email-already") || msg.contains("email já") || msg.contains("collision") || msg.contains("already-in-use") -> 
                "Este e-mail já está sendo usado em outra conta."
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") -> 
                "Erro de conexão. Verifique sua internet e tente novamente."
            msg.contains("too many requests") || msg.contains("blocked") ->
                "Muitas tentativas falhas. Sua conta foi temporariamente bloqueada por segurança."
            msg.contains("weak-password") ->
                "A senha é muito fraca. Use pelo menos 6 caracteres."
            msg.contains("invalid-email") ->
                "O formato do e-mail é inválido."
            msg.contains("permission") || msg.contains("permissão") ->
                "Aguardando autenticação para acessar dados. Tente novamente em instantes."
            else -> "Ocorreu um erro inesperado. Por favor, tente novamente."
        }
    }
}
