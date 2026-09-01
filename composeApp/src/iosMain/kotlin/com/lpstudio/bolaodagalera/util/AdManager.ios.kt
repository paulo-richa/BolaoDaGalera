package com.lpstudio.bolaodagalera.util

import cocoapods.Google_Mobile_Ads_SDK.GADInterstitialAd
import cocoapods.Google_Mobile_Ads_SDK.GADRequest
import com.lpstudio.bolaodagalera.ADMOB_IOS_INTERSTITIAL_ID
import com.lpstudio.bolaodagalera.observability.appLogger
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow

@OptIn(ExperimentalForeignApi::class)
actual object AdManager {
    private val logger = appLogger("AdManager")
    private var interstitial: GADInterstitialAd? = null
    private var isLoading = false
    private var isEnabled = true

    actual fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        if (!enabled) {
            interstitial = null
        }
    }

    init {
        logger.d { "AdManager: Initializing" }
    }

    actual fun prepare() {
        logger.d { "AdManager: Prepare called - pre-loading interstitial" }
        loadInterstitial()
    }

    private fun loadInterstitial() {
        if (!isEnabled || isLoading || interstitial != null) return

        isLoading = true
        logger.d { "AdManager: Starting to load interstitial with ID: $ADMOB_IOS_INTERSTITIAL_ID" }

        GADInterstitialAd.loadWithAdUnitID(
            adUnitID = ADMOB_IOS_INTERSTITIAL_ID,
            request = GADRequest(),
            completionHandler = { ad, error ->
                isLoading = false
                if (error == null) {
                    logger.d { "AdManager: Interstitial loaded successfully" }
                    interstitial = ad
                } else {
                    logger.d { "AdManager: Failed to load interstitial: ${error.localizedDescription}" }
                }
            }
        )
    }

    private fun getRootViewController(): UIViewController? {
        // Tenta pegar a window ativa de forma mais robusta para SwiftUI
        val window =
            UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
                ?: UIApplication.sharedApplication.keyWindow

        return window?.rootViewController
    }

    actual fun showInterstitial() {
        if (!isEnabled) return
        val rootViewController = getRootViewController()
        val ad = interstitial

        logger.d { "AdManager: Attempting to show interstitial. Ad present: ${ad != null}, VC present: ${rootViewController != null}" }

        if (ad != null && rootViewController != null) {
            ad.presentFromRootViewController(rootViewController)
            interstitial = null
            logger.d { "AdManager: Interstitial presented" }
            loadInterstitial()
        } else {
            if (ad == null) logger.d { "AdManager: Ad was null, triggering new load" }
            if (rootViewController == null) logger.d { "AdManager: RootViewController was null" }
            loadInterstitial()
        }
    }
}
