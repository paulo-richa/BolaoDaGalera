package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Escala de espaçamento do app (grid de 4dp, só números pares). Não é um
 * token oficial do Material 3 - o M3 não prescreve espaçamento -, mas é
 * prática recomendada nos apps de referência do Google (Now in Android,
 * Jetsnack, Reply) pra evitar `.dp` mágico espalhado pelas telas.
 */
object BolaoSpacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp
    val huge = 40.dp
}
