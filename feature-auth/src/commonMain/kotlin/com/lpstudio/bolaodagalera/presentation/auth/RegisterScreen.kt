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
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.util.ValidationUtils
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val NAME_MAX_LENGTH = 50
private const val EMAIL_MAX_LENGTH = 60
private const val NICKNAME_MAX_LENGTH = 20
private const val PHONE_MAX_LENGTH = 15
private const val PASSWORD_MAX_LENGTH = 30

@Composable
private fun RegisterField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    keyboardOptions: KeyboardOptions,
    keyboardActions: KeyboardActions,
    isPassword: Boolean = false
) {
    Column {
        BolaoTextField(
            value = value,
            onValueChange = onValueChange,
            label = label,
            isPassword = isPassword,
            isError = error != null,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )
        error?.let {
            BolaoText(
                it,
                color = ErrorRed,
                fontSize = BolaoTypography.bodyMedium.fontSize,
                modifier = Modifier.padding(start = BolaoSpacing.sm, top = BolaoSpacing.xs)
            )
        }
    }
}

private class RegisterFormState(
    val name: String = "",
    val username: String = "",
    val nickname: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = ""
)

private class RegisterFieldsMutableState(initialEmail: String) {
    var name by mutableStateOf("")
    var username by mutableStateOf("")
    var nickname by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf(initialEmail)
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    fun snapshot() = RegisterFormState(name, username, nickname, phone, email, password, confirmPassword)
}

private class RegisterTouchedMutableState(initialEmailTouched: Boolean) {
    var name by mutableStateOf(false)
    var nickname by mutableStateOf(false)
    var phone by mutableStateOf(false)
    var email by mutableStateOf(initialEmailTouched)
    var password by mutableStateOf(false)
    var confirmPassword by mutableStateOf(false)

    fun snapshot() = RegisterTouchedState(name, nickname, phone, email, password, confirmPassword)
}

private class RegisterValidation(
    val nameError: String?,
    val emailError: String?,
    val phoneError: String?,
    val nicknameError: String?,
    val passwordError: String?,
    val confirmPasswordError: String?,
    val isFormValid: Boolean
)

private const val PHONE_MIN_DIGITS = 10
private const val MIN_PASSWORD_LENGTH = 6

private fun validateName(name: String, touched: Boolean, requiredText: String, invalidText: String): String? = when {
    !touched -> null
    name.isBlank() -> requiredText
    !ValidationUtils.isValidFullName(name) -> invalidText
    else -> null
}

private fun validatePhone(phone: String, touched: Boolean, errorText: String): String? {
    if (!touched || phone.isBlank()) return null
    val digits = phone.filter { it.isDigit() }
    return if (digits.length < PHONE_MIN_DIGITS) errorText else null
}

private fun validateNickname(nickname: String, touched: Boolean, errorText: String): String? {
    if (!touched || nickname.isBlank()) return null
    return if (!nickname.all { it.isLetterOrDigit() }) errorText else null
}

@Composable
private fun rememberRegisterValidation(fields: RegisterFormState, touched: RegisterTouchedState): RegisterValidation {
    val nameError =
        validateName(
            fields.name,
            touched.name,
            stringResource(Res.string.register_name_error_required),
            stringResource(Res.string.register_name_error_invalid)
        )
    val emailError = if (touched.email) ValidationUtils.validateEmail(fields.email) else null
    val phoneError = validatePhone(fields.phone, touched.phone, stringResource(Res.string.register_phone_error_invalid))
    val nicknameError = validateNickname(fields.nickname, touched.nickname, stringResource(Res.string.register_nickname_error_invalid))

    val passwordErrorText = stringResource(Res.string.register_password_error_min_length)
    val passwordError = if (touched.password && fields.password.length < MIN_PASSWORD_LENGTH) passwordErrorText else null

    val confirmPasswordErrorText = stringResource(Res.string.register_confirm_password_error_mismatch)
    val confirmPasswordError =
        if (touched.confirmPassword && fields.confirmPassword != fields.password) confirmPasswordErrorText else null

    val isFormValid =
        fields.name.isNotBlank() &&
            fields.email.isNotBlank() &&
            fields.password.length >= MIN_PASSWORD_LENGTH &&
            fields.confirmPassword == fields.password &&
            nameError == null &&
            emailError == null &&
            phoneError == null &&
            nicknameError == null

    return RegisterValidation(nameError, emailError, phoneError, nicknameError, passwordError, confirmPasswordError, isFormValid)
}

private class RegisterTouchedState(
    val name: Boolean = false,
    val nickname: Boolean = false,
    val phone: Boolean = false,
    val email: Boolean = false,
    val password: Boolean = false,
    val confirmPassword: Boolean = false
)

private const val USERNAME_GENERATION_DEBOUNCE = 800L
private const val MIN_NAME_PARTS_FOR_USERNAME = 2

@Composable
private fun AutoGenerateUsername(
    name: String,
    isGeneratingUsername: Boolean,
    onGeneratingChange: (Boolean) -> Unit,
    onUsernameGenerated: (String) -> Unit,
    generateUsername: suspend () -> String
) {
    LaunchedEffect(name) {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        if ((parts.size >= MIN_NAME_PARTS_FOR_USERNAME) && !isGeneratingUsername) {
            // Debounce so this doesn't fire on every keystroke while typing fast
            kotlinx.coroutines.delay(USERNAME_GENERATION_DEBOUNCE.milliseconds)
            onGeneratingChange(true)
            try {
                val generated = generateUsername()
                if (generated.isNotBlank()) {
                    onUsernameGenerated(generated)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                appLogger("RegisterScreen").w(e) { "Falha ao auto-gerar username sugerido" }
            }
            onGeneratingChange(false)
        }
    }
}

@Composable
private fun RegisterHeader() {
    BolaoText(stringResource(Res.string.register_welcome_prefix), color = TextMuted, fontSize = BolaoTypography.bodyLarge.fontSize)
    BolaoText(
        stringResource(Res.string.register_app_name),
        fontSize = BolaoTypography.displayMedium.fontSize,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White
    )
}

private fun onFieldChange(maxLength: Int, setValue: (String) -> Unit, setTouched: () -> Unit): (String) -> Unit = { value ->
    if (value.length <= maxLength) setValue(value)
    setTouched()
}

private suspend fun submitRegistration(
    viewModel: AuthViewModel,
    fields: RegisterFormState,
    username: String,
    onUsernameChange: (String) -> Unit,
    onGeneratingChange: (Boolean) -> Unit
) {
    val finalUsername =
        username.ifBlank {
            // Debounced generation may not have completed yet if the user submitted quickly
            onGeneratingChange(true)
            viewModel.generateAvailableUsername(fields.name).also {
                onUsernameChange(it)
                onGeneratingChange(false)
            }
        }
    if (finalUsername.isNotBlank()) {
        viewModel.register(fields.email, fields.password, fields.name, fields.phone, fields.nickname, finalUsername)
    }
}

private class RegisterCardActions(
    val onNameChange: (String) -> Unit,
    val onEmailChange: (String) -> Unit,
    val onNicknameChange: (String) -> Unit,
    val onPhoneChange: (String) -> Unit,
    val onPasswordChange: (String) -> Unit,
    val onConfirmPasswordChange: (String) -> Unit,
    val onSubmit: () -> Unit
)

@Composable
private fun RegisterContactFields(
    fields: RegisterFormState,
    validation: RegisterValidation,
    focusManager: androidx.compose.ui.focus.FocusManager,
    actions: RegisterCardActions
) {
    RegisterField(
        value = fields.name,
        onValueChange = actions.onNameChange,
        label = stringResource(Res.string.register_field_name_label),
        error = validation.nameError,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    RegisterField(
        value = fields.email,
        onValueChange = actions.onEmailChange,
        label = stringResource(Res.string.register_field_email_label),
        error = validation.emailError,
        keyboardOptions =
        KeyboardOptions(keyboardType = KeyboardType.Email, capitalization = KeyboardCapitalization.None, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    RegisterField(
        value = fields.nickname,
        onValueChange = actions.onNicknameChange,
        label = stringResource(Res.string.register_field_nickname_label),
        error = validation.nicknameError,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    RegisterField(
        value = fields.phone,
        onValueChange = actions.onPhoneChange,
        label = stringResource(Res.string.register_field_phone_label),
        error = validation.phoneError,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
}

@Composable
private fun RegisterPasswordFields(
    fields: RegisterFormState,
    validation: RegisterValidation,
    focusManager: androidx.compose.ui.focus.FocusManager,
    actions: RegisterCardActions
) {
    RegisterField(
        value = fields.password,
        onValueChange = actions.onPasswordChange,
        label = stringResource(Res.string.register_field_password_label),
        error = validation.passwordError,
        isPassword = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    RegisterField(
        value = fields.confirmPassword,
        onValueChange = actions.onConfirmPasswordChange,
        label = stringResource(Res.string.register_field_confirm_password_label),
        error = validation.confirmPasswordError,
        isPassword = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
    )
}

@Composable
private fun RegisterCard(
    fields: RegisterFormState,
    validation: RegisterValidation,
    uiState: AuthUiState,
    isGeneratingUsername: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
    actions: RegisterCardActions
) {
    BolaoGlassCard {
        RegisterContactFields(fields, validation, focusManager, actions)
        RegisterPasswordFields(fields, validation, focusManager, actions)

        uiState.error?.let {
            BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize, modifier = Modifier.fillMaxWidth())
        }

        Spacer(Modifier.height(4.dp))

        BolaoButton(
            text = stringResource(Res.string.register_button_submit),
            isLoading = uiState.isLoading || isGeneratingUsername,
            enabled = validation.isFormValid && !uiState.isLoading && !isGeneratingUsername,
            onClick = actions.onSubmit
        )
    }
}

@Composable
fun RegisterScreen(initialEmail: String = "", onRegisterSuccess: () -> Unit, onNavigateBack: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val fieldsState = remember { RegisterFieldsMutableState(initialEmail) }
    val touchedState = remember { RegisterTouchedMutableState(initialEmail.isNotBlank()) }
    var isGeneratingUsername by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    AutoGenerateUsername(
        name = fieldsState.name,
        isGeneratingUsername = isGeneratingUsername,
        onGeneratingChange = { isGeneratingUsername = it },
        onUsernameGenerated = { fieldsState.username = it },
        generateUsername = { viewModel.generateAvailableUsername(fieldsState.name) }
    )

    LaunchedEffect(uiState.user) { if (uiState.user != null) onRegisterSuccess() }

    val fields = fieldsState.snapshot()
    val validation = rememberRegisterValidation(fields, touchedState.snapshot())
    val actions =
        RegisterCardActions(
            onNameChange = onFieldChange(NAME_MAX_LENGTH, { fieldsState.name = it }, { touchedState.name = true }),
            onEmailChange =
            onFieldChange(EMAIL_MAX_LENGTH, { fieldsState.email = it.lowercase().trim() }, { touchedState.email = true }),
            onNicknameChange = onFieldChange(NICKNAME_MAX_LENGTH, { fieldsState.nickname = it }, { touchedState.nickname = true }),
            onPhoneChange = onFieldChange(PHONE_MAX_LENGTH, { fieldsState.phone = it }, { touchedState.phone = true }),
            onPasswordChange = onFieldChange(PASSWORD_MAX_LENGTH, { fieldsState.password = it }, { touchedState.password = true }),
            onConfirmPasswordChange =
            onFieldChange(PASSWORD_MAX_LENGTH, { fieldsState.confirmPassword = it }, { touchedState.confirmPassword = true }),
            onSubmit = {
                scope.launch {
                    submitRegistration(
                        viewModel = viewModel,
                        fields = fields,
                        username = fieldsState.username,
                        onUsernameChange = { fieldsState.username = it },
                        onGeneratingChange = { isGeneratingUsername = it }
                    )
                }
            }
        )

    RegisterScreenContent(
        scrollState = scrollState,
        keyboardHeight = keyboardHeight,
        fields = fields,
        validation = validation,
        uiState = uiState,
        isGeneratingUsername = isGeneratingUsername,
        focusManager = focusManager,
        actions = actions,
        onNavigateBack = onNavigateBack
    )
}

@Composable
private fun RegisterScreenContent(
    scrollState: androidx.compose.foundation.ScrollState,
    keyboardHeight: androidx.compose.ui.unit.Dp,
    fields: RegisterFormState,
    validation: RegisterValidation,
    uiState: AuthUiState,
    isGeneratingUsername: Boolean,
    focusManager: androidx.compose.ui.focus.FocusManager,
    actions: RegisterCardActions,
    onNavigateBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(GradientBg)) {
        BolaoScaffold(
            containerColor = Color.Transparent,
            topBar = { BolaoTopBar(title = stringResource(Res.string.register_top_bar_title), onNavigateBack = onNavigateBack) }
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
                RegisterHeader()

                Spacer(Modifier.height(32.dp))

                RegisterCard(
                    fields = fields,
                    validation = validation,
                    uiState = uiState,
                    isGeneratingUsername = isGeneratingUsername,
                    focusManager = focusManager,
                    actions = actions
                )

                Spacer(Modifier.height(32.dp))
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }
            }
        }
    }
}
