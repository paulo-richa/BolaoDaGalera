package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.GlassWhite
import com.lpstudio.bolaodagalera.designsystem.theme.GradientBg

/**
 * Card padrão do app: cantos arredondados (MaterialTheme.shapes.large,
 * 16.dp - o valor já usado na maioria dos cards hoje) e fundo
 * surfaceVariant. Substitui o padrão hand-rolled
 * `Modifier.clip(RoundedCornerShape(16.dp)).background(NavyCard)`
 * repetido em várias telas.
 */
@Composable
fun BolaoCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, GlassBorder, MaterialTheme.shapes.large)
            .padding(16.dp),
        content = content
    )
}

@Preview
@Composable
private fun BolaoCardPreview() {
    BolaoTheme {
        BolaoCard {
            Text("Título do card", color = Color.White)
            Text("Conteúdo de exemplo dentro do card.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/**
 * Card translúcido ("glass"), usado nas telas de autenticação sobre o
 * GradientBg (login/cadastro). Cantos mais arredondados (20.dp) e fundo
 * semi-transparente - visual distinto do [BolaoCard] opaco.
 */
@Composable
fun BolaoGlassCard(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier =
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(GlassWhite)
            .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
            .padding(24.dp),
        verticalArrangement = verticalArrangement,
        content = content
    )
}

@Preview
@Composable
private fun BolaoGlassCardPreview() {
    BolaoTheme {
        Column(modifier = Modifier.background(GradientBg).padding(16.dp)) {
            BolaoGlassCard {
                Text("Título do card", color = Color.White)
                Text("Conteúdo de exemplo dentro do card.", color = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}
