package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.Neon

/**
 * Spinner padrão do app (cor Neon). Use direto pra um indicador inline, ou
 * [BolaoFullScreenLoading] pra centralizar numa tela inteira.
 */
@Composable
fun BolaoLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = Neon,
        strokeWidth = 3.dp
    )
}

@Composable
fun BolaoFullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BolaoLoadingIndicator()
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
