package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import bolaodagalera.feature_auth.generated.resources.Res
import bolaodagalera.feature_auth.generated.resources.login_button_change_email
import bolaodagalera.feature_auth.generated.resources.login_button_continue
import bolaodagalera.feature_auth.generated.resources.login_button_create_account_now
import bolaodagalera.feature_auth.generated.resources.login_button_forgot_password
import bolaodagalera.feature_auth.generated.resources.login_button_submit
import bolaodagalera.feature_auth.generated.resources.login_email_not_found_message
import bolaodagalera.feature_auth.generated.resources.login_field_email_label
import bolaodagalera.feature_auth.generated.resources.login_field_password_label
import bolaodagalera.feature_auth.generated.resources.login_logo_content_description
import bolaodagalera.feature_auth.generated.resources.login_password_error_min_length
import bolaodagalera.feature_auth.generated.resources.login_subtitle
import bolaodagalera.feature_auth.generated.resources.login_title
import bolaodagalera.feature_auth.generated.resources.logo_oficial
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoGlassCard
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientBg
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.util.ValidationUtils
import kotlin.time.Duration.Companion.milliseconds
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
            // Small delay to let Compose recompose the Column with the trailing spacer
            kotlinx.coroutines.delay(100.milliseconds)
            scrollState.animateScrollTo(scrollState.maxValue)
        }
    }

    // Validation helpers
    val emailError = if (emailTouched) ValidationUtils.validateEmail(email) else null

    val passwordErrorText = stringResource(Res.string.login_password_error_min_length)
    val passwordError = if (passwordTouched && (password.length < 6)) passwordErrorText else null

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
                    .padding(horizontal = BolaoSpacing.xxxl)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(60.dp))
                // Logo area
                Image(
                    painter = painterResource(Res.drawable.logo_oficial),
                    contentDescription = stringResource(Res.string.login_logo_content_description),
                    modifier = Modifier.size(180.dp)
                )

                Spacer(Modifier.height(20.dp))

                BolaoText(
                    stringResource(Res.string.login_title),
                    fontSize = BolaoTypography.displayMedium.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                BolaoText(
                    stringResource(Res.string.login_subtitle),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = Gold,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(48.dp))

                // Glass card
                BolaoGlassCard {
                    // STEP 1: Email
                    Column {
                        BolaoTextField(
                            value = email,
                            onValueChange = {
                                email = it.lowercase().trim()
                                emailTouched = true
                                if (uiState.emailExists != null) viewModel.resetEmailCheck()
                            },
                            label = stringResource(Res.string.login_field_email_label),
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
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }

                        if (uiState.emailExists != null) {
                            BolaoTextButton(
                                onClick = { viewModel.resetEmailCheck() },
                                modifier = Modifier.height(32.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                BolaoText(
                                    stringResource(Res.string.login_button_change_email),
                                    color = Neon,
                                    fontSize = BolaoTypography.bodyMedium.fontSize,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = uiState.emailExists == true) {
                        Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.lg)) {
                            Column {
                                BolaoTextField(
                                    value = password,
                                    onValueChange = {
                                        password = it
                                        passwordTouched = true
                                    },
                                    label = stringResource(Res.string.login_field_password_label),
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
                                    BolaoText(
                                        it,
                                        color = ErrorRed,
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                                    )
                                }

                                BolaoTextButton(
                                    onClick = { viewModel.resetPassword(email) },
                                    modifier = Modifier.align(Alignment.End).height(32.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    BolaoText(
                                        stringResource(Res.string.login_button_forgot_password),
                                        color = Gold,
                                        fontSize = BolaoTypography.bodyMedium.fontSize,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            BolaoButton(
                                text = stringResource(Res.string.login_button_submit),
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
                            verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
                        ) {
                            BolaoText(
                                stringResource(Res.string.login_email_not_found_message),
                                color = Gold,
                                fontSize = BolaoTypography.bodyLarge.fontSize,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = BolaoSpacing.sm)
                            )

                            BolaoButton(
                                text = stringResource(Res.string.login_button_create_account_now)
                            ) {
                                onNavigateToRegister(email)
                            }
                        }
                    }

                    if (uiState.emailExists == null) {
                        BolaoButton(
                            text = stringResource(Res.string.login_button_continue),
                            isLoading = uiState.isLoading,
                            enabled = email.isNotBlank() && emailError == null && !uiState.isLoading
                        ) {
                            viewModel.checkEmail(email)
                        }
                    }

                    uiState.error?.let {
                        BolaoText(
                            it,
                            color = ErrorRed,
                            fontSize = BolaoTypography.bodyMedium.fontSize,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    uiState.successMessage?.let {
                        BolaoText(
                            it,
                            color = Neon,
                            fontSize = BolaoTypography.bodyMedium.fontSize,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))
                // 300dp cushion to keep clearance between the field and the keyboard
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }
            }
        }
    }
}
