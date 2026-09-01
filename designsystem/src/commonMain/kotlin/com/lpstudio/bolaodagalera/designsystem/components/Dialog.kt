package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

/**
 * Diálogo de confirmação padrão (título + mensagem + confirmar/cancelar),
 * pro padrão repetido de "tem certeza que deseja...?" nas telas do app.
 * [isDestructive] pinta o botão de confirmação em vermelho (ex: excluir).
 */
@Composable
fun BolaoConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancelar",
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        title = { Text(title, color = Color.White, fontWeight = FontWeight.Bold) },
        text = { Text(message, color = TextMuted) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmText,
                    color = if (isDestructive) ErrorRed else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(dismissText, color = TextMuted)
            }
        }
    )
}

@Preview
@Composable
private fun BolaoConfirmDialogPreview() {
    BolaoTheme {
        BolaoConfirmDialog(
            title = "Excluir Bolão?",
            message = "Esta ação não pode ser desfeita. Todos os participantes e palpites serão removidos.",
            confirmText = "Excluir",
            isDestructive = true,
            onConfirm = {},
            onDismiss = {}
        )
    }
}
