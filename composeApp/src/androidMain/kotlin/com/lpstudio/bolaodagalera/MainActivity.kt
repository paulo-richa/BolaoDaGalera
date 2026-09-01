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
import com.lpstudio.bolaodagalera.util.AdManager
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val authRepository: AuthRepository by inject()
    private val notificationRepository: NotificationRepository by inject()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* usuário decidiu, nada a fazer aqui */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Inicializa o AdMob
        MobileAds.initialize(this) {}

        // Provê a atividade para o AdManager
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

    // onNewToken só dispara quando o Firebase gera um token NOVO - num app já
    // instalado o token pode já existir de antes do usuário logar, então
    // registramos o token atual toda vez que o estado de login muda.
    private fun registerFcmTokenOnLogin() {
        lifecycleScope.launch {
            authRepository.authStateFlow.collectLatest { user ->
                if (user == null) return@collectLatest
                try {
                    val token = getFcmToken()
                    notificationRepository.registerFcmToken(user.id, token)
                } catch (e: kotlinx.coroutines.CancellationException) {
                    // authStateFlow emitiu de novo (ex: cache -> doc real do Firestore)
                    // antes do token terminar de buscar - collectLatest cancela essa
                    // coleta e já reprocessa com o valor novo, nada a fazer aqui.
                    throw e
                } catch (e: Exception) {
                    println("BOLAOLOG: Erro ao registrar fcmToken no login: ${e.message}")
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
