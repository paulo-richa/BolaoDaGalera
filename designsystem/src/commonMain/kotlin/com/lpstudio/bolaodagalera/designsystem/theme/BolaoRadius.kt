package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * Escala de raio de canto do app (só números pares). O Material 3 só define
 * 5 papéis oficiais (extraSmall...extraLarge, ver [BolaoShapes]), mas o app
 * usa raios intermediários (ex: 20dp) com bastante frequência - esta escala
 * cobre esses casos sem forçar tudo nos 5 papéis do M3.
 */
object BolaoRadius {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 20.dp
    val xxl = 28.dp
}

object BolaoRadiusShape {
    val xs = RoundedCornerShape(BolaoRadius.xs)
    val sm = RoundedCornerShape(BolaoRadius.sm)
    val md = RoundedCornerShape(BolaoRadius.md)
    val lg = RoundedCornerShape(BolaoRadius.lg)
    val xl = RoundedCornerShape(BolaoRadius.xl)
    val xxl = RoundedCornerShape(BolaoRadius.xxl)
}
