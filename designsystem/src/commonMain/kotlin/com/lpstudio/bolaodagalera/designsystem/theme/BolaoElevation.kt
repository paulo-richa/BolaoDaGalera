package com.lpstudio.bolaodagalera.designsystem.theme

import androidx.compose.ui.unit.dp

/**
 * The 6 official Material 3 "tonal elevation" levels (level0...level5).
 * The app's visual style is flat (borders + translucent background instead
 * of shadow), so most components default to level0 - but the levels exist
 * for when a component genuinely needs to stand out (e.g. dialogs, which
 * already used level3 before this token existed, as the M3 default).
 */
object BolaoElevation {
    val level0 = 0.dp
    val level1 = 1.dp
    val level2 = 3.dp
    val level3 = 6.dp
    val level4 = 8.dp
    val level5 = 12.dp
}
