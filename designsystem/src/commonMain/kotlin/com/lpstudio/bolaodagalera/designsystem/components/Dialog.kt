package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoElevation
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

/**
 * Standard confirmation dialog (title + message + confirm/cancel), for the
 * repeated "are you sure you want to...?" pattern across the app's screens.
 * [isDestructive] paints the confirm button red (e.g. delete actions).
 */
@Composable
fun BolaoConfirmDialog(
    title: String,
    message: String,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "Cancelar",
    isDestructive: Boolean = false,
    tonalElevation: Dp = BolaoElevation.level3
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = tonalElevation,
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

/**
 * Generic dialog with customizable slots, for cases that don't fit the
 * confirm/cancel shape of [BolaoConfirmDialog] (e.g. a success dialog with
 * an emoji, or an embedded form).
 */
@Composable
fun BolaoDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    dismissButton: (@Composable () -> Unit)? = null,
    title: (@Composable () -> Unit)? = null,
    text: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    tonalElevation: Dp = BolaoElevation.level3
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = confirmButton,
        modifier = modifier,
        dismissButton = dismissButton,
        title = title,
        text = text,
        containerColor = containerColor,
        tonalElevation = tonalElevation
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
