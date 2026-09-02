package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes

/**
 * Escala de formas do Material 3 (extraSmall...extraLarge), calibrada pelos
 * raios de canto mais usados no app (levantamento real do código: 12dp é o
 * mais comum, seguido de 8dp, 16dp e 20dp). Ver [BolaoRadius] pra valores
 * intermediários que não se encaixam nos 5 papéis oficiais do M3.
 */
val BolaoShapes =
    Shapes(
        extraSmall = RoundedCornerShape(BolaoRadius.xs),
        small = RoundedCornerShape(BolaoRadius.sm),
        medium = RoundedCornerShape(BolaoRadius.md),
        large = RoundedCornerShape(BolaoRadius.lg),
        extraLarge = RoundedCornerShape(BolaoRadius.xxl)
    )
