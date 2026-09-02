package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeNotificationRepository : NotificationRepository {
    private val notifications = MutableStateFlow<List<Notification>>(emptyList())
    private val tokensByUser = mutableMapOf<String, MutableSet<String>>()

    /** Simulates a notification already written by a Cloud Function, for tests. */
    fun seed(userId: String, notification: Notification) {
        notifications.value = notifications.value + notification.copy(id = "$userId:${notification.id}")
    }

    override fun getNotifications(userId: String): Flow<List<Notification>> =
        notifications.map { list -> list.filter { it.id.startsWith("$userId:") } }

    override suspend fun markAsRead(notificationId: String) {
        notifications.value = notifications.value.map { if (it.id == notificationId) it.copy(isRead = true) else it }
    }

    override suspend fun markAllAsRead(userId: String) {
        notifications.value =
            notifications.value.map { if (it.id.startsWith("$userId:")) it.copy(isRead = true) else it }
    }

    override suspend fun registerFcmToken(userId: String, token: String) {
        tokensByUser.getOrPut(userId) { mutableSetOf() }.add(token)
    }

    override suspend fun unregisterFcmToken(userId: String, token: String) {
        tokensByUser[userId]?.remove(token)
    }
}
