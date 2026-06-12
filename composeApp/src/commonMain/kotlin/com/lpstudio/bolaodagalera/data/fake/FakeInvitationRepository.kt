package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.InvitationStatus
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// Singleton global para garantir que os dados persistam na memória do processo
private val globalInvitations = MutableStateFlow<List<Invitation>>(listOf(
    Invitation(
        id = "inv-0",
        bolaoId = "bolao-1",
        bolaoName = "Copa da Galera 2026",
        inviterName = "Jogador Teste",
        inviteeIdentifier = "amigo@email.com",
        status = InvitationStatus.PENDING,
        createdAtMillis = 0L
    )
))

class FakeInvitationRepository : InvitationRepository {

    override fun getInvitationsForUser(identifier: String): Flow<List<Invitation>> {
        return globalInvitations.map { list ->
            if (identifier.isBlank()) return@map emptyList()
            
            val target = identifier.trim().lowercase()
            list.filter { 
                (it.inviteeIdentifier.trim().lowercase() == target) && 
                it.status == InvitationStatus.PENDING 
            }
        }
    }

    override suspend fun sendInvitation(
        bolaoId: String,
        bolaoName: String,
        inviterName: String,
        inviteeIdentifier: String
    ) {
        val newInvitation = Invitation(
            id = "inv-${globalInvitations.value.size + 1}",
            bolaoId = bolaoId,
            bolaoName = bolaoName,
            inviterName = inviterName,
            inviteeIdentifier = inviteeIdentifier,
            status = InvitationStatus.PENDING,
            createdAtMillis = TimeSource.nowMillis()
        )
        globalInvitations.value = globalInvitations.value + newInvitation
    }

    override suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        globalInvitations.value = globalInvitations.value.map {
            if (it.id == invitationId) {
                it.copy(status = if (accept) InvitationStatus.ACCEPTED else InvitationStatus.DECLINED)
            } else it
        }
    }
}
