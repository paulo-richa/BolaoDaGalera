package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/**
 * App corner-radius scale (even numbers only). Material 3 only defines
 * 5 official roles (extraSmall...extraLarge, see [BolaoShapes]), but the app
 * uses intermediate radii (e.g. 20dp) quite frequently - this scale covers
 * those cases without forcing everything into the 5 M3 roles.
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
