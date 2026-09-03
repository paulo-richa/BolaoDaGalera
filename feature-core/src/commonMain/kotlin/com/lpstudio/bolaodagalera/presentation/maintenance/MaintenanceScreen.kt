package com.lpstudio.bolaodagalera.presentation.maintenance

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_core.generated.resources.Res
import bolaodagalera.feature_core.generated.resources.maintenance_screen_button_logout
import bolaodagalera.feature_core.generated.resources.maintenance_screen_icon_cd
import bolaodagalera.feature_core.generated.resources.maintenance_screen_message
import bolaodagalera.feature_core.generated.resources.maintenance_screen_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import org.jetbrains.compose.resources.stringResource

@Composable
private fun BouncingBall() {
    val infiniteTransition = rememberInfiniteTransition()

    // Rotation animation (spinning ball)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(animation = tween(2000, easing = LinearEasing), repeatMode = RepeatMode.Restart)
    )

    // Bounce animation (ball hop)
    val bounceTranslation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
    )

    // Shadow animation (grows/shrinks in sync with the bounce)
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse)
    )

    Box(modifier = Modifier.height(200.dp), contentAlignment = Alignment.BottomCenter) {
        // Neon shadow on the ground
        Box(
            modifier =
            Modifier
                .size(width = 80.dp, height = 20.dp)
                .graphicsLayer(scaleX = shadowScale, scaleY = shadowScale, alpha = shadowAlpha)
                .background(Neon.copy(alpha = 0.6f), CircleShape)
                .blur(10.dp)
        )

        // Soccer ball
        BolaoIcon(
            imageVector = Icons.Default.SportsSoccer,
            contentDescription = stringResource(Res.string.maintenance_screen_icon_cd),
            modifier = Modifier.size(80.dp).offset(y = bounceTranslation.dp).graphicsLayer(rotationZ = rotation),
            tint = Neon
        )
    }
}

@Composable
private fun MaintenanceMessage() {
    BolaoText(
        text = stringResource(Res.string.maintenance_screen_title),
        color = Color.White,
        fontSize = BolaoTypography.displaySmall.fontSize,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    BolaoText(
        text = stringResource(Res.string.maintenance_screen_message),
        color = Color.White.copy(alpha = 0.7f),
        fontSize = BolaoTypography.titleLarge.fontSize,
        textAlign = TextAlign.Center,
        lineHeight = 24.sp,
        modifier = Modifier.padding(horizontal = BolaoSpacing.lg)
    )
}

@Composable
fun MaintenanceScreen(onLogout: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().background(DeepNavy).padding(BolaoSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        BouncingBall()

        Spacer(modifier = Modifier.height(40.dp))

        MaintenanceMessage()

        Spacer(modifier = Modifier.height(48.dp))

        BolaoTextButton(onClick = onLogout) {
            BolaoText(
                stringResource(Res.string.maintenance_screen_button_logout),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = BolaoTypography.bodyLarge.fontSize,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
