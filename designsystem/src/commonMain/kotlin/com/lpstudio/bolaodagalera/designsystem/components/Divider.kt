package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BolaoHorizontalDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = MaterialTheme.colorScheme.outlineVariant) {
    HorizontalDivider(modifier = modifier, thickness = thickness, color = color)
}

@Composable
fun BolaoVerticalDivider(modifier: Modifier = Modifier, thickness: Dp = 1.dp, color: Color = MaterialTheme.colorScheme.outlineVariant) {
    VerticalDivider(modifier = modifier, thickness = thickness, color = color)
}
