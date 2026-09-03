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
        val existing =
            collection
                .where { "bolaoId" equalTo bolaoId }
                .where { "inviteeIdentifier" equalTo inviteeIdentifier }
                .where { "status" equalTo "PENDING" }
                .get()

        if (existing.documents.isNotEmpty()) {
            // If a pending invitation already exists, just update the timestamp and inviter name (in case it changed)
            val docId = existing.documents.first().id
            collection.document(docId).update(
                "createdAtMillis" to TimeSource.nowMillis(),
                "inviterName" to inviterName
            )
            return
        }

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
