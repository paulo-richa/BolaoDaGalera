package com.lpstudio.bolaodagalera

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import com.lpstudio.bolaodagalera.di.appModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.presentation.maintenance.MaintenanceScreen
import com.lpstudio.bolaodagalera.presentation.navigation.NavGraph
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
import com.lpstudio.bolaodagalera.util.AdManager
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// ── Configuração de dados ─────────────────────

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        val remoteConfigManager = koinInject<RemoteConfigManager>()
        val authRepository = koinInject<AuthRepository>()
        val championshipRepository = koinInject<ChampionshipRepository>()
        
        val showAds by remoteConfigManager.showAds.collectAsState()

        // Atualiza o estado global de anúncios para o AdManager (interstitials)
        LaunchedEffect(showAds) {
            AdManager.setEnabled(showAds)
        }

        // Pré-carrega anúncios (Interstitiais) se estiverem ativados
        LaunchedEffect(Unit) {
            AdManager.prepare()
        }
        
        val scope = rememberCoroutineScope()
        
        val isMaintenanceMode by remoteConfigManager.isMaintenanceMode.collectAsState()
        val currentUser by authRepository.authStateFlow.collectAsState(initial = authRepository.currentUser)
        
        val shouldShowMaintenance = currentUser != null && 
                isMaintenanceMode && 
                currentUser?.email != "paulo.richa@hotmail.com" && 
                currentUser?.email != "redacted@example.com"

        LaunchedEffect(currentUser) {
            try {
                // Remote Config pode ser buscado sem login
                remoteConfigManager.fetchAndActivate()

                // Operações no Firestore DEVEM aguardar login para evitar PERMISSION_DENIED
                if (currentUser != null) {
                    // Inicia carregamento de campeonatos e mantém cache atualizado
                    championshipRepository.refreshCache()
                    championshipRepository.getChampionships().collect { }
                }
            } catch (e: Exception) {
                // Falha silenciosa
            }
        }

        AppTheme {
            CompositionLocalProvider(LocalAdsEnabled provides showAds) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(DeepNavy)
                        .navigationBarsPadding() // Resolve sobreposição em todas as telas
                ) {
                    // Background sólido para a status bar para dar destaque aos ícones
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(DeepNavy)
                    )
                    Box(Modifier.weight(1f)) {
                        if (shouldShowMaintenance) {
                            MaintenanceScreen(onLogout = {
                                scope.launch { authRepository.signOut() }
                            })
                        } else {
                            NavGraph()
                        }
                    }
                }
            }
        }
    }
}

val LocalAdsEnabled = staticCompositionLocalOf { true }
