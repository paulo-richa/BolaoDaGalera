package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.Neon

typealias BolaoSnackbarHostState = SnackbarHostState

@Composable
fun rememberBolaoSnackbarHostState(): BolaoSnackbarHostState = remember { SnackbarHostState() }

@Composable
fun BolaoSnackbarHost(hostState: SnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState, modifier = modifier) { data ->
        BolaoSnackbar(data)
    }
}

@Composable
fun BolaoSnackbar(data: SnackbarData, modifier: Modifier = Modifier) {
    Snackbar(
        snackbarData = data,
        modifier = modifier,
        containerColor = Neon,
        contentColor = DeepNavy,
        shape = RoundedCornerShape(12.dp)
    )
}
