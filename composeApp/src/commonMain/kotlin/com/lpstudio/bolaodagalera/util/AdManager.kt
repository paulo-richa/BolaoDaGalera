package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter

expect object AdManager {
    fun setEnabled(enabled: Boolean)

    fun prepare()

    fun showInterstitial()
}

object PredictionAdCounter : InterstitialAdCounter {
    private const val PREDICTIONS_BEFORE_INTERSTITIAL = 3

    private var count = 0

    override fun incrementAndShowIfNecessary() {
        count++
        if (count >= PREDICTIONS_BEFORE_INTERSTITIAL) {
            AdManager.showInterstitial()
            count = 0
        }
    }
}
