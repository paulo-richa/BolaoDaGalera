package com.lpstudio.bolaodagalera.presentation.splash

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
import com.lpstudio.bolaodagalera.presentation.theme.*
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(2500) // Tempo da Splash restaurado
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo Central
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(Neon.copy(alpha = 0.2f), Color.Transparent),
                            ),
                            radius = size.maxDimension * 0.8f
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(GradientPrimary)
                        .border(2.dp, Neon.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚽", fontSize = 60.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                "BOLÃO DA GALERA",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp
            )
            
            Text(
                "CRIE SEU TIME, DÊ SEU PALPITE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Neon,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(48.dp))

            // Thin Neon Loading Bar
            Box(modifier = Modifier.width(180.dp)) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .clip(CircleShape),
                    color = Neon,
                    trackColor = Color.White.copy(alpha = 0.1f)
                )
            }
        }
        
        Text(
            "LP STUDIO",
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            fontSize = 10.sp,
            color = TextSubtle,
            letterSpacing = 1.sp
        )
    }
}
