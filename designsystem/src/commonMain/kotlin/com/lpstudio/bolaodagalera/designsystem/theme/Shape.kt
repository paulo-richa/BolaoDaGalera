package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Material 3 shape scale (extraSmall...extraLarge), calibrated from the
 * corner radii actually used across the app (code audit: 12dp is the most
 * common, followed by 8dp, 16dp, and 20dp). See [BolaoRadius] for
 * intermediate values that don't fit the 5 official M3 roles.
 */
val BolaoShapes =
    Shapes(
        extraSmall = RoundedCornerShape(BolaoRadius.xs),
        small = RoundedCornerShape(BolaoRadius.sm),
        medium = RoundedCornerShape(BolaoRadius.md),
        large = RoundedCornerShape(BolaoRadius.lg),
        extraLarge = RoundedCornerShape(BolaoRadius.xxl)
    )
