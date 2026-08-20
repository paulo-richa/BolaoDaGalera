package com.lpstudio.bolaodagalera.data.remote

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.time.Duration.Companion.minutes

class RemoteConfigManager {

    private val remoteConfig = Firebase.remoteConfig

    private val _isMaintenanceMode = MutableStateFlow(false)
    val isMaintenanceMode: StateFlow<Boolean> = _isMaintenanceMode.asStateFlow()

    private val _showAds = MutableStateFlow(true)
    val showAds: StateFlow<Boolean> = _showAds.asStateFlow()

    suspend fun fetchAndActivate() {
        try {
            remoteConfig.settings {
                minimumFetchInterval = 1.minutes
            }
            remoteConfig.setDefaults(
                "maintenance_mode" to false,
                "show_ads" to true
            )
            remoteConfig.fetchAndActivate()
            _isMaintenanceMode.value = remoteConfig.getValue("maintenance_mode").asBoolean()
            _showAds.value = remoteConfig.getValue("show_ads").asBoolean()
        } catch (e: Exception) {
            // Se falhar, mantém os defaults
        }
    }
}
