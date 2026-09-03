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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
import com.lpstudio.bolaodagalera.di.appModule
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.presentation.maintenance.MaintenanceScreen
import com.lpstudio.bolaodagalera.presentation.navigation.NavGraph
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.util.AdManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.koin.compose.KoinApplication
import org.koin.compose.koinInject

private val EXEMPT_MAINTENANCE_EMAILS = setOf("paulo.richa@hotmail.com", "pedro-richa@hotmail.com")

private fun computeShouldShowMaintenance(currentUserEmail: String?, isMaintenanceMode: Boolean): Boolean =
    currentUserEmail != null && isMaintenanceMode && currentUserEmail !in EXEMPT_MAINTENANCE_EMAILS

@Composable
private fun AppSideEffects(
    showAds: Boolean,
    currentUser: User?,
    remoteConfigManager: RemoteConfigManager,
    championshipRepository: ChampionshipRepository,
    crashReporter: CrashReporter,
    analyticsTracker: AnalyticsTracker
) {
    // Update the global ads state for AdManager (interstitials)
    LaunchedEffect(showAds) {
        AdManager.setEnabled(showAds)
    }

    // Preload interstitial ads if enabled
    LaunchedEffect(Unit) {
        AdManager.prepare()
    }

    LaunchedEffect(currentUser) {
        crashReporter.setUserId(currentUser?.id)
        analyticsTracker.setUserId(currentUser?.id)
    }

    LaunchedEffect(currentUser) {
        try {
            // Remote Config can be fetched without login
            remoteConfigManager.fetchAndActivate()

            // Firestore operations MUST wait for login to avoid PERMISSION_DENIED
            if (currentUser != null) {
                // Start loading championships and keep the cache up to date
                championshipRepository.refreshCache()
                championshipRepository.getChampionships().collect { }
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao carregar Remote Config/campeonatos no App startup")
        }
    }
}

@Composable
fun App() {
    KoinApplication(application = { modules(appModule) }) {
        val remoteConfigManager = koinInject<RemoteConfigManager>()
        val authRepository = koinInject<AuthRepository>()
        val championshipRepository = koinInject<ChampionshipRepository>()
        val crashReporter = koinInject<CrashReporter>()
        val analyticsTracker = koinInject<AnalyticsTracker>()

        val showAds by remoteConfigManager.showAds.collectAsState()
        val scope = rememberCoroutineScope()
        val isMaintenanceMode by remoteConfigManager.isMaintenanceMode.collectAsState()
        val currentUser by authRepository.authStateFlow.collectAsState(initial = authRepository.currentUser)
        val shouldShowMaintenance = computeShouldShowMaintenance(currentUser?.email, isMaintenanceMode)

        AppSideEffects(
            showAds = showAds,
            currentUser = currentUser,
            remoteConfigManager = remoteConfigManager,
            championshipRepository = championshipRepository,
            crashReporter = crashReporter,
            analyticsTracker = analyticsTracker
        )

        AppTheme {
            CompositionLocalProvider(localAdsEnabled provides showAds) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(DeepNavy)
                        // Resolve overlap across all screens
                        .navigationBarsPadding()
                ) {
                    // Solid background for the status bar to make icons stand out
                    Spacer(
                        Modifier
                            .fillMaxWidth()
                            .windowInsetsTopHeight(WindowInsets.statusBars)
                            .background(DeepNavy)
                    )
                    Box(Modifier.weight(1f)) {
                        if (shouldShowMaintenance) {
                            MaintenanceScreen(
                                onLogout = {
                                    scope.launch { authRepository.signOut() }
                                }
                            )
                        } else {
                            NavGraph()
                        }
                    }
                }
            }
        }
    }
}

val localAdsEnabled = staticCompositionLocalOf { true }
