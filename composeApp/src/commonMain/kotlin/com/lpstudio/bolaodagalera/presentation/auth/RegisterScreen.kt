package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.GlassWhite
import com.lpstudio.bolaodagalera.presentation.theme.GradientBg
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.util.ValidationUtils
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
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

    // Estados para controlar se o campo já foi interagido (para não mostrar erro logo de cara)
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

    // Lógica de Geração Automática de ID
    LaunchedEffect(name) {
        val parts = name.trim().split(" ").filter { it.isNotBlank() }
        if ((parts.size >= 2) && !isGeneratingUsername) {
            // Pequeno delay para não disparar a cada tecla se a pessoa digitar rápido
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
        // Removido scroll automático para o final, pois o formulário é longo e esconde o topo
    }

    LaunchedEffect(uiState.user) { if (uiState.user != null) onRegisterSuccess() }

    // Helpers de Validação
    val nameError =
        if (nameTouched) {
            when {
                name.isBlank() -> "Nome obrigatório"
                !ValidationUtils.isValidFullName(name) -> "Digite seu nome e sobrenome"
                else -> null
            }
        } else {
            null
        }

    val emailError = if (emailTouched) ValidationUtils.validateEmail(email) else null

    val phoneError =
        if (phoneTouched && phone.isNotBlank()) {
            val digits = phone.filter { it.isDigit() }
            if (digits.length < 10) "Telefone inválido (mín. 10 dígitos)" else null
        } else {
            null
        }

    val nicknameError =
        if (nicknameTouched && nickname.isNotBlank()) {
            if (!nickname.all { it.isLetterOrDigit() }) "Use apenas letras e números" else null
        } else {
            null
        }

    val passwordError = if (passwordTouched && password.length < 6) "Mínimo 6 caracteres" else null

    val confirmPasswordError = if (confirmPasswordTouched && confirmPassword != password) "As senhas não coincidem" else null

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
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Criar conta",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Voltar",
                                tint = Color.White
                            )
                        }
                    },
                    colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(12.dp))

                Text(
                    "Bem-vindo ao",
                    color = TextMuted,
                    fontSize = 14.sp
                )
                Text(
                    "Bolão da Galera",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )

                Spacer(Modifier.height(32.dp))

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
                    // Campo Nome
                    Column {
                        BolaoTextField(
                            value = name,
                            onValueChange = {
                                if (it.length <= 50) name = it
                                nameTouched = true
                            },
                            label = "Nome Completo",
                            isError = nameError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                capitalization = KeyboardCapitalization.Words,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nameError?.let {
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    // Campo Email - Movido para baixo do nome
                    Column {
                        BolaoTextField(
                            value = email,
                            onValueChange = {
                                if (it.length <= 60) email = it.lowercase().trim()
                                emailTouched = true
                            },
                            label = "E-mail (ex: joaosilva@gmail.com)",
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
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    // Campo Apelido
                    Column {
                        BolaoTextField(
                            value = nickname,
                            onValueChange = {
                                if (it.length <= 20) nickname = it
                                nicknameTouched = true
                            },
                            label = "Apelido (opcional, ex: Fofinho)",
                            isError = nicknameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nicknameError?.let {
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    // Campo Telefone
                    Column {
                        BolaoTextField(
                            value = phone,
                            onValueChange = {
                                if (it.length <= 15) phone = it
                                phoneTouched = true
                            },
                            label = "Telefone (opcional, ex: 11987654321)",
                            isError = phoneError != null,
                            keyboardOptions =
                            KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        phoneError?.let {
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    // Campo Email - Removido daqui pois foi movido para cima

                    // Campo Senha
                    Column {
                        BolaoTextField(
                            value = password,
                            onValueChange = {
                                if (it.length <= 30) password = it
                                passwordTouched = true
                            },
                            label = "Senha (min. 6 caracteres)",
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
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    // Campo Confirmar Senha
                    Column {
                        BolaoTextField(
                            value = confirmPassword,
                            onValueChange = {
                                if (it.length <= 30) confirmPassword = it
                                confirmPasswordTouched = true
                            },
                            label = "Confirmar senha",
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
                            Text(
                                it,
                                color = ErrorRed,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }
                    }

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(4.dp))

                    BolaoButton(
                        text = "Criar conta",
                        isLoading = uiState.isLoading || isGeneratingUsername,
                        enabled = isFormValid && !uiState.isLoading && !isGeneratingUsername
                    ) {
                        if (username.isBlank()) {
                            // Caso o usuário tenha sido muito rápido, gera o ID agora
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
