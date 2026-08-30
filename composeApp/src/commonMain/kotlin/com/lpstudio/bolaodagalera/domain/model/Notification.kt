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
    val bolaoId: String? = null
)

enum class NotificationType {
    // Lembrete de jogo para palpitar
    MATCH_REMINDER,

    // Placar atualizado e pontos somados
    RESULT_READY,

    // Novo convite para bolão
    INVITATION,

    // Admin: Alguém quer entrar no seu bolão
    JOIN_REQUEST,

    // Admin: Alguém quer sair do seu bolão
    EXIT_REQUEST,

    // Avisos gerais
    SYSTEM
}
