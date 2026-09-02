package com.lpstudio.bolaodagalera.data.fake

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.ads.AdBannerProvider

class FakeAdBannerProvider : AdBannerProvider {
    @Composable
    override fun Banner(modifier: Modifier) = Unit
}
