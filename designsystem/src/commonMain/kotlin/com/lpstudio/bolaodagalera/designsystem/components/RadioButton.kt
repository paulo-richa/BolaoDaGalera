package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun BolaoRadioButton(
    selected: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedColor: Color? = null,
    unselectedColor: Color? = null,
    colors: RadioButtonColors =
        if (selectedColor != null || unselectedColor != null) {
            RadioButtonDefaults.colors(
                selectedColor = selectedColor ?: RadioButtonDefaults.colors().selectedColor,
                unselectedColor = unselectedColor ?: RadioButtonDefaults.colors().unselectedColor
            )
        } else {
            RadioButtonDefaults.colors()
        }
) {
    RadioButton(selected = selected, onClick = onClick, modifier = modifier, enabled = enabled, colors = colors)
}
