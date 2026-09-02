package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter

class FakeInterstitialAdCounter : InterstitialAdCounter {
    var incrementCount = 0
        private set

    override fun incrementAndShowIfNecessary() {
        incrementCount++
    }
}
