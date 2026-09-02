package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Invitation

/**
 * A user can be invited to the same pool through more than one identifier
 * (email, phone, username) at once; the UI shows at most one invitation per pool.
 */
class DedupeInvitationsByBolaoUseCase {
    operator fun invoke(invitations: List<Invitation>): List<Invitation> =
        invitations.filter { it.id.isNotBlank() }.distinctBy { it.bolaoId }
}
