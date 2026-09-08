package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard

/**
 * A clickable card with a background image (loaded via Coil3), used for the
 * marketing banner carousel on the Home screen. If [title]/[subtitle] are
 * given, they're overlaid on a dark gradient at the bottom for legibility
 * over any image.
 */
@Composable
fun BolaoBannerCard(imageUrl: String, onClick: () -> Unit, modifier: Modifier = Modifier, title: String? = null, subtitle: String? = null) {
    // No border/frame drawn here - the source banner images already ship with
    // their own border baked into the artwork, and overlaying a second one
    // (often slightly misaligned after Crop) reads as a visual glitch.
    Box(
        modifier =
        modifier
            .clip(BolaoRadiusShape.lg)
            .background(NavyCard)
            .clickable(onClick = onClick)
    ) {
        SubcomposeAsyncImage(
            model =
            ImageRequest.Builder(LocalPlatformContext.current)
                .data(imageUrl)
                .decoderFactory(SvgDecoder.Factory())
                .crossfade(true)
                .build(),
            contentDescription = title,
            // FillBounds (not Crop): the source banners already ship as a
            // complete design with their own border baked into the edges -
            // Crop would shave that border unevenly whenever the card's
            // aspect ratio doesn't exactly match the image's.
            contentScale = ContentScale.FillBounds,
            modifier = Modifier.fillMaxSize(),
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { BolaoLoadingIndicator() } }
        )

        if (title != null || subtitle != null) {
            Column(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomStart)
                    .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))))
                    .padding(BolaoSpacing.lg)
            ) {
                if (title != null) {
                    BolaoText(title, color = Color.White, fontSize = BolaoTypography.titleLarge.fontSize, fontWeight = FontWeight.Bold)
                }
                if (subtitle != null) {
                    BolaoText(subtitle, color = Color.White.copy(alpha = 0.85f), fontSize = BolaoTypography.bodyMedium.fontSize)
                }
            }
        }
    }
}
