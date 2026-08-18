package com.lpstudio.bolaodagalera.util

import cocoapods.Google_Mobile_Ads_SDK.*
import com.lpstudio.bolaodagalera.ADMOB_IOS_INTERSTITIAL_ID
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIWindow
import platform.UIKit.UIViewController

@OptIn(ExperimentalForeignApi::class)
actual object AdManager {
    private var interstitial: GADInterstitialAd? = null
    private var isLoading = false

    init {
        println("AdManager: Initializing")
    }

    actual fun prepare() {
        println("AdManager: Prepare called - pre-loading interstitial")
        loadInterstitial()
    }

    private fun loadInterstitial() {
        if (isLoading || interstitial != null) return
        
        isLoading = true
        println("AdManager: Starting to load interstitial with ID: $ADMOB_IOS_INTERSTITIAL_ID")
        
        GADInterstitialAd.loadWithAdUnitID(
            adUnitID = ADMOB_IOS_INTERSTITIAL_ID,
            request = GADRequest(),
            completionHandler = { ad, error ->
                isLoading = false
                if (error == null) {
                    println("AdManager: Interstitial loaded successfully")
                    interstitial = ad
                } else {
                    println("AdManager: Failed to load interstitial: ${error.localizedDescription}")
                }
            }
        )
    }

    private fun getRootViewController(): UIViewController? {
        // Tenta pegar a window ativa de forma mais robusta para SwiftUI
        val window = UIApplication.sharedApplication.windows.filterIsInstance<UIWindow>().firstOrNull { it.isKeyWindow() }
            ?: UIApplication.sharedApplication.keyWindow
        
        return window?.rootViewController
    }

    actual fun showInterstitial() {
        val rootViewController = getRootViewController()
        val ad = interstitial
        
        println("AdManager: Attempting to show interstitial. Ad present: ${ad != null}, VC present: ${rootViewController != null}")
        
        if (ad != null && rootViewController != null) {
            ad.presentFromRootViewController(rootViewController)
            interstitial = null
            println("AdManager: Interstitial presented")
            loadInterstitial()
        } else {
            if (ad == null) println("AdManager: Ad was null, triggering new load")
            if (rootViewController == null) println("AdManager: RootViewController was null")
            loadInterstitial()
        }
    }
}
