package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import bolaodagalera.designsystem.generated.resources.Res
import bolaodagalera.designsystem.generated.resources.app_bar_navigate_back
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import org.jetbrains.compose.resources.stringResource

/**
 * Standard app top bar: transparent background, white text/icons, no
 * elevation - the same pattern currently repeated (with minor variations)
 * on nearly every screen with a Scaffold + TopAppBar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolaoTopBar(title: String, onNavigateBack: (() -> Unit)? = null, actions: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(Res.string.app_bar_navigate_back),
                        tint = Color.White
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        windowInsets = WindowInsets(top = 0.dp)
    )
}

@Preview
@Composable
private fun BolaoTopBarPreview() {
    BolaoTheme {
        BolaoTopBar(title = "Configurações", onNavigateBack = {})
    }
}
