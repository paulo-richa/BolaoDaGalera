package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

/**
 * Small pill for status/tags (e.g. "Pending", "Live", phase name).
 * Replaces the various hand-rolled badges currently scattered across screens.
 */
@Composable
fun BolaoChip(text: String, modifier: Modifier = Modifier, containerColor: Color = NavyElevated, contentColor: Color = TextMuted) {
    Text(
        text = text,
        color = contentColor,
        style = BolaoTypography.labelMedium,
        modifier =
        modifier
            .clip(CircleShape)
            .background(containerColor)
            .padding(horizontal = BolaoSpacing.sm, vertical = BolaoSpacing.xs)
    )
}

@Preview
@Composable
private fun BolaoChipPreview() {
    BolaoTheme {
        BolaoChip(text = "Pendente")
    }
}

@Preview
@Composable
private fun BolaoChipSuccessPreview() {
    BolaoTheme {
        BolaoChip(text = "Ao vivo", containerColor = Neon.copy(alpha = 0.2f), contentColor = Neon)
    }
}

@Preview
@Composable
private fun BolaoChipGoldPreview() {
    BolaoTheme {
        BolaoChip(text = "1º lugar", containerColor = Gold.copy(alpha = 0.2f), contentColor = Gold)
    }
}

@Preview
@Composable
private fun BolaoChipErrorPreview() {
    BolaoTheme {
        BolaoChip(text = "Encerrado", containerColor = ErrorRed.copy(alpha = 0.2f), contentColor = ErrorRed)
    }
}
