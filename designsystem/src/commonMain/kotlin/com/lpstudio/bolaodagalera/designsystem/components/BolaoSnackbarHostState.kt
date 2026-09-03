package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.Neon

/**
 * Wraps [SnackbarHostState] so screens outside :designsystem don't need to
 * depend on material3 just to hold this reference.
 */
class BolaoSnackbarHostState internal constructor(internal val delegate: SnackbarHostState) {
    suspend fun showSnackbar(message: String) {
        delegate.showSnackbar(message)
    }
}

@Composable
fun rememberBolaoSnackbarHostState(): BolaoSnackbarHostState {
    val delegate = remember { SnackbarHostState() }
    return remember(delegate) { BolaoSnackbarHostState(delegate) }
}

@Composable
fun BolaoSnackbarHost(hostState: BolaoSnackbarHostState, modifier: Modifier = Modifier) {
    SnackbarHost(hostState = hostState.delegate, modifier = modifier) { data ->
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
        shape = BolaoRadiusShape.md
    )
}
