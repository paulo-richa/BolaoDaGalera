package com.lpstudio.bolaodagalera.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.components.BolaoBannerCard
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Banner
import kotlinx.coroutines.delay

private val CAROUSEL_HEIGHT = 140.dp
private const val AUTO_SCROLL_DELAY_MILLIS = 5000L

/**
 * Marketing carousel promoting the championships available for creating a
 * pool. Auto-scrolls every [AUTO_SCROLL_DELAY_MILLIS], looping back to the
 * first page after the last one.
 */
@Composable
fun BannerCarousel(banners: List<Banner>, onBannerClick: (Banner) -> Unit, modifier: Modifier = Modifier) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    if (banners.size > 1) {
        LaunchedEffect(pagerState, banners.size) {
            while (true) {
                delay(AUTO_SCROLL_DELAY_MILLIS)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }

    Column(modifier = modifier.padding(vertical = BolaoSpacing.sm)) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxWidth().height(CAROUSEL_HEIGHT),
            pageSpacing = BolaoSpacing.sm,
            contentPadding = PaddingValues(horizontal = BolaoSpacing.xl)
        ) { page ->
            val banner = banners[page]
            BolaoBannerCard(
                imageUrl = banner.imageUrl,
                onClick = { onBannerClick(banner) },
                title = banner.title,
                subtitle = banner.subtitle,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (banners.size > 1) {
            BannerPageIndicator(pageCount = banners.size, currentPage = pagerState.currentPage)
        }
    }
}

@Composable
private fun BannerPageIndicator(pageCount: Int, currentPage: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = BolaoSpacing.sm),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { index ->
            val isSelected = index == currentPage
            Box(
                modifier =
                Modifier
                    .padding(horizontal = BolaoSpacing.xs / 2)
                    .size(if (isSelected) 8.dp else 6.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) Neon else TextMuted.copy(alpha = 0.4f))
            )
        }
    }
}
