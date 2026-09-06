package com.lpstudio.bolaodagalera.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.lpstudio.bolaodagalera.MainActivity
import com.lpstudio.bolaodagalera.R
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Mensagens são enviadas pelas Cloud Functions só com payload "data" (nunca
 * "notification") - assim onMessageReceived é sempre chamado, mesmo com o
 * app em background, e a gente controla o canal/ícone/deep link da
 * notificação em vez de deixar o Android montar algo genérico.
 */
class BolaoFirebaseMessagingService : FirebaseMessagingService() {
    private val logger = appLogger("BolaoFirebaseMessagingService")
    private val authRepository: AuthRepository by inject()
    private val notificationRepository: NotificationRepository by inject()
    private val crashReporter: CrashReporter by inject()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        val userId = authRepository.currentUser?.id ?: return
        scope.launch {
            try {
                notificationRepository.registerFcmToken(userId, token)
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao registrar fcmToken (onNewToken)")
                logger.e(e) { "Erro ao registrar fcmToken" }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.data["title"] ?: return
        val body = message.data["body"] ?: ""
        val deepLink = message.data["deepLink"]
        showNotification(title, body, deepLink)
    }

    private fun showNotification(title: String, body: String, deepLink: String?) {
        ensureChannel()

        val intent =
            Intent(this, MainActivity::class.java).apply {
                action = Intent.ACTION_VIEW
                if (!deepLink.isNullOrBlank()) data = Uri.parse(deepLink)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                System.currentTimeMillis().toInt(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        // The small (status bar) icon is always rendered as a flat white silhouette by the
        // system on API 21+, regardless of its actual colors - setLargeIcon is what actually
        // shows the app's real, colored logo in the notification body.
        val largeIcon = BitmapFactory.decodeResource(resources, R.drawable.logo_oficial)

        val notification =
            NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(System.currentTimeMillis().toInt(), notification)
        }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANNEL_ID, "Bolão da Galera", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Lembretes de palpite, convites e resultados"
                }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "bolao_default"
    }
}
