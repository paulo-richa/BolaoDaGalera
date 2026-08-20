package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class FirebaseSupportRepository : SupportRepository {
    private val db = Firebase.firestore

    override suspend fun sendSupportTicket(userId: String, userEmail: String, message: String) {
        // Usamos serverTimestamp para o ID e data para evitar erros de importação de Clock no DTO
        val timestamp = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis()
        val ticketId = "ticket_${timestamp}_$userId"
        
        db.collection("support_tickets").document(ticketId).set(
            mapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "message" to message,
                "timestamp" to timestamp,
                "status" to "OPEN"
            )
        )
    }
}
