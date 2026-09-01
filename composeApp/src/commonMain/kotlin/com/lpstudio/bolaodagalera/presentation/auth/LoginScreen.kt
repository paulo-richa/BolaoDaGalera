package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.logo_oficial
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.GlassWhite
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.GradientBg
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.util.ValidationUtils
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onNavigateToRegister: (String?) -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    var visible by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(keyboardHeight) {
        if (keyboardHeight > 0.dp) {
            // Pequeno delay para o Compose recompor a Column com o novo Spacer no final
            kotlinx.coroutines.delay(100.milliseconds)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Helpers de Validação
    val emailError = if (emailTouched) ValidationUtils.validateEmail(email) else null

    val passwordError = if (passwordTouched && (password.length < 6)) "Mínimo 6 caracteres" else null

    val isFormValid = email.isNotBlank() && password.length >= 6 && emailError == null && passwordError == null

    LaunchedEffect(Unit) {
        visible = true
        viewModel.resetEmailCheck()
    }
    LaunchedEffect(uiState.user) { if (uiState.user != null) onLoginSuccess() }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(GradientBg)
            .systemBarsPadding()
    ) {
        // Decorative glow
        Box(
            modifier =
            Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .offset(y = (-60).dp)
                .background(
                    Brush.radialGradient(listOf(Neon.copy(alpha = 0.12f), Color.Transparent)),
                    shape = RoundedCornerShape(50)
                )
        )

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 3 }
        ) {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(horizontal = 28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(60.dp))
                // Logo area
                Image(
                    painter = painterResource(Res.drawable.logo_oficial),
                    contentDescription = "Logo Bolão da Galera",
                    modifier = Modifier.size(180.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    "Bolão da Galera",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Crie e jogue bolões de futebol com os seus amigos",
                    fontSize = 14.sp,
                    color = Gold,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(48.dp))

                // Glass card
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // STEP 1: Email
                    Column {
                        BolaoTextField(
                            value = email,
                            onValueChange = {
                                email = it.lowercase().trim()
                                emailTouched = true
                                if (uiState.emailExists != null) viewModel.resetEmailCheck()
                            },
                            label = "E-mail",
                            enabled = uiState.emailExists == null,
                            isError = emailError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                capitalization = androidx.compose.ui.text.input.KeyboardCapitalization.None,
                                imeAction = if (uiState.emailExists == true) ImeAction.Next else ImeAction.Done
                            ),
                            keyboardActions =
                            KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) },
                                onDone = {
                                    if (uiState.emailExists == null && emailError == null && email.isNotBlank()) {
                                        viewModel.checkEmail(email)
                                    }
                                }
                            )
                        )
                        emailError?.let {
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        if (uiState.emailExists != null) {
                            TextButton(
                                onClick = { viewModel.resetEmailCheck() },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.height(32.dp)
                            ) {
                                Text("Mudar e-mail", color = Neon, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    AnimatedVisibility(visible = uiState.emailExists == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Column {
                                BolaoTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordTouched = true
                                    },
                                    label = "Senha",
                                    isPassword = true,
                                    isError = passwordError != null,
                                    keyboardOptions =
                                    KeyboardOptions(
                                        keyboardType = KeyboardType.Password,
                                        imeAction = ImeAction.Done
                                    ),
                                    keyboardActions =
                                    KeyboardActions(
                                        onDone = {
                                            focusManager.clearFocus()
                                            if (isFormValid) {
                                                viewModel.login(email, password)
                                            }
                                        }
                                    )
                                )
                                passwordError?.let {
                                    Text(
                                        it,
                                        color = ErrorRed,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.resetPassword(email) },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier.align(Alignment.End).height(32.dp)
                                ) {
                                    Text("Esqueceu a senha?", color = Gold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            BolaoButton(
                                text = "Entrar",
                                isLoading = uiState.isLoading,
                                enabled = isFormValid && !uiState.isLoading
                            ) {
                                viewModel.login(email, password)
                            }
                        }
                    }

                    AnimatedVisibility(visible = uiState.emailExists == false) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Este e-mail ainda não possui conta no Bolão da Galera.",
                                color = Gold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            BolaoButton(
                                text = "CRIAR CONTA AGORA"
                            ) {
                                onNavigateToRegister(email)
                            }
                        }
                    }

                    if (uiState.emailExists == null) {
                        BolaoButton(
                            text = "Continuar",
                            isLoading = uiState.isLoading,
                            enabled = email.isNotBlank() && emailError == null && !uiState.isLoading
                        ) {
                            viewModel.checkEmail(email)
                        }
                    }

                    uiState.error?.let {
                        Text(
                            it,
                            color = ErrorRed,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    uiState.successMessage?.let {
                        Text(
                            it,
                            color = Neon,
                            fontSize = 12.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))
                // Cushion aumentado para 300dp para garantir folga entre campo e teclado
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }
            }
        }
    }
}
