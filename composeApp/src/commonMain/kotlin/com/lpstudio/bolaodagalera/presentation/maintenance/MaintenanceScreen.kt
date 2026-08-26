package com.lpstudio.bolaodagalera.presentation.maintenance

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material3.*
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
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.Neon

@Composable
fun MaintenanceScreen(onLogout: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition()

    // Animação de Rotação (Bola girando)
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart,
            ),
    )

    // Animação de Pulo (Quique da bola)
    val bounceTranslation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -100f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    // Animação da Sombra (Aumenta/Diminui com o pulo)
    val shadowScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    val shadowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
    )

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(DeepNavy)
                .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.height(200.dp),
            contentAlignment = Alignment.BottomCenter,
        ) {
            // Sombra Neon no chão
            Box(
                modifier =
                    Modifier
                        .size(width = 80.dp, height = 20.dp)
                        .graphicsLayer(
                            scaleX = shadowScale,
                            scaleY = shadowScale,
                            alpha = shadowAlpha,
                        )
                        .background(Neon.copy(alpha = 0.6f), CircleShape)
                        .blur(10.dp),
            )

            // Bola de Futebol
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = "Manutenção",
                modifier =
                    Modifier
                        .size(80.dp)
                        .offset(y = bounceTranslation.dp)
                        .graphicsLayer(rotationZ = rotation),
                tint = Neon,
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Intervalo para Ajustes!",
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Nossa equipe entrou em campo para fazer alguns ajustes técnicos. Voltamos logo após o comercial!",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = onLogout) {
            Text(
                "Sair da conta",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}
