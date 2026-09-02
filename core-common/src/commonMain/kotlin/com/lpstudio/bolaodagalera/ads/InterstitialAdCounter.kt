package com.lpstudio.bolaodagalera.ads

/**
 * Abstrai a lógica de "mostrar anúncio intersticial a cada N ações" da
 * implementação real do SDK de Ads (que fica em :composeApp, já que o
 * AdManager depende de cinterop do CocoaPods no iOS e só o módulo com o
 * plugin native.cocoapods consegue referenciá-lo).
 */
interface InterstitialAdCounter {
    fun incrementAndShowIfNecessary()
}
