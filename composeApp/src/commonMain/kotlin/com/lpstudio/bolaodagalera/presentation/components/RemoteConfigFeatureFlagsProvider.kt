package com.lpstudio.bolaodagalera.presentation.components

import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
import com.lpstudio.bolaodagalera.featureflags.FeatureFlagsProvider
import kotlinx.coroutines.flow.StateFlow

class RemoteConfigFeatureFlagsProvider(private val remoteConfigManager: RemoteConfigManager) : FeatureFlagsProvider {
    override val forceShowNotificationBanner: StateFlow<Boolean> = remoteConfigManager.forceShowNotificationBanner
    override val showBannerCarousel: StateFlow<Boolean> = remoteConfigManager.showBannerCarousel
}
