package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Immutable
@Serializable
data class Notification(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: NotificationType,
    val isRead: Boolean = false,
    val matchId: String? = null,
    val bolaoId: String? = null,
)

enum class NotificationType {
    MATCH_REMINDER, // Lembrete de jogo para palpitar
    RESULT_READY, // Placar atualizado e pontos somados
    INVITATION, // Novo convite para bolão
    JOIN_REQUEST, // Admin: Alguém quer entrar no seu bolão
    EXIT_REQUEST, // Admin: Alguém quer sair do seu bolão
    SYSTEM, // Avisos gerais
}
