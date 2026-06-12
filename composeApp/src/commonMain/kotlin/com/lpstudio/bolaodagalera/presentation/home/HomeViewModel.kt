package com.lpstudio.bolaodagalera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import com.lpstudio.bolaodagalera.domain.model.*
import com.lpstudio.bolaodagalera.domain.repository.*
import kotlinx.coroutines.flow.*
import com.lpstudio.bolaodagalera.util.TimeSource
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

    init {
        authRepository.authStateFlow.onEach { user ->
            if (user == null) {
                _uiState.update { it.copy(user = null, isLoading = false) }
            } else {
                _uiState.update { it.copy(user = user) }
                loadUserData(user)
            }
        }.launchIn(viewModelScope)
    }

    private fun loadUserData(user: User) {
        val invitationsFlow = combine(
            invitationRepository.getInvitationsForUser(user.email),
            invitationRepository.getInvitationsForUser(user.id),
            invitationRepository.getInvitationsForUser(user.username),
            invitationRepository.getInvitationsForUser(user.phone)
        ) { list1, list2, list3, list4 ->
            (list1 + list2 + list3 + list4)
                .filter { it.id.isNotBlank() }
                .distinctBy { it.bolaoId } // Garante apenas 1 convite por bolão
        }

        // Carrega Bolões, Jogos, Palpites, Convites e IDs lidos
        combine(
            bolaoRepository.getUserBoloes(user.id),
            matchRepository.getMatches(),
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
                
                // Encontra todos os convites pendentes para este mesmo bolão e responde a todos
                // Isso garante que duplicados (e-mail/ID/telefone) sumam de uma vez só
                val relatedInvitations = currentInvitations.filter { it.bolaoId == bolaoId }
                relatedInvitations.forEach { inv ->
                    invitationRepository.respondToInvitation(inv.id, accept)
                }

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
