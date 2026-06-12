package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Invitation
import kotlinx.coroutines.flow.Flow

interface InvitationRepository {
    fun getInvitationsForUser(identifier: String): Flow<List<Invitation>>
    suspend fun sendInvitation(bolaoId: String, bolaoName: String, inviterName: String, inviteeIdentifier: String)
    suspend fun respondToInvitation(invitationId: String, accept: Boolean)
}
