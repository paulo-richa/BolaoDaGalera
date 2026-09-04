package com.lpstudio.bolaodagalera.data.remote

import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlin.time.Duration.Companion.minutes
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RemoteConfigManager(private val crashReporter: CrashReporter) {
    private val logger = appLogger("RemoteConfigManager")
    private val remoteConfig = Firebase.remoteConfig

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode: StateFlow<Boolean> = _isMaintenanceMode.asStateFlow()

    private val _showAds = MutableStateFlow(true)
    val showAds: StateFlow<Boolean> = _showAds.asStateFlow()

    private val _maintenanceExemptEmails = MutableStateFlow(emptySet<String>())
    val maintenanceExemptEmails: StateFlow<Set<String>> = _maintenanceExemptEmails.asStateFlow()

    suspend fun fetchAndActivate() {
        try {
            remoteConfig.settings {
                minimumFetchInterval = 1.minutes
            }
            remoteConfig.setDefaults(
                "maintenance_mode" to false,
                "show_ads" to true,
                "maintenance_exempt_emails" to ""
            )
            remoteConfig.fetchAndActivate()
            _isMaintenanceMode.value = remoteConfig.getValue("maintenance_mode").asBoolean()
            _showAds.value = remoteConfig.getValue("show_ads").asBoolean()
            _maintenanceExemptEmails.value =
                remoteConfig
                    .getValue("maintenance_exempt_emails")
                    .asString()
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // On failure, keep the defaults
            crashReporter.recordException(e, "Erro ao buscar Remote Config")
            logger.w(e) { "Erro ao buscar Remote Config, mantendo defaults" }
        }
    }
}
