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
    // Reminder to submit a prediction for a match
    MATCH_REMINDER,

    // Score updated and points tallied
    RESULT_READY,

    // New invitation to a pool
    INVITATION,

    // Admin: someone wants to join your pool
    JOIN_REQUEST,

    // Admin: someone wants to leave your pool
    EXIT_REQUEST,

    // End-of-round/phase summary: hits, misses and points for the user in that period
    ROUND_SUMMARY,

    // General announcements
    SYSTEM
}
