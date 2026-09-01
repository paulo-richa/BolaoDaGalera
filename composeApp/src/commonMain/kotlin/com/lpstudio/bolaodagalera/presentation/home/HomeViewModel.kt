package com.lpstudio.bolaodagalera.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.NotificationType
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private data class HomeBaseData(
    val boloes: List<Bolao>,
    val matches: List<com.lpstudio.bolaodagalera.domain.model.Match>,
    val predictions: List<com.lpstudio.bolaodagalera.domain.model.Prediction>,
    val invitations: List<Invitation>,
    val readIds: Set<String>
)

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
    private val predictionRepository: PredictionRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val readNotificationIds = MutableStateFlow<Set<String>>(emptySet())
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
            println("BOLAOLOG: Erro no authStateFlow: ${e.message}")
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

        // Carrega Bolões, Jogos, Palpites, Convites e IDs lidos
        val baseFlow =
            combine(
                bolaoRepository.getUserBoloes(user.id),
                matchRepository.getAllMatches(),
                predictionRepository.getUserPredictions(user.id, ""),
                invitationsFlow,
                readNotificationIds
            ) { boloes, matches, predictions, invitations, readIds ->
                HomeBaseData(boloes, matches, predictions, invitations, readIds)
            }

        // Combina com as notificações persistidas (gravadas pelas Cloud Functions).
        // Tipos que ainda não têm um evento server-side equivalente (ex: lembrete
        // de jogos de hoje) continuam sendo sintetizados localmente aqui embaixo -
        // as duas listas são só mescladas por id, sem duplicar.
        dataCollectionJob =
            combine(baseFlow, notificationRepository.getNotifications(user.id)) { base, persisted ->
                val (boloes, matches, predictions, invitations, readIds) = base

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
                                matchId = pUserId
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
                                matchId = pUserId
                            )
                        )
                    }
                }

                // 2. Notificações de Lembrete de Jogos
                val now = TimeSource.nowMillis()
                val today = Instant.fromEpochMilliseconds(now)
                    .toLocalDateTime(TimeZone.currentSystemDefault()).date
                val matchesToday =
                    matches.filter {
                        Instant.fromEpochMilliseconds(it.matchDateMillis)
                            .toLocalDateTime(TimeZone.currentSystemDefault()).date == today
                    }

                if (matchesToday.isNotEmpty()) {
                    val predictionMatchIds = predictions.map { it.matchId }.toSet()
                    val missingCount = matchesToday.count { it.id !in predictionMatchIds }

                    if (missingCount > 0) {
                        val id = "reminder_today_$today"
                        allGenerated.add(
                            Notification(
                                id = id,
                                title = "Jogos de Hoje! ⚽",
                                message = "Você tem $missingCount jogo(s) hoje sem palpite. " +
                                    "Não perca pontos!",
                                timestamp = TimeSource.nowMillis(),
                                type = NotificationType.MATCH_REMINDER,
                                isRead = readIds.contains(id)
                            )
                        )
                    }
                }

                val sortedNotifications =
                    (allGenerated + persisted)
                        .distinctBy { it.id }
                        .sortedByDescending { n -> n.timestamp }
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
                println("BOLAOLOG: Erro no dataCollectionJob: ${e.message}")
                _uiState.update { it.copy(isLoading = false, error = "Erro ao carregar dados.") }
            }.launchIn(viewModelScope)
    }

    fun markAllNotificationsAsRead() {
        val allIds = uiState.value.notifications.map { it.id }.toSet()
        readNotificationIds.value = readNotificationIds.value + allIds

        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            try {
                notificationRepository.markAllAsRead(userId)
            } catch (e: Exception) {
                println("BOLAOLOG: Erro ao marcar notificações como lidas: ${e.message}")
            }
        }
    }

    fun respondToInvitation(invitationId: String, accept: Boolean, onSuccess: () -> Unit = {}) {
        val user = authRepository.currentUser ?: return
        val currentInvitations = uiState.value.invitations
        val targetInvitation = currentInvitations.find { it.id == invitationId } ?: return
        val bolaoId = targetInvitation.bolaoId

        println("BOLAOLOG: [HomeVM] respondToInvitation iniciada. InvId: $invitationId, Accept: $accept, BolaoId: $bolaoId")

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // 1. Se aceitou, adiciona ao bolão primeiro
                if (accept) {
                    println("BOLAOLOG: [HomeVM] Chamando addParticipantDirectly...")
                    bolaoRepository.addParticipantDirectly(bolaoId, user.id)
                    println("BOLAOLOG: [HomeVM] addParticipantDirectly concluído.")
                }

                // 2. Resolve (deleta) todos os convites pendentes deste usuário para este bolão
                val toResolve =
                    (allInvitationsCache + targetInvitation)
                        .filter { it.bolaoId == bolaoId }
                        .distinctBy { it.id }

                println("BOLAOLOG: [HomeVM] Encontrados ${toResolve.size} convites para resolver.")

                toResolve.forEach { inv ->
                    println("BOLAOLOG: [HomeVM] Deletando convite ${inv.id}...")
                    invitationRepository.respondToInvitation(inv.id, accept)
                    println("BOLAOLOG: [HomeVM] Convite ${inv.id} deletado.")
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
                println("BOLAOLOG: [HomeVM] Estado UI atualizado (convites filtrados).")

                // 4. Só navega após o sucesso total
                if (accept) {
                    println("BOLAOLOG: [HomeVM] Chamando callback de sucesso para navegação.")
                    onSuccess()
                }
            } catch (e: Exception) {
                println("BOLAOLOG: [HomeVM] ERRO ao processar convite: ${e.message}")

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
