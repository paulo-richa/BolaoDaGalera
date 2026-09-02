package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(userId: String): Flow<List<Notification>>

    suspend fun markAsRead(notificationId: String)

    suspend fun markAllAsRead(userId: String)

    suspend fun registerFcmToken(userId: String, token: String)

    suspend fun unregisterFcmToken(userId: String, token: String)
}
