package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

/**
 * Stepper input for a scoring rule value, shared between create and edit bolao screens.
 * All user-facing strings are passed pre-resolved so this composable stays resource-agnostic
 * and reusable across screens that reference different string resource sets.
 */
@Composable
fun ScoreInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    decreaseContentDescription: String,
    increaseContentDescription: String,
    pointSingularLabel: String,
    pointPluralLabel: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
        BolaoText(label, fontSize = BolaoTypography.bodyMedium.fontSize, color = TextMuted)
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolaoRadiusShape.md)
                .background(NavyCard)
                .border(1.dp, GlassBorder, BolaoRadiusShape.md)
                .padding(BolaoSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BolaoIconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                modifier = Modifier.size(36.dp)
            ) {
                BolaoText(
                    decreaseContentDescription,
                    color = Neon,
                    fontSize = BolaoTypography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BolaoText(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(4.dp))
                BolaoText(
                    text = if (value == 1) pointSingularLabel else pointPluralLabel,
                    color = TextMuted,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Medium
                )
            }

            BolaoIconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(36.dp)
            ) {
                BolaoText(
                    increaseContentDescription,
                    color = Neon,
                    fontSize = BolaoTypography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
