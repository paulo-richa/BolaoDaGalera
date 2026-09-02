package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository

/**
 * Accepts or declines a pool invitation. On accept, adds the participant to the
 * pool first so the invitation resolution below can't leave the user "accepted"
 * but not actually a member. A user may have received the same pool's invite
 * through more than one identifier (email, phone, username): every matching
 * pending invitation for that pool is resolved, not just the one the UI acted on.
 */
class RespondToInvitationUseCase(private val bolaoRepository: BolaoRepository, private val invitationRepository: InvitationRepository) {
    suspend operator fun invoke(userId: String, targetInvitation: Invitation, allInvitations: List<Invitation>, accept: Boolean) {
        if (accept) {
            bolaoRepository.addParticipantDirectly(targetInvitation.bolaoId, userId)
        }

        val toResolve =
            (allInvitations + targetInvitation)
                .filter { it.bolaoId == targetInvitation.bolaoId }
                .distinctBy { it.id }

        toResolve.forEach { invitation -> invitationRepository.respondToInvitation(invitation.id, accept) }
    }
}
