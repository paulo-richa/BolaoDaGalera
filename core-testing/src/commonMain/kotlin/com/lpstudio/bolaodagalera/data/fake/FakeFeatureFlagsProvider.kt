package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.featureflags.FeatureFlagsProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class FakeFeatureFlagsProvider : FeatureFlagsProvider {
    val forceShowNotificationBannerFlow = MutableStateFlow(false)
    override val forceShowNotificationBanner: StateFlow<Boolean> = forceShowNotificationBannerFlow

    val showBannerCarouselFlow = MutableStateFlow(true)
    override val showBannerCarousel: StateFlow<Boolean> = showBannerCarouselFlow

    val phaseAvailabilityStrictModeFlow = MutableStateFlow(false)
    override val phaseAvailabilityStrictMode: StateFlow<Boolean> = phaseAvailabilityStrictModeFlow
}
