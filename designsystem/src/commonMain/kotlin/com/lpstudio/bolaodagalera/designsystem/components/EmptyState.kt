package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

/**
 * Estado vazio padrão (emoji/ícone + título + subtítulo opcional + ação
 * opcional), pro caso comum de "lista sem itens ainda" nas telas do app.
 */
@Composable
fun BolaoEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    subtitle: String? = null,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(BolaoSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)
    ) {
        if (emoji != null) {
            Text(emoji, style = BolaoTypography.displayLarge)
        }
        Text(
            title,
            color = Color.White,
            style = BolaoTypography.titleLarge,
            textAlign = TextAlign.Center
        )
        if (subtitle != null) {
            Text(
                subtitle,
                color = TextMuted,
                style = BolaoTypography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        action?.invoke()
    }
}

@Preview
@Composable
private fun BolaoEmptyStatePreview() {
    BolaoTheme {
        BolaoEmptyState(
            emoji = "🔍",
            title = "Nenhum bolão encontrado",
            subtitle = "Crie um novo bolão ou entre com um código de convite."
        )
    }
}

@Preview
@Composable
private fun BolaoEmptyStateWithActionPreview() {
    BolaoTheme {
        BolaoEmptyState(
            emoji = "📭",
            title = "Nenhum convite pendente",
            subtitle = "Quando alguém te convidar, o convite aparece aqui.",
            action = { Text("Atualizar", color = MaterialTheme.colorScheme.primary) }
        )
    }
}
