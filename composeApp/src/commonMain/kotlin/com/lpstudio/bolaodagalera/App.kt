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
import com.lpstudio.bolaodagalera.di.fakeAppModule
import com.lpstudio.bolaodagalera.di.openFootballAppModule
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.presentation.navigation.NavGraph
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

// ... (constants unchanged)

// ── Configuração de dados (expect/actual para plataformas) ─────────────────────
expect val USE_FAKE_DATA: Boolean
expect val USE_OPEN_FOOTBALL: Boolean

@Composable
fun App() {
    val module = when {
        USE_FAKE_DATA     -> fakeAppModule
        USE_OPEN_FOOTBALL -> openFootballAppModule
        else              -> appModule
    }
    
    KoinApplication(application = { modules(module) }) {
        val matchRepository = koinInject<MatchRepository>()
        
        LaunchedEffect(Unit) {
            try {
                // Semeia os jogos iniciais (fake data no iOS, real no Android/Desktop)
                matchRepository.seedMatchesIfNeeded()
            } catch (e: Exception) {
                // Falha silenciosa - app continua funcionando
            }
        }

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
                    NavGraph()
                }
            }
        }
    }
}
