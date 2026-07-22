package com.lpstudio.bolaodagalera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.*
import com.lpstudio.bolaodagalera.domain.repository.*
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

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
    private val matchRepository: MatchRepository,
    private val invitationRepository: InvitationRepository,
    private val predictionRepository: PredictionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val readNotificationIds = MutableStateFlow<Set<String>>(emptySet())
    private var dataCollectionJob: kotlinx.coroutines.Job? = null

    init {
        authRepository.authStateFlow.onEach { user ->
            dataCollectionJob?.cancel() // Cancela fluxos ativos ao mudar de estado de auth
            
            if (user == null) {
                _uiState.update { it.copy(user = null, isLoading = false, boloes = emptyList(), invitations = emptyList()) }
            } else {
                _uiState.update { it.copy(user = user) }
                loadUserData(user)
            }
        }.launchIn(viewModelScope)
    }

    private fun loadUserData(user: User) {
        val invitationsFlow = combine(
            invitationRepository.getInvitationsForUser(user.email.trim().lowercase()),
            invitationRepository.getInvitationsForUser(user.id),
            invitationRepository.getInvitationsForUser(user.username.trim().lowercase()),
            invitationRepository.getInvitationsForUser(user.phone.filter { it.isDigit() })
        ) { list1, list2, list3, list4 ->
            (list1 + list2 + list3 + list4)
                .filter { it.id.isNotBlank() }
                .distinctBy { it.bolaoId } // Garante apenas 1 convite por bolão
        }

        // Carrega Bolões, Jogos, Palpites, Convites e IDs lidos
        dataCollectionJob = combine(
            bolaoRepository.getUserBoloes(user.id),
            matchRepository.getAllMatches(),
            predictionRepository.getUserPredictions(user.id, ""),
            invitationsFlow,
            readNotificationIds
        ) { boloes, matches, predictions, invitations, readIds ->
            
            val allGenerated = mutableListOf<Notification>()

            // 1. Notificações de Convite
            invitations.forEach { invitation ->
                val id = "invitation_${invitation.id}"
                allGenerated.add(
                    Notification(
                        id = id,
                        title = "Novo Convite! 📩",
                        message = "${invitation.inviterName} te convidou para o bolão '${invitation.bolaoName}'.",
                        timestamp = invitation.createdAtMillis,
                        type = NotificationType.INVITATION,
                        isRead = readIds.contains(id),
                        bolaoId = invitation.bolaoId
                    )
                )
            }

            // 1.1 Notificações de Solicitações (Admin)
            boloes.filter { it.ownerId == user.id }.forEach { bolao ->
                bolao.pendingParticipants.forEach { pUserId ->
                    val id = "join_req_${bolao.id}_$pUserId"
                    allGenerated.add(
                        Notification(
                            id = id,
                            title = "Pedido para entrar 👤",
                            message = "Alguém quer entrar no seu bolão '${bolao.name}'.",
                            timestamp = TimeSource.nowMillis(),
                            type = NotificationType.JOIN_REQUEST,
                            isRead = readIds.contains(id),
                            bolaoId = bolao.id,
                            matchId = pUserId // Reuso matchId para guardar o userId do solicitante
                        )
                    )
                }
                bolao.pendingExits.forEach { pUserId ->
                    val id = "exit_req_${bolao.id}_$pUserId"
                    allGenerated.add(
                        Notification(
                            id = id,
                            title = "Pedido para sair 🚩",
                            message = "Alguém quer sair do seu bolão '${bolao.name}'.",
                            timestamp = TimeSource.nowMillis(),
                            type = NotificationType.EXIT_REQUEST,
                            isRead = readIds.contains(id),
                            bolaoId = bolao.id,
                            matchId = pUserId // Reuso matchId para guardar o userId do solicitante
                        )
                    )
                }
            }
            
            // 2. Notificações de Lembrete de Jogos
            val today = Instant.fromEpochMilliseconds(TimeSource.nowMillis()).toLocalDateTime(TimeZone.currentSystemDefault()).date
            val matchesToday = matches.filter { 
                Instant.fromEpochMilliseconds(it.matchDateMillis)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date == today
            }

            if (matchesToday.isNotEmpty()) {
                val predictionMatchIds = predictions.map { it.matchId }.toSet()
                val missingCount = matchesToday.count { it.id !in predictionMatchIds }
                
                if (missingCount > 0) {
                    val id = "reminder_today_${today}"
                    allGenerated.add(
                        Notification(
                            id = id,
                            title = "Jogos de Hoje! ⚽",
                            message = "Você tem $missingCount jogo(s) hoje sem palpite. Não perca pontos!",
                            timestamp = TimeSource.nowMillis(),
                            type = NotificationType.MATCH_REMINDER,
                            isRead = readIds.contains(id)
                        )
                    )
                }
            }

            val sortedNotifications = allGenerated.sortedByDescending { n -> n.timestamp }
            val hasUnread = sortedNotifications.any { !it.isRead }

            _uiState.update { it.copy(
                boloes = boloes, 
                invitations = invitations,
                notifications = sortedNotifications,
                hasUnreadNotifications = hasUnread,
                isLoading = false 
            ) }
        }.launchIn(viewModelScope)
    }

    fun markAllNotificationsAsRead() {
        val allIds = uiState.value.notifications.map { it.id }.toSet()
        readNotificationIds.value = readNotificationIds.value + allIds
    }

    fun respondToInvitation(invitationId: String, accept: Boolean) {
        val user = authRepository.currentUser ?: return
        val currentInvitations = uiState.value.invitations
        val targetInvitation = currentInvitations.find { it.id == invitationId } ?: return
        val bolaoId = targetInvitation.bolaoId

        viewModelScope.launch {
            try {
                if (accept) {
                    bolaoRepository.addParticipantDirectly(bolaoId, user.id)
                }
                
                // Encontra TODOS os convites pendentes que este usuário tem para ESTE bolão específico
                // (Seja convite por e-mail, por ID ou por telefone)
                val userIdentifiers = setOf(
                    user.id,
                    user.email.trim().lowercase(),
                    user.username.trim().lowercase(),
                    user.phone.filter { it.isDigit() }
                ).filter { it.isNotBlank() }

                // Busca no banco todos os convites pendentes para este bolão que batam com os IDs do usuário
                val allRelatedInvitations = invitationRepository.getInvitationsForUser(user.id).first() +
                                           invitationRepository.getInvitationsForUser(user.email.trim().lowercase()).first() +
                                           invitationRepository.getInvitationsForUser(user.username.trim().lowercase()).first() +
                                           invitationRepository.getInvitationsForUser(user.phone.filter { it.isDigit() }).first()

                val toResolve = allRelatedInvitations
                    .filter { it.bolaoId == bolaoId && it.status == InvitationStatus.PENDING }
                    .distinctBy { it.id }

                // Resolve todos de uma vez
                withTimeout(5000) {
                    toResolve.forEach { inv ->
                        invitationRepository.respondToInvitation(inv.id, accept)
                    }
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun respondToJoinRequest(bolaoId: String, userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveJoinRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun respondToExitRequest(bolaoId: String, userId: String, approve: Boolean) {
        viewModelScope.launch {
            try {
                bolaoRepository.approveLeaveRequest(bolaoId, userId, approve)
            } catch (e: Exception) {
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
