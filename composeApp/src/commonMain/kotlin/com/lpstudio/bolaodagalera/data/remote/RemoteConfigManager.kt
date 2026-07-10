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

    suspend fun fetchAndActivate() {
        try {
            remoteConfig.settings {
                minimumFetchInterval = 1.minutes
            }
            remoteConfig.setDefaults("maintenance_mode" to false)
            remoteConfig.fetchAndActivate()
            _isMaintenanceMode.value = remoteConfig.getValue("maintenance_mode").asBoolean()
        } catch (e: Exception) {
            // Se falhar, mantém o default (false)
        }
    }
}
