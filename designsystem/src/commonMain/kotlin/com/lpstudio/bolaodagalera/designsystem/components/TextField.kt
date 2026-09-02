package com.lpstudio.bolaodagalera.designsystem.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassWhite
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted

@Composable
fun BolaoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isPassword: Boolean = false,
    isError: Boolean = false,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    modifier: Modifier = Modifier,
    accentColor: Color = Neon,
    textStyle: TextStyle = LocalTextStyle.current
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = if (isError) ErrorRed else TextMuted, fontSize = 13.sp) },
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        isError = isError,
        textStyle = textStyle,
        colors =
        OutlinedTextFieldDefaults.colors(
            focusedBorderColor = accentColor,
            unfocusedBorderColor = Color(0xFF2A3D55),
            focusedTextColor = Color.White,
            unfocusedTextColor = if (enabled) Color.White else TextMuted,
            cursorColor = accentColor,
            focusedContainerColor = NavyElevated,
            unfocusedContainerColor = NavyCard,
            disabledContainerColor = NavyCard.copy(alpha = 0.5f),
            disabledBorderColor = Color(0xFF2A3D55).copy(alpha = 0.5f),
            disabledTextColor = TextMuted,
            disabledLabelColor = TextMuted.copy(alpha = 0.5f),
            errorBorderColor = ErrorRed,
            errorLabelColor = ErrorRed,
            errorCursorColor = ErrorRed
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        singleLine = true
    )
}

/**
 * Campo compacto (2 dígitos, centralizado) para entrada rápida de placar -
 * usado no diálogo de ajuste manual de placar oficial (ex: [BolaoDialog]
 * com times de admin). Estilo "filled" (não outlined), distinto do
 * [BolaoTextField] padrão do app.
 */
@Composable
fun BolaoScoreField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.width(64.dp),
        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
        colors =
        TextFieldDefaults.colors(
            focusedContainerColor = GlassWhite,
            unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
        )
    )
}

@Preview
@Composable
private fun BolaoTextFieldPreview() {
    var value by remember { mutableStateOf("") }
    BolaoTheme {
        BolaoTextField(value = value, onValueChange = { value = it }, label = "E-mail")
    }
}

@Preview
@Composable
private fun BolaoTextFieldErrorPreview() {
    BolaoTheme {
        BolaoTextField(value = "email-invalido", onValueChange = {}, label = "E-mail", isError = true)
    }
}
