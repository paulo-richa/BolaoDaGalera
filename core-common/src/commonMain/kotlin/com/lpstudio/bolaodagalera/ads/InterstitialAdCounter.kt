package com.lpstudio.bolaodagalera.ads

/**
 * Abstracts the "show an interstitial ad every N actions" logic from the
 * actual Ads SDK implementation (which lives in :composeApp, since
 * AdManager depends on CocoaPods cinterop on iOS and only the module with
 * the native.cocoapods plugin can reference it).
 */
interface InterstitialAdCounter {
    fun incrementAndShowIfNecessary()
}
