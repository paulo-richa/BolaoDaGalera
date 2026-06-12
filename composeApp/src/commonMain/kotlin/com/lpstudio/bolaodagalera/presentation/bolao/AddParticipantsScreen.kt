package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.presentation.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private enum class ParticipantInputType {
    EMAIL, PHONE, USER
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsScreen(
    bolaoId: String,
    onNavigateBack: () -> Unit
) {
    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val launcherProvider = com.lpstudio.bolaodagalera.rememberLauncherProvider()
    val bolaoRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.BolaoRepository>()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val invitationRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.InvitationRepository>()
    var bolaoName by remember { mutableStateOf("") }

    // Carrega o nome do bolão ao iniciar
    LaunchedEffect(bolaoId) {
        try {
            val bolao = bolaoRepository.getBolao(bolaoId)
            bolaoName = bolao.name
        } catch (e: Exception) { }
    }

    // Detecção automática e inteligente do tipo de entrada
    val detectedType = remember(identifier) {
        val trimmed = identifier.trim()
        when {
            trimmed.contains("@") -> ParticipantInputType.EMAIL
            trimmed.any { it.isDigit() } && trimmed.length >= 8 -> ParticipantInputType.PHONE
            else -> ParticipantInputType.USER
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        Column(Modifier.fillMaxSize()) {
            // ... (TopAppBar e resto do conteúdo)
            // ── Header ─────────────────────────────────────────────────────────
            TopAppBar(
                title = {
                    Text("Adicionar Participantes", fontWeight = FontWeight.Bold, color = Color.White)
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(20.dp))
                
                Text(
                    "CONVIDAR AMIGO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    letterSpacing = 1.5.sp
                )
                
                Spacer(Modifier.height(12.dp))

                // Input field único e inteligente
                BolaoTextField(
                    value = identifier,
                    onValueChange = { identifier = it },
                    label = "E-mail, Telefone ou ID",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                Spacer(Modifier.height(24.dp))

                if (showSuccessMessage) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(SuccessGreen.copy(alpha = 0.1f))
                            .border(1.dp, SuccessGreen.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Convite enviado com sucesso!",
                            color = SuccessGreen,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(24.dp))
                }

                error?.let {
                    Text(it, color = ErrorRed, fontSize = 12.sp, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp), textAlign = TextAlign.Center)
                }

                // Botão Único: Enviar Convite
                BolaoButton(
                    text = "Enviar Convite",
                    isLoading = isLoading,
                    enabled = identifier.isNotBlank() && !isLoading,
                    onClick = {
                        scope.launch {
                            isLoading = true
                            error = null
                            try {
                                val trimmedId = identifier.trim()
                                val exists = when(detectedType) {
                                    ParticipantInputType.EMAIL -> authRepository.isEmailInUse(trimmedId)
                                    ParticipantInputType.PHONE -> authRepository.isPhoneInUse(trimmedId)
                                    ParticipantInputType.USER -> authRepository.isUsernameInUse(trimmedId.lowercase())
                                }

                                val inviteUrl = "https://bolaodagalera.app/invite?code=${bolaoId.take(6).uppercase()}" // Simplificado para o exemplo, ideal usar o code do bolao
                                val msg = "Entre no meu bolão da Copa 2026! 🏆\nBolão: $bolaoName\n\nLink: $inviteUrl\n\nCódigo: ${bolaoId.take(6).uppercase()}"
                                val inviterName = authRepository.currentUser?.name ?: "Alguém"

                                if (exists) {
                                    // 1. Enviar Notificação/Convite Interno no App
                                    invitationRepository.sendInvitation(
                                        bolaoId = bolaoId,
                                        bolaoName = bolaoName,
                                        inviterName = inviterName,
                                        inviteeIdentifier = if (detectedType == ParticipantInputType.USER) trimmedId.lowercase() else trimmedId
                                    )
                                    
                                    // 2. Ação Complementar Externa
                                    when(detectedType) {
                                        ParticipantInputType.EMAIL -> launcherProvider.sendEmail(trimmedId, "Convite: Bolão da Galera", msg)
                                        ParticipantInputType.PHONE -> launcherProvider.sendWhatsApp(trimmedId, msg)
                                        ParticipantInputType.USER -> { /* Apenas interno */ }
                                    }
                                    
                                    showSuccessMessage = true
                                    identifier = ""
                                    delay(3000)
                                    showSuccessMessage = false
                                } else {
                                    // Usuário não existe no app: Apenas canais externos
                                    when(detectedType) {
                                        ParticipantInputType.EMAIL -> {
                                            launcherProvider.sendEmail(trimmedId, "Convite: Bolão da Galera", msg)
                                            showSuccessMessage = true
                                            identifier = ""
                                            delay(3000)
                                            showSuccessMessage = false
                                        }
                                        ParticipantInputType.PHONE -> {
                                            launcherProvider.sendWhatsApp(trimmedId, msg)
                                            showSuccessMessage = true
                                            identifier = ""
                                            delay(3000)
                                            showSuccessMessage = false
                                        }
                                        ParticipantInputType.USER -> {
                                            error = "ID não encontrado. Verifique se o ID está correto ou convide via E-mail/WhatsApp."
                                        }
                                    }
                                }
                            } catch (e: Exception) {
                                error = "Erro ao processar convite. Tente novamente."
                            } finally {
                                isLoading = false
                            }
                        }
                    }
                )

                Spacer(Modifier.height(40.dp))
                
                // Info section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(NavyElevated)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("📢", fontSize = 32.sp)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Como funciona?",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Enviaremos uma notificação ou e-mail para o usuário convidado. Assim que ele aceitar, ele aparecerá automaticamente na lista de participantes.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
