package com.lpstudio.bolaodagalera.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.localAdsEnabled

@Composable
fun AdBanner(modifier: Modifier = Modifier, adId: String) {
    if (localAdsEnabled.current) {
        AdBannerNative(modifier = modifier, adId = adId)
    }
}

@Composable
expect fun AdBannerNative(modifier: Modifier = Modifier, adId: String)
