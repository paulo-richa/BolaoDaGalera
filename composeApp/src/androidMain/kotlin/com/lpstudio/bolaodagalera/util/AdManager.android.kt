package com.lpstudio.bolaodagalera.util

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.lpstudio.bolaodagalera.ADMOB_ANDROID_INTERSTITIAL_ID
import java.lang.ref.WeakReference

actual object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var activityRef: WeakReference<Activity>? = null

    fun init(activity: Activity) {
        activityRef = WeakReference(activity)
        loadInterstitial()
    }

    actual fun prepare() {
        // Já inicializado via init(activity) na MainActivity
        if (interstitialAd == null) {
            loadInterstitial()
        }
    }

    private fun loadInterstitial() {
        val activity = activityRef?.get() ?: return
        val adRequest = AdRequest.Builder().build()
        
        InterstitialAd.load(
            activity,
            ADMOB_ANDROID_INTERSTITIAL_ID,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            }
        )
    }

    actual fun showInterstitial() {
        val activity = activityRef?.get() ?: return
        val ad = interstitialAd
        
        if (ad != null) {
            ad.show(activity)
            interstitialAd = null // Consume the ad
            loadInterstitial() // Load the next one
        } else {
            loadInterstitial() // Try to load if not available
        }
    }
}
