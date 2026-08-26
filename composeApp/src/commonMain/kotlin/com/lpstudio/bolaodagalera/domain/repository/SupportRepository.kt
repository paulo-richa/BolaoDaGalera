package com.lpstudio.bolaodagalera.domain.repository

interface SupportRepository {
    suspend fun sendSupportTicket(
        userId: String,
        userEmail: String,
        message: String,
    )
}
