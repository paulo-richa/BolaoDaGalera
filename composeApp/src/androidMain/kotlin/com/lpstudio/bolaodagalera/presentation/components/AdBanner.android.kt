package com.lpstudio.bolaodagalera.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@Composable
actual fun AdBannerNative(modifier: Modifier, adId: String) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.BANNER)
                setAdUnitId(adId)
                loadAd(AdRequest.Builder().build())
            }
        },
        update = { adView ->
            // Updates if needed
        }
    )
}
