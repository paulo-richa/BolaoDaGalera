package com.lpstudio.bolaodagalera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val user: User? = null,
    val boloes: List<Bolao> = emptyList(),
    val invitations: List<Invitation> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val hasUnreadNotifications: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class HomeViewModel(
    private val authRepository: AuthRepository,
    private val bolaoRepository: BolaoRepository,
    private val invitationRepository: InvitationRepository,
    private val notificationRepository: NotificationRepository,
    private val crashReporter: CrashReporter
) : ViewModel() {
    private val logger = appLogger("HomeViewModel")
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var dataCollectionJob: kotlinx.coroutines.Job? = null

    // Cache de todos os convites (sem o distinctBy) para facilitar a limpeza ao aceitar
    private var allInvitationsCache: List<Invitation> = emptyList()

    init {
        authRepository.authStateFlow.onEach { user ->
            dataCollectionJob?.cancel() // Cancela fluxos ativos ao mudar de estado de auth

            if (user == null) {
                _uiState.update { it.copy(user = null, isLoading = false, boloes = emptyList(), invitations = emptyList()) }
            } else {
                _uiState.update { it.copy(user = user) }
                loadUserData(user)
            }
        }.catch { e ->
            logger.e(e) { "Erro no authStateFlow" }
        }.launchIn(viewModelScope)
    }

    private fun loadUserData(user: User) {
        val invitationsFlow =
            combine(
                invitationRepository.getInvitationsForUser(user.email.trim().lowercase()),
                invitationRepository.getInvitationsForUser(user.id),
                invitationRepository.getInvitationsForUser(user.username.trim().lowercase()),
                invitationRepository.getInvitationsForUser(user.phone.filter { it.isDigit() })
            ) { list1, list2, list3, list4 ->
                val all = (list1 + list2 + list3 + list4).filter { it.id.isNotBlank() }
                allInvitationsCache = all
                all.distinctBy { it.bolaoId } // Garante apenas 1 convite por bolão na UI
            }.catch { emit(emptyList()) }

        // Todos os tipos de notificação (convite, pedido de entrada/saída,
        // resumo diário) já são gravados pelas Cloud Functions em
        // notifications/{id} - o sininho só reflete o que o servidor mandou,
        // sem recalcular nada localmente.
        dataCollectionJob =
            combine(
                bolaoRepository.getUserBoloes(user.id),
                invitationsFlow,
                notificationRepository.getNotifications(user.id)
            ) { boloes, invitations, notifications ->
                val sortedNotifications = notifications.sortedByDescending { it.timestamp }
                val hasUnread = sortedNotifications.any { !it.isRead }

                _uiState.update {
                    it.copy(
                        boloes = boloes,
                        invitations = invitations,
                        notifications = sortedNotifications,
                        hasUnreadNotifications = hasUnread,
                        isLoading = false
                    )
                }
            }.catch { e ->
                logger.e(e) { "Erro no dataCollectionJob" }
                _uiState.update { it.copy(isLoading = false, error = "Erro ao carregar dados.") }
            }.launchIn(viewModelScope)
    }

    fun markAllNotificationsAsRead() {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(userId)
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao marcar notificações como lidas")
                logger.e(e) { "Erro ao marcar notificações como lidas" }
            }
        }
    }

    fun respondToInvitation(invitationId: String, accept: Boolean, onSuccess: () -> Unit = {}) {
        val user = authRepository.currentUser ?: return
        val currentInvitations = uiState.value.invitations
        val targetInvitation = currentInvitations.find { it.id == invitationId } ?: return
        val bolaoId = targetInvitation.bolaoId

        logger.d { "[HomeVM] respondToInvitation iniciada. InvId: $invitationId, Accept: $accept, BolaoId: $bolaoId" }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Se aceitou, adiciona ao bolão primeiro
                if (accept) {
                    logger.d { "[HomeVM] Chamando addParticipantDirectly..." }
                    bolaoRepository.addParticipantDirectly(bolaoId, user.id)
                    logger.d { "[HomeVM] addParticipantDirectly concluído." }
                }

                // 2. Resolve (deleta) todos os convites pendentes deste usuário para este bolão
                val toResolve =
                    (allInvitationsCache + targetInvitation)
                        .filter { it.bolaoId == bolaoId }
                        .distinctBy { it.id }

                logger.d { "[HomeVM] Encontrados ${toResolve.size} convites para resolver." }

                toResolve.forEach { inv ->
                    logger.d { "[HomeVM] Deletando convite ${inv.id}..." }
                    invitationRepository.respondToInvitation(inv.id, accept)
                    logger.d { "[HomeVM] Convite ${inv.id} deletado." }
                }

                // 3. Limpeza local imediata
                allInvitationsCache =
                    allInvitationsCache.filter { inv ->
                        inv.bolaoId != bolaoId
                    }
                _uiState.update {
                    it.copy(
                        invitations = it.invitations.filter { inv -> inv.bolaoId != bolaoId },
                        isLoading = false
                    )
                }
                logger.d { "[HomeVM] Estado UI atualizado (convites filtrados)." }

                // 4. Só navega após o sucesso total
                if (accept) {
                    logger.d { "[HomeVM] Chamando callback de sucesso para navegação." }
                    onSuccess()
                }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao processar convite")
                logger.e(e) { "[HomeVM] ERRO ao processar convite" }

                val msg = e.message?.lowercase() ?: ""
                val friendly =
                    if (msg.contains("permission")) {
                        "Erro de permissão ao aceitar convite. Verifique se você já está no bolão."
                    } else {
                        "Não foi possível processar o convite. Tente novamente."
                    }

                _uiState.update { it.copy(error = friendly, isLoading = false) }
            }
        }
    }

    fun respondToJoinRequest(bolaoId: String, userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao responder pedido de entrada")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun respondToExitRequest(bolaoId: String, userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveLeaveRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao responder pedido de saída")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
