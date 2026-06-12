package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.presentation.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: AuthViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    
    var name by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    // Estados para controlar se o campo já foi interagido (para não mostrar erro logo de cara)
    var nameTouched by remember { mutableStateOf(false) }
    var usernameTouched by remember { mutableStateOf(false) }
    var nicknameTouched by remember { mutableStateOf(false) }
    var phoneTouched by remember { mutableStateOf(false) }
    var emailTouched by remember { mutableStateOf(false) }
    var passwordTouched by remember { mutableStateOf(false) }
    var confirmPasswordTouched by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.user) { if (uiState.user != null) onRegisterSuccess() }

    // Helpers de Validação
    fun isValidFullName(n: String): Boolean {
        val parts = n.trim().split(" ").filter { it.isNotBlank() }
        return parts.size >= 2 && parts.all { it.length >= 2 }
    }

    val nameError = if (nameTouched) {
        when {
            name.isBlank() -> "Nome obrigatório"
            !isValidFullName(name) -> "Digite seu nome e sobrenome"
            else -> null
        }
    } else null

    val usernameError = if (usernameTouched) {
        when {
            username.isBlank() -> "ID obrigatório"
            username.length <= 4 -> "Mínimo 5 letras"
            !username.all { it.isLetter() } -> "Use apenas letras (sem números ou símbolos)"
            else -> null
        }
    } else null
    
    val emailError = if (emailTouched) {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\$"
        if (email.isBlank() || !email.matches(emailRegex.toRegex())) "E-mail inválido" else null
    } else null
    
    val phoneError = if (phoneTouched) {
        val digits = phone.filter { it.isDigit() }
        if (digits.length < 10) "Telefone inválido (mín. 10 dígitos)" else null
    } else null
    
    val nicknameError = if (nicknameTouched && nickname.isNotBlank()) {
        if (!nickname.all { it.isLetterOrDigit() }) "Use apenas letras e números" else null
    } else null
    
    val passwordError = if (passwordTouched && password.length < 6) "Mínimo 6 caracteres" else null
    
    val confirmPasswordError = if (confirmPasswordTouched && confirmPassword != password) "As senhas não coincidem" else null

    val isFormValid = name.isNotBlank() && username.isNotBlank() && email.isNotBlank() && password.length >= 6 && 
                     confirmPassword == password && nameError == null && usernameError == null && emailError == null &&
                     phoneError == null && nicknameError == null

    Box(
        modifier = Modifier
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
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 28.dp)
                    .verticalScroll(rememberScrollState()),
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
                    modifier = Modifier
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
                            label = "Nome Completo (ex: João da Silva)",
                            isError = nameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nameError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

                    // Campo ID (Username)
                    Column {
                        BolaoTextField(
                            value = username,
                            onValueChange = { 
                                if (it.length <= 20) username = it.filter { char -> char.isLetter() }
                                usernameTouched = true
                            },
                            label = "ID (usuário único, ex: joaosilva)",
                            isError = usernameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        usernameError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

                    // Campo Apelido
                    Column {
                        BolaoTextField(
                            value = nickname,
                            onValueChange = { 
                                if (it.length <= 20) nickname = it
                                nicknameTouched = true
                            },
                            label = "Apelido (ex: Fofinho)",
                            isError = nicknameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        nicknameError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

                    // Campo Telefone
                    Column {
                        BolaoTextField(
                            value = phone,
                            onValueChange = { 
                                if (it.length <= 15) phone = it
                                phoneTouched = true
                            },
                            label = "Telefone (com DDD, ex: 11987654321)",
                            isError = phoneError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        phoneError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

                    // Campo Email
                    Column {
                        BolaoTextField(
                            value = email,
                            onValueChange = { 
                                if (it.length <= 60) email = it
                                emailTouched = true
                            },
                            label = "E-mail (ex: joaosilva@gmail.com)",
                            isError = emailError != null,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        emailError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        passwordError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
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
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                        )
                        confirmPasswordError?.let { Text(it, color = ErrorRed, fontSize = 11.sp, modifier = Modifier.padding(start = 8.dp, top = 4.dp)) }
                    }

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(4.dp))

                    BolaoButton(
                        text = "Criar conta",
                        isLoading = uiState.isLoading,
                        enabled = isFormValid && !uiState.isLoading,
                        onClick = {
                            viewModel.register(email, password, name, phone, nickname, username)
                        }
                    )
                }

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
