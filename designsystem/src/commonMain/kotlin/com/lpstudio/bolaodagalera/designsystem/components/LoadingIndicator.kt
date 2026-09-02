package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.Neon

/**
 * Spinner padrão do app (cor Neon). Use direto pra um indicador inline, ou
 * [BolaoFullScreenLoading] pra centralizar numa tela inteira.
 */
@Composable
fun BolaoLoadingIndicator(modifier: Modifier = Modifier, color: Color = Neon, strokeWidth: androidx.compose.ui.unit.Dp = 3.dp) {
    CircularProgressIndicator(
        modifier = modifier,
        color = color,
        strokeWidth = strokeWidth
    )
}

@Composable
fun BolaoFullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BolaoLoadingIndicator()
    }
}

@Composable
fun BolaoLinearProgressIndicator(
    modifier: Modifier = Modifier,
    progress: (() -> Float)? = null,
    trackColor: Color = ProgressIndicatorDefaults.linearTrackColor
) {
    if (progress != null) {
        LinearProgressIndicator(progress = progress, modifier = modifier, color = Neon, trackColor = trackColor)
    } else {
        LinearProgressIndicator(modifier = modifier, color = Neon, trackColor = trackColor)
    }
}

@Preview
@Composable
private fun BolaoLoadingIndicatorPreview() {
    BolaoTheme {
        BolaoLoadingIndicator()
    }
}

@Preview
@Composable
private fun BolaoFullScreenLoadingPreview() {
    BolaoTheme {
        BolaoFullScreenLoading()
    }
}
