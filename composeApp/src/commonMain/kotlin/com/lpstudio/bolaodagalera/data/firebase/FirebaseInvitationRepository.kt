package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Invitation
import com.lpstudio.bolaodagalera.domain.model.InvitationStatus
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.util.TimeSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
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

class FirebaseInvitationRepository(
    private val bolaoRepository: BolaoRepository
) : InvitationRepository {

    private val db = Firebase.firestore
    private val collection = db.collection("invitations")

    override fun getInvitationsForUser(identifier: String): Flow<List<Invitation>> {
        return collection
            .where { "inviteeIdentifier" equalTo identifier }
            .where { "status" equalTo "PENDING" }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<InvitationDto>().toDomain(doc.id)
                }
            }
    }

    override suspend fun sendInvitation(
        bolaoId: String,
        bolaoName: String,
        inviterName: String,
        inviteeIdentifier: String
    ) {
        val dto = InvitationDto(
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
        val newStatus = if (accept) "ACCEPTED" else "DECLINED"
        collection.document(invitationId).update("status" to newStatus)
        
        if (accept) {
            val doc = collection.document(invitationId).get()
            val dto = doc.data<InvitationDto>()
            // Note: We might need a userId here, assuming inviteeIdentifier WAS the userId
            // Or we handle the join logic in a ViewModel that calls bolaoRepository.joinBolao
            // For now, this is just updating the status.
        }
    }
}
