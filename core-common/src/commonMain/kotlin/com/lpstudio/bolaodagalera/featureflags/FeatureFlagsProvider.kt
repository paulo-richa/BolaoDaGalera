package com.lpstudio.bolaodagalera.featureflags

import kotlinx.coroutines.flow.StateFlow

/**
 * Abstracts Remote Config feature flags from the module that owns Remote
 * Config access (:composeApp, via RemoteConfigManager in :core-data) - the
 * same reasoning as AdBannerProvider, so feature modules can read flags
 * without depending on :core-data.
 */
interface FeatureFlagsProvider {
    /**
     * Forces the Home notifications-disabled banner to show regardless of
     * the actual OS permission state - useful to preview/QA it without
     * disabling notifications on a real device.
     */
    val forceShowNotificationBanner: StateFlow<Boolean>

    /**
     * Turns the Home marketing banner carousel on/off entirely - carousel
     * content (images, order) lives in Firestore, not here.
     */
    val showBannerCarousel: StateFlow<Boolean>
}
