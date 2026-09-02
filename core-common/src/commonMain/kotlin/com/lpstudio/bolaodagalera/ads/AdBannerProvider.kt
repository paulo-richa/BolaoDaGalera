package com.lpstudio.bolaodagalera.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Abstracts ad banner rendering from the actual SDK implementation
 * (which lives in :composeApp, for the same reason as [InterstitialAdCounter]:
 * the real AdBanner depends on platform-specific Ads bindings that can only
 * be resolved in the module with the native.cocoapods plugin on iOS).
 */
interface AdBannerProvider {
    @Composable
    fun Banner(modifier: Modifier)
}
