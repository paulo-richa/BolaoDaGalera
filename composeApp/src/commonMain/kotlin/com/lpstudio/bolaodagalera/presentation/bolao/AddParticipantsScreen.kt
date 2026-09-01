package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.SuccessGreen
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.compose.koinInject

private enum class ParticipantInputType {
    EMAIL,
    PHONE,
    USER
}

private val logger = appLogger("AddParticipantsScreen")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddParticipantsScreen(bolaoId: String, onNavigateBack: () -> Unit) {
    var identifier by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var bolaoCode by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val launcherProvider = rememberLauncherProvider()
    val bolaoRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.BolaoRepository>()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val invitationRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.InvitationRepository>()
    val crashReporter = koinInject<CrashReporter>()
    var bolaoName by remember { mutableStateOf("") }

    // Carrega o nome e código do bolão ao iniciar
    LaunchedEffect(bolaoId) {
        try {
            val bolao = bolaoRepository.getBolao(bolaoId)
            bolaoName = bolao.name
            bolaoCode = bolao.code
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao carregar dados do bolão para convite")
        }
    }

    // Detecção automática e inteligente do tipo de entrada
    val detectedType =
        remember(identifier) {
            val trimmed = identifier.trim()
            when {
                trimmed.contains("@") && trimmed.contains(".") -> ParticipantInputType.EMAIL
                trimmed.filter { it.isDigit() }.length >= 8 -> ParticipantInputType.PHONE
                else -> ParticipantInputType.USER
            }
        }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(DeepNavy)
            .systemBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // ── Header ─────────────────────────────────────────────────────────
            BolaoTopBar(title = "Adicionar Participantes", onNavigateBack = onNavigateBack)

            Column(
                modifier =
                Modifier
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
                    keyboardOptions =
                    KeyboardOptions(
                        keyboardType =
                        when (detectedType) {
                            ParticipantInputType.EMAIL -> KeyboardType.Email
                            ParticipantInputType.PHONE -> KeyboardType.Phone
                            else -> KeyboardType.Text
                        }
                    )
                )

                Spacer(Modifier.height(24.dp))

                if (showSuccessMessage) {
                    Box(
                        modifier =
                        Modifier
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
                    Text(
                        it,
                        color = ErrorRed,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        textAlign = TextAlign.Center
                    )
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
                                val inviterName = authRepository.currentUser?.name ?: "Alguém"

                                // 1. Verificação de existência do usuário no banco de dados
                                val userExists =
                                    when (detectedType) {
                                        ParticipantInputType.EMAIL -> authRepository.isEmailInUse(trimmedId.lowercase())
                                        ParticipantInputType.PHONE -> authRepository.isPhoneInUse(trimmedId.filter { it.isDigit() })
                                        ParticipantInputType.USER -> authRepository.isUsernameInUse(trimmedId.lowercase())
                                    }

                                if (!userExists) {
                                    error = "Usuário não encontrado. Peça para seu amigo criar uma conta primeiro " +
                                        "ou compartilhe o link de convite abaixo."
                                    isLoading = false
                                    return@launch
                                }

                                // 2. Enviar convite interno
                                val inviteeIdentifier =
                                    when (detectedType) {
                                        ParticipantInputType.EMAIL -> trimmedId.lowercase()
                                        ParticipantInputType.PHONE -> trimmedId.filter { it.isDigit() }
                                        ParticipantInputType.USER -> trimmedId.lowercase()
                                    }

                                try {
                                    withTimeout(3000) {
                                        invitationRepository.sendInvitation(
                                            bolaoId = bolaoId,
                                            bolaoName = bolaoName,
                                            inviterName = inviterName,
                                            inviteeIdentifier = inviteeIdentifier
                                        )
                                    }
                                } catch (e: Exception) {
                                    logger.w(e) { "Convite interno em cache para envio posterior (rede lenta)" }
                                }

                                isLoading = false
                                showSuccessMessage = true
                                identifier = ""

                                delay(3000)
                                showSuccessMessage = false
                            } catch (e: Exception) {
                                crashReporter.recordException(e, "Erro ao enviar convite")
                                error = "Não foi possível enviar. Verifique sua conexão."
                                isLoading = false
                            }
                        }
                    }
                )

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = {
                        val webUrl = "https://bolaodagalera-bb002.web.app/invite?code=$bolaoCode"
                        val appUrl = "bolaodagalera://invite?code=$bolaoCode"
                        launcherProvider.shareText(
                            "Entre no meu bolão '$bolaoName'! 🏆\n\nLink: $webUrl\n\n" +
                                "Se o link não abrir o app automaticamente, use este: $appUrl\n\nCódigo: $bolaoCode"
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Neon)
                ) {
                    Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Compartilhar Link de Convite", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(32.dp))

                // Info section
                Column(
                    modifier =
                    Modifier
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
                        "Adicione seus amigos diretamente via E-mail, Telefone ou ID do app. Eles receberão um " +
                            "convite instantâneo na tela inicial deles. Alternativamente, compartilhe o link do bolão " +
                            "para que eles solicitem a entrada.",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }

                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    val keyboardHeight = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                    Spacer(Modifier.height(keyboardHeight + 100.dp))
                }
            }
        }
    }
}
