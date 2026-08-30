package com.lpstudio.bolaodagalera.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class InvitationStatus {
    PENDING,
    ACCEPTED,
    DECLINED
}

@Serializable
data class Invitation(
    val id: String,
    val bolaoId: String,
    val bolaoName: String,
    val inviterName: String,
    /** Can be email, phone or userId */
    val inviteeIdentifier: String,
    val status: InvitationStatus = InvitationStatus.PENDING,
    val createdAtMillis: Long
)
