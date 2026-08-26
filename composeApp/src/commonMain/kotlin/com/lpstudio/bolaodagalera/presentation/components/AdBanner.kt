package com.lpstudio.bolaodagalera.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.LocalAdsEnabled

@Composable
fun AdBanner(
    modifier: Modifier = Modifier,
    adId: String,
) {
    if (LocalAdsEnabled.current) {
        AdBannerNative(modifier = modifier, adId = adId)
    }
}

@Composable
expect fun AdBannerNative(
    modifier: Modifier = Modifier,
    adId: String,
)
