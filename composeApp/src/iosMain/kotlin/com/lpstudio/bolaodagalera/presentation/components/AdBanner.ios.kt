package com.lpstudio.bolaodagalera.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import platform.UIKit.UIView

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AdBanner(modifier: Modifier, adId: String) {
    UIKitView(
        factory = {
            val banner = cocoapods.Google_Mobile_Ads_SDK.GADBannerView()
            banner.adUnitID = adId
            banner.rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
            banner.loadRequest(cocoapods.Google_Mobile_Ads_SDK.GADRequest())
            banner as UIView
        },
        modifier = modifier,
        update = { _ -> }
    )
}
