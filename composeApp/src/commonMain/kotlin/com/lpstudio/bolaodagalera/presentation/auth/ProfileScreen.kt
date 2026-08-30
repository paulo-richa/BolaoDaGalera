package com.lpstudio.bolaodagalera.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.APP_VERSION
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.theme.TextSubtle
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.util.ValidationUtils
import com.lpstudio.bolaodagalera.util.getInitials
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onNavigateToHelp: () -> Unit, onNavigateBack: () -> Unit, onSignOut: () -> Unit) {
    val viewModel: AuthViewModel = koinInject()
    val uiState by viewModel.uiState.collectAsState()
    val launcherProvider = rememberLauncherProvider()
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()
    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()

    LaunchedEffect(keyboardHeight) {
        // Removido scroll automático para o final em formulários longos
    }

    var name by remember { mutableStateOf(value = "") }
    var nickname by remember { mutableStateOf(value = "") }
    var phone by remember { mutableStateOf(value = "") }
    var showSignOutDialog by remember { mutableStateOf(value = false) }
    var showDeleteAccountDialog by remember { mutableStateOf(value = false) }
    var showChangePasswordDialog by remember { mutableStateOf(value = false) }

    val isNameValid = ValidationUtils.isValidFullName(name)

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.user) {
        if (uiState.user == null) {
            onSignOut()
        } else {
            uiState.user?.let {
                name = it.name
                nickname = it.nickname
                phone = it.phone
            }
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = NavyCard,
            title = { Text("Sair da conta?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Você precisará entrar novamente para acessar seus bolões.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSignOutDialog = false
                        viewModel.signOut()
                    }
                ) {
                    Text("Sair", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = NavyCard,
            title = { Text("Excluir conta permanentemente?", color = ErrorRed, fontWeight = FontWeight.Bold) },
            text = { Text("Esta ação não pode ser desfeita. Todos os seus bolões e palpites serão perdidos.", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteAccountDialog = false
                        // viewModel.deleteAccount() // A implementar
                    }
                ) {
                    Text("Excluir", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            containerColor = NavyCard,
            title = { Text("Alterar Senha", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Enviaremos um e-mail de recuperação para ${uiState.user?.email} " +
                        "para que você possa redefinir sua senha com segurança.",
                    color = TextMuted
                )
            },
            confirmButton = {
                BolaoButton(
                    text = "Enviar E-mail",
                    onClick = {
                        showChangePasswordDialog = false
                        uiState.user?.email?.let { viewModel.resetPassword(it) }
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = Neon,
                        contentColor = DeepNavy,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            topBar = {
                TopAppBar(
                    title = {
                        Text("Minha Conta", fontWeight = FontWeight.Bold, color = Color.White)
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
                    actions = {
                        IconButton(onClick = { showSignOutDialog = true }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, "Sair", tint = ErrorRed)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets(top = 0.dp)
                )
            }
        ) { padding ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(24.dp))
                // Spacer(Modifier.height(10.dp)) // Removido para subir o header ao máximo

                // Avatar large
                UserAvatar(
                    initials = uiState.user?.name?.getInitials() ?: "?",
                    size = 100.dp,
                    fontSize = 40.sp,
                    borderColor = Neon
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    uiState.user?.email ?: "",
                    fontSize = 14.sp,
                    color = TextMuted
                )

                Spacer(Modifier.height(32.dp))

                // Form card
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(NavyCard)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campo ID (Não editável)
                    BolaoTextField(
                        value = uiState.user?.username ?: "",
                        onValueChange = { },
                        label = "ID da Conta",
                        enabled = false
                    )

                    BolaoTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = "Nome Completo (ex: João da Silva)",
                        isError = name.isNotBlank() && !isNameValid
                    )
                    if (name.isNotBlank() && !isNameValid) {
                        Text(
                            "Digite seu nome e sobrenome",
                            color = ErrorRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    BolaoTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = "Apelido (ex: Fofinho)"
                    )

                    BolaoTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = "Telefone (com DDD, ex: 11987654321)"
                    )

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth())
                    }

                    Spacer(Modifier.height(8.dp))

                    BolaoButton(
                        text = "Salvar Alterações",
                        isLoading = uiState.isLoading,
                        enabled = isNameValid && !uiState.isLoading,
                        onClick = { viewModel.updateProfile(name, phone, nickname) }
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Extra Options
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ProfileOptionItem(
                        icon = Icons.Default.Share,
                        title = "Convidar Amigos para o App"
                    ) {
                        launcherProvider.shareText(
                            "Vem jogar o Bolão da Galera comigo! ⚽ Baixe agora e crie seu bolão.\n\n" +
                                "Disponível na Google Play: " +
                                "https://play.google.com/store/apps/details?id=com.lpstudio.bolaodagalera"
                        )
                    }
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Outlined.HelpOutline,
                        title = "Ajuda e Regras"
                    ) {
                        onNavigateToHelp()
                    }
                    ProfileOptionItem(
                        icon = Icons.Default.Person,
                        title = "Alterar Senha"
                    ) {
                        showChangePasswordDialog = true
                    }
                    ProfileOptionItem(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Excluir Minha Conta",
                        textColor = ErrorRed
                    ) {
                        showDeleteAccountDialog = true
                    }
                }

                Spacer(Modifier.height(40.dp))

                Text(
                    "Versão $APP_VERSION",
                    fontSize = 12.sp,
                    color = TextSubtle
                )

                Spacer(Modifier.height(10.dp))
                if (keyboardHeight > 0.dp) {
                    Spacer(Modifier.height(300.dp))
                }

                Spacer(Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun ProfileOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    textColor: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = NavyElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(icon, null, tint = textColor.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
            Text(title, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
        }
    }
}
