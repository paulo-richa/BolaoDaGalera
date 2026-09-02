package com.lpstudio.bolaodagalera.presentation.splash

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_core.generated.resources.Res
import bolaodagalera.feature_core.generated.resources.splash_screen_app_title
import bolaodagalera.feature_core.generated.resources.splash_screen_logo_emoji
import bolaodagalera.feature_core.generated.resources.splash_screen_studio_label
import bolaodagalera.feature_core.generated.resources.splash_screen_tagline
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLinearProgressIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.GradientPrimary
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec =
        spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Restored splash duration
        onSplashFinished()
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Centered logo
            Box(
                modifier =
                Modifier
                    .size(120.dp)
                    .scale(scale)
                    .drawBehind {
                        drawCircle(
                            brush =
                            Brush.radialGradient(
                                colors = listOf(Neon.copy(alpha = 0.2f), Color.Transparent)
                            ),
                            radius = size.maxDimension * 0.8f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(GradientPrimary)
                        .border(2.dp, Neon.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    BolaoText(stringResource(Res.string.splash_screen_logo_emoji), fontSize = BolaoTypography.displayLarge.fontSize)
                }
            }

            Spacer(Modifier.height(24.dp))

            BolaoText(
                stringResource(Res.string.splash_screen_app_title),
                fontSize = BolaoTypography.displayMedium.fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )

            BolaoText(
                stringResource(Res.string.splash_screen_tagline),
                fontSize = BolaoTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = Neon,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Thin Neon Loading Bar
            Box(modifier = Modifier.width(180.dp)) {
                BolaoLinearProgressIndicator(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape),
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        BolaoText(
            stringResource(Res.string.splash_screen_studio_label),
            modifier =
            Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = BolaoSpacing.xxxl),
            fontSize = BolaoTypography.bodySmall.fontSize,
            color = TextSubtle,
            letterSpacing = 1.sp
        )
    }
}
