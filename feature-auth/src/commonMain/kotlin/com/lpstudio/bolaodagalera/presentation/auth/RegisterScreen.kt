package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import bolaodagalera.feature_auth.generated.resources.Res
import bolaodagalera.feature_auth.generated.resources.register_app_name
import bolaodagalera.feature_auth.generated.resources.register_button_submit
import bolaodagalera.feature_auth.generated.resources.register_confirm_password_error_mismatch
import bolaodagalera.feature_auth.generated.resources.register_field_confirm_password_label
import bolaodagalera.feature_auth.generated.resources.register_field_email_label
import bolaodagalera.feature_auth.generated.resources.register_field_name_label
import bolaodagalera.feature_auth.generated.resources.register_field_nickname_label
import bolaodagalera.feature_auth.generated.resources.register_field_password_label
import bolaodagalera.feature_auth.generated.resources.register_field_phone_label
import bolaodagalera.feature_auth.generated.resources.register_name_error_invalid
import bolaodagalera.feature_auth.generated.resources.register_name_error_required
import bolaodagalera.feature_auth.generated.resources.register_nickname_error_invalid
import bolaodagalera.feature_auth.generated.resources.register_password_error_min_length
import bolaodagalera.feature_auth.generated.resources.register_phone_error_invalid
import bolaodagalera.feature_auth.generated.resources.register_top_bar_title
import bolaodagalera.feature_auth.generated.resources.register_welcome_prefix
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoGlassCard
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GradientBg
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.util.ValidationUtils
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(initialEmail: String = "", onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf(initialEmail) }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Tracks whether each field has been interacted with, to suppress premature error display
    var nameTouched by remember { mutableStateOf(false) }
    var nicknameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(initialEmail.isNotBlank()) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }

    var isGeneratingUsername by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    // Automatic ID generation logic
    LaunchedEffect(name) {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        if ((parts.size >= 2) && !isGeneratingUsername) {
            // Debounce so this doesn't fire on every keystroke while typing fast
            kotlinx.coroutines.delay(800.milliseconds)
            isGeneratingUsername = true
            try {
                val generated = viewModel.generateAvailableUsername(name)
                if (generated.isNotBlank()) {
                    username = generated
                }
            } catch (_: Exception) {
            }
            isGeneratingUsername = false
        }
    }

    LaunchedEffect(keyboardHeight) {
        // Auto-scroll-to-end intentionally disabled: the form is long and would hide the top
    }

    LaunchedEffect(uiState.user) { if (uiState.user != null) onRegisterSuccess() }

    // Validation helpers
    val nameErrorRequiredText = stringResource(Res.string.register_name_error_required)
    val nameErrorInvalidText = stringResource(Res.string.register_name_error_invalid)
    val nameError =
        if (nameTouched) {
            when {
                name.isBlank() -> nameErrorRequiredText
                !ValidationUtils.isValidFullName(name) -> nameErrorInvalidText
                else -> null
            }
        } else {
            null
        }

    val emailError = if (emailTouched) ValidationUtils.validateEmail(email) else null

    val phoneErrorText = stringResource(Res.string.register_phone_error_invalid)
    val phoneError =
        if (phoneTouched && phone.isNotBlank()) {
            val digits = phone.filter { it.isDigit() }
            if (digits.length < 10) phoneErrorText else null
        } else {
            null
        }

    val nicknameErrorText = stringResource(Res.string.register_nickname_error_invalid)
    val nicknameError =
        if (nicknameTouched && nickname.isNotBlank()) {
            if (!nickname.all { it.isLetterOrDigit() }) nicknameErrorText else null
        } else {
            null
        }

    val passwordErrorText = stringResource(Res.string.register_password_error_min_length)
    val passwordError = if (passwordTouched && password.length < 6) passwordErrorText else null

    val confirmPasswordErrorText = stringResource(Res.string.register_confirm_password_error_mismatch)
    val confirmPasswordError = if (confirmPasswordTouched && confirmPassword != password) confirmPasswordErrorText else null

    val isFormValid =
        name.isNotBlank() &&
            email.isNotBlank() &&
            password.length >= 6 &&
            confirmPassword == password &&
            nameError == null &&
            emailError == null &&
            phoneError == null &&
            nicknameError == null

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(GradientBg)
            .systemBarsPadding()
    ) {
        BolaoScaffold(
            containerColor = Color.Transparent,
            topBar = {
                BolaoTopBar(title = stringResource(Res.string.register_top_bar_title), onNavigateBack = onNavigateBack)
            }
        ) { padding ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = BolaoSpacing.xxxl)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(12.dp))

                BolaoText(
                    stringResource(Res.string.register_welcome_prefix),
                    color = TextMuted,
                    fontSize = BolaoTypography.bodyLarge.fontSize
                )
                BolaoText(
                    stringResource(Res.string.register_app_name),
                    fontSize = BolaoTypography.displayMedium.fontSize,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(Modifier.height(32.dp))

                BolaoGlassCard {
                    // Name field
                    Column {
                        BolaoTextField(
                            value = name,
                            onValueChange = {
                                if (it.length <= 50) name = it
                                nameTouched = true
                            },
                            label = stringResource(Res.string.register_field_name_label),
                            isError = nameError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nameError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    // Email field - placed below the name field
                    Column {
                        BolaoTextField(
                            value = email,
                            onValueChange = {
                                if (it.length <= 60) email = it.lowercase().trim()
                                emailTouched = true
                            },
                            label = stringResource(Res.string.register_field_email_label),
                            isError = emailError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                capitalization = KeyboardCapitalization.None,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        emailError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    // Nickname field
                    Column {
                        BolaoTextField(
                            value = nickname,
                            onValueChange = {
                                if (it.length <= 20) nickname = it
                                nicknameTouched = true
                            },
                            label = stringResource(Res.string.register_field_nickname_label),
                            isError = nicknameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nicknameError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    // Phone field
                    Column {
                        BolaoTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 15) phone = it
                                phoneTouched = true
                            },
                            label = stringResource(Res.string.register_field_phone_label),
                            isError = phoneError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        phoneError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    // Email field - removed from here; relocated above

                    // Password field
                    Column {
                        BolaoTextField(
                            value = password,
                            onValueChange = {
                                if (it.length <= 30) password = it
                                passwordTouched = true
                            },
                            label = stringResource(Res.string.register_field_password_label),
                            isPassword = true,
                            isError = passwordError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        passwordError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    // Confirm password field
                    Column {
                        BolaoTextField(
                            value = confirmPassword,
                            onValueChange = {
                                if (it.length <= 30) confirmPassword = it
                                confirmPasswordTouched = true
                            },
                            label = stringResource(Res.string.register_field_confirm_password_label),
                            isPassword = true,
                            isError = confirmPasswordError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                        confirmPasswordError?.let {
                            BolaoText(
                                it,
                                color = ErrorRed,
                                fontSize = BolaoTypography.bodyMedium.fontSize,
                                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
                            )
                        }
                    }

                    uiState.error?.let {
                        BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(4.dp))

                    BolaoButton(
                        text = stringResource(Res.string.register_button_submit),
                        isLoading = uiState.isLoading || isGeneratingUsername,
                        enabled = isFormValid && !uiState.isLoading && !isGeneratingUsername
                    ) {
                        if (username.isBlank()) {
                            // Debounced generation may not have completed yet if the user submitted quickly
                            scope.launch {
                                isGeneratingUsername = true
                                val generated = viewModel.generateAvailableUsername(name)
                                username = generated
                                isGeneratingUsername = false
                                if (username.isNotBlank()) {
                                    viewModel.register(email, password, name, phone, nickname, username)
                                }
                            }
                        } else {
                            viewModel.register(email, password, name, phone, nickname, username)
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }
            }
        }
    }
}
