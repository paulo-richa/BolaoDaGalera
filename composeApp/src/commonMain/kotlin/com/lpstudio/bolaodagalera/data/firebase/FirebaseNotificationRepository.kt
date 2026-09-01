package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Notification
import com.lpstudio.bolaodagalera.domain.model.NotificationType
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOf
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
 * Notificações são gravadas só pelas Cloud Functions (Admin SDK, ignora as
 * regras) - o cliente só lê e marca como lido. Isso garante que o histórico
 * reflete exatamente o que foi de fato enviado por push, sem depender de
 * cálculo local que se perde ao reiniciar o app.
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
            .catch { e ->
                crashReporter.recordException(e, "Erro ao observar notificações")
                logger.e(e) { "Erro ao observar notificações" }
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar notificações")
        logger.e(e) { "Erro crítico ao observar notificações" }
        flowOf(emptyList())
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
