package com.lpstudio.bolaodagalera.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Abstrai a renderização do banner de anúncios da implementação real do SDK
 * (que fica em :composeApp, pelo mesmo motivo do [InterstitialAdCounter]:
 * o AdBanner real depende de bindings de Ads específicos de plataforma só
 * resolvíveis no módulo com o plugin native.cocoapods no iOS).
 */
interface AdBannerProvider {
    @Composable
    fun Banner(modifier: Modifier)
}
