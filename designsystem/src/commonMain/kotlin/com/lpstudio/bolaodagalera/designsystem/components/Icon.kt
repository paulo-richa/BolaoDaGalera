package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme

@Composable
fun BolaoIcon(
    imageVector: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(imageVector = imageVector, contentDescription = contentDescription, modifier = modifier, tint = tint)
}

@Composable
fun BolaoIcon(painter: Painter, contentDescription: String?, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(painter = painter, contentDescription = contentDescription, modifier = modifier, tint = tint)
}

@Composable
fun BolaoIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource? = null,
    content: @Composable () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

@Preview
@Composable
private fun BolaoIconButtonPreview() {
    BolaoTheme {
        BolaoIconButton(onClick = {}) {
            BolaoIcon(imageVector = Icons.Default.Delete, contentDescription = "Excluir")
        }
    }
}
