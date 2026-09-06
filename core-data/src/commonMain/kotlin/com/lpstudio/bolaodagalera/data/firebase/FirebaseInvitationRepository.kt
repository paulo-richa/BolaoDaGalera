package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.InvitationStatus
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.observability.reportAndRethrow
import com.lpstudio.bolaodagalera.util.TimeSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class InvitationDto(
    val bolaoId: String = "",
    val bolaoName: String = "",
    val inviterName: String = "",
    val inviteeIdentifier: String = "",
    val status: String = "PENDING",
    val createdAtMillis: Long = 0L
)

private fun InvitationDto.toDomain(id: String) = Invitation(
    id = id,
    bolaoId = bolaoId,
    bolaoName = bolaoName,
    inviterName = inviterName,
    inviteeIdentifier = inviteeIdentifier,
    status = InvitationStatus.valueOf(status),
    createdAtMillis = createdAtMillis
)

class FirebaseInvitationRepository(private val crashReporter: CrashReporter) : InvitationRepository {
    private val logger = appLogger("FirebaseInvitationRepository")
    private val db = Firebase.firestore
    private val collection = db.collection("invitations")

    override fun getInvitationsForUser(identifier: String): Flow<List<Invitation>> = try {
        collection
            .where { "inviteeIdentifier" equalTo identifier }
            .where { "status" equalTo "PENDING" }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<InvitationDto>().toDomain(doc.id)
                }
            }
            .reportAndRethrow(crashReporter, "Erro ao observar convites")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar convites")
        logger.e(e) { "Erro crítico ao observar convites" }
        kotlinx.coroutines.flow.flow { throw e }
    }

    override suspend fun sendInvitation(bolaoId: String, bolaoName: String, inviterName: String, inviteeIdentifier: String) {
        // No pre-check for an existing pending invitation here: the security rules only let a
        // user read/query invitations addressed to themselves (isMyInvitation), so a query by
        // the inviter for the invitee's identifier is always rejected with PERMISSION_DENIED,
        // which used to abort sendInvitation before it ever created the document. Duplicates
        // are handled on the read side instead: DedupeInvitationsByBolaoUseCase collapses
        // multiple pending invitations for the same bolão into one for display, and
        // RespondToInvitationUseCase resolves every pending invitation for that bolão at once
        // when the invitee accepts or declines.
        val dto =
            InvitationDto(
                bolaoId = bolaoId,
                bolaoName = bolaoName,
                inviterName = inviterName,
                inviteeIdentifier = inviteeIdentifier,
                status = "PENDING",
                createdAtMillis = TimeSource.nowMillis()
            )
        collection.add(dto)
    }

    override suspend fun respondToInvitation(invitationId: String, accept: Boolean) {
        // Instead of just updating the status, delete the invitation
        // since it has already been processed (accepted or declined)
        collection.document(invitationId).delete()
    }
}
