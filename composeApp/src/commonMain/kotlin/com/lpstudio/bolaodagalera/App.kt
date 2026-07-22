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
import com.lpstudio.bolaodagalera.di.appModule
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.presentation.maintenance.MaintenanceScreen
import com.lpstudio.bolaodagalera.presentation.navigation.NavGraph
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// ── Configuração de dados ─────────────────────
expect val APP_VERSION: String

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        val remoteConfigManager = koinInject<RemoteConfigManager>()
        val authRepository = koinInject<AuthRepository>()
        val championshipRepository = koinInject<ChampionshipRepository>()
        
        val isMaintenanceMode by remoteConfigManager.isMaintenanceMode.collectAsState()
        val currentUser by authRepository.authStateFlow.collectAsState(initial = authRepository.currentUser)
        
        val shouldShowMaintenance = isMaintenanceMode && currentUser?.email != "paulo.richa@hotmail.com"

        LaunchedEffect(Unit) {
            try {
                // Ativa verificação de manutenção primeiro
                remoteConfigManager.fetchAndActivate()

                // Inicia carregamento de campeonatos
                championshipRepository.refreshCache()
            } catch (e: Exception) {
                // Falha silenciosa - app continua funcionando
            }
        }
        
        // Inicia observação dos campeonatos para manter o cache atualizado
        val championships by championshipRepository.getChampionships().collectAsState(initial = emptyList())

        AppTheme {
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
                        MaintenanceScreen()
                    } else {
                        NavGraph()
                    }
                }
            }
        }
    }
}
