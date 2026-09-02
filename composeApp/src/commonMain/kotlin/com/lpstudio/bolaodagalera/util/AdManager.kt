package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter

expect object AdManager {
    fun setEnabled(enabled: Boolean)

    fun prepare()

    fun showInterstitial()
}

object PredictionAdCounter : InterstitialAdCounter {
    private var count = 0

    override fun incrementAndShowIfNecessary() {
        count++
        if (count >= 3) {
            AdManager.showInterstitial()
            count = 0
        }
    }
}
