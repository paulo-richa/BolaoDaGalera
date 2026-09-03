package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.NotificationType
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.observability.reportAndRethrow
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class NotificationDto(
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "SYSTEM",
    val createdAtMillis: Long = 0L,
    val read: Boolean = false,
    val bolaoId: String? = null,
    val matchId: String? = null
)

private fun NotificationDto.toDomain(id: String) = Notification(
    id = id,
    title = title,
    message = message,
    timestamp = createdAtMillis,
    type = runCatching { NotificationType.valueOf(type) }.getOrDefault(NotificationType.SYSTEM),
    isRead = read,
    matchId = matchId,
    bolaoId = bolaoId
)

/**
 * Notifications are written only by Cloud Functions (Admin SDK, bypasses
 * security rules) - the client only reads and marks them as read. This
 * guarantees the history reflects exactly what was actually sent via push,
 * without relying on local state that would be lost on app restart.
 */
class FirebaseNotificationRepository(private val crashReporter: CrashReporter) : NotificationRepository {
    private val logger = appLogger("FirebaseNotificationRepository")
    private val db = Firebase.firestore
    private val collection = db.collection("notifications")
    private val usersCollection = db.collection("users")

    override fun getNotifications(userId: String): Flow<List<Notification>> = try {
        collection
            .where { "userId" equalTo userId }
            .orderBy("createdAtMillis", Direction.DESCENDING)
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc -> doc.data<NotificationDto>().toDomain(doc.id) }
            }
            .reportAndRethrow(crashReporter, "Erro ao observar notificações")
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar notificações")
        logger.e(e) { "Erro crítico ao observar notificações" }
        flow { throw e }
    }

    override suspend fun markAsRead(notificationId: String) {
        collection.document(notificationId).update("read" to true)
    }

    override suspend fun markAllAsRead(userId: String) {
        val pending = collection.where { "userId" equalTo userId }.where { "read" equalTo false }.get()
        for (doc in pending.documents) {
            collection.document(doc.id).update("read" to true)
        }
    }

    override suspend fun registerFcmToken(userId: String, token: String) {
        usersCollection.document(userId).update("fcmTokens" to FieldValue.arrayUnion(token))
    }

    override suspend fun unregisterFcmToken(userId: String, token: String) {
        usersCollection.document(userId).update("fcmTokens" to FieldValue.arrayRemove(token))
    }
}
