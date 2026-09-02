package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.repository.SupportRepository

class FakeSupportRepository : SupportRepository {
    private val tickets = mutableListOf<Triple<String, String, String>>()

    override suspend fun sendSupportTicket(userId: String, userEmail: String, message: String) {
        tickets.add(Triple(userId, userEmail, message))
    }
}
