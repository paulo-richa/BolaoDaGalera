package com.lpstudio.bolaodagalera.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.ADMOB_ANDROID_BANNER_ID
import com.lpstudio.bolaodagalera.ADMOB_IOS_BANNER_ID
import com.lpstudio.bolaodagalera.ads.AdBannerProvider
import com.lpstudio.bolaodagalera.getPlatform

class BolaoAdBannerProvider : AdBannerProvider {
    @Composable
    override fun Banner(modifier: Modifier) {
        val adId = if (getPlatform().name.lowercase().contains("android")) ADMOB_ANDROID_BANNER_ID else ADMOB_IOS_BANNER_ID
        AdBanner(modifier = modifier, adId = adId)
    }
}
