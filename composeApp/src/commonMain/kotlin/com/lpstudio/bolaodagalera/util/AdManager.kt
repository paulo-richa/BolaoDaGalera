package com.lpstudio.bolaodagalera.util

expect object AdManager {
    fun setEnabled(enabled: Boolean)

    fun prepare()

    fun showInterstitial()
}

object PredictionAdCounter {
    private var count = 0

    fun incrementAndShowIfNecessary() {
        count++
        if (count >= 3) {
            AdManager.showInterstitial()
            count = 0
        }
    }
}
