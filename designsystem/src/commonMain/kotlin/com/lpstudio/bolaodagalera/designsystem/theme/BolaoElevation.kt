package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * Os 6 níveis oficiais de "tonal elevation" do Material 3 (level0...level5).
 * O visual do app é flat (bordas + fundo translúcido em vez de sombra), então
 * a maioria dos componentes usa level0 por padrão - mas os níveis existem
 * pra quando um componente precisar se destacar de verdade (ex: diálogos,
 * que já usam level3 mesmo antes desse token existir, como padrão do M3).
 */
object BolaoElevation {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
    val level4 = 8.dp
    val level5 = 12.dp
}
