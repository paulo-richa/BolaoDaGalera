package com.lpstudio.bolaodagalera

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.ads.MobileAds
import com.google.firebase.messaging.FirebaseMessaging
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.util.AdManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val logger = appLogger("MainActivity")
    private val authRepository: AuthRepository by inject()
    private val notificationRepository: NotificationRepository by inject()
    private val crashReporter: CrashReporter by inject()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* user decided, nothing to do here */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Initialize AdMob
        MobileAds.initialize(this) {}

        // Provide the activity to AdManager
        AdManager.init(this)

        requestNotificationPermissionIfNeeded()
        registerFcmTokenOnLogin()

        setContent {
            SystemAppearance(isDark = true)
            App()
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val alreadyGranted =
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        if (!alreadyGranted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // onNewToken only fires when Firebase generates a NEW token - on an already
    // installed app the token may already exist from before the user logged in,
    // so we register the current token every time the login state changes.
    private fun registerFcmTokenOnLogin() {
        lifecycleScope.launch {
            authRepository.authStateFlow.collectLatest { user ->
                if (user == null) return@collectLatest
                try {
                    val token = getFcmToken()
                    notificationRepository.registerFcmToken(user.id, token)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // authStateFlow emitted again (e.g. cache -> real Firestore doc)
                    // before the token fetch completed - collectLatest cancels this
                    // collection and already reprocesses with the new value, nothing to do here.
                    throw e
                } catch (e: Exception) {
                    crashReporter.recordException(e, "Erro ao registrar fcmToken no login")
                    logger.e(e) { "Erro ao registrar fcmToken no login" }
                }
            }
        }
    }

    private suspend fun getFcmToken(): String = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> continuation.resume(token) }
            .addOnFailureListener { e -> continuation.resumeWithException(e) }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
