package com.lpstudio.bolaodagalera.presentation.help

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.help_screen_button_send_request
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onNavigateBack: () -> Unit) {
    val supportRepository = koinInject<SupportRepository>()
    val authRepository = koinInject<AuthRepository>()
    val scope = rememberCoroutineScope()

    var message by remember { mutableStateOf("") }
    var isSending by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var showErrorDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    // Reset da tela de sucesso após 3 segundos
    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            kotlinx.coroutines.delay(3000.milliseconds)
            showSuccess = false
        }
    }

    // Diálogo de Confirmação
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Confirmar Envio", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Deseja enviar sua solicitação de suporte agora?", color = TextMuted) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConfirmDialog = false
                        if (message.isNotBlank() && !isSending) {
                            scope.launch {
                                isSending = true
                                try {
                                    val user = authRepository.currentUser
                                    supportRepository.sendSupportTicket(
                                        userId = user?.id ?: "anonymous",
                                        userEmail = user?.email ?: "no-email",
                                        message = message
                                    )
                                    message = "" // Limpa o texto após sucesso
                                    showSuccess = true
                                } catch (_: Exception) {
                                    showErrorDialog = true
                                } finally {
                                    isSending = false
                                }
                            }
                        }
                    }
                ) {
                    Text("SIM, ENVIAR", color = Neon, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("CANCELAR", color = TextMuted)
                }
            },
            containerColor = NavyCard,
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Diálogo de Erro
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("Falha no Envio", color = ErrorRed, fontWeight = FontWeight.Bold) },
            text = { Text("Ocorreu um erro ao enviar sua mensagem. Verifique sua conexão e tente novamente.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("TENTAR NOVAMENTE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            containerColor = NavyCard,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Central de Ajuda", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                },
                colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepNavy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DeepNavy
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs Personalizadas
            Row(
                modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(NavyCard)
                    .padding(4.dp)
            ) {
                listOf("Regras", "FAQ", "Suporte").forEachIndexed { index, label ->
                    val selected = selectedTab == index
                    Box(
                        modifier =
                        Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) Neon else Color.Transparent)
                            .clickable { selectedTab = index }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (selected) DeepNavy else TextMuted,
                            fontSize = 14.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> RulesSection()
                    1 -> FaqSection()
                    2 ->
                        SupportSection(
                            message = message,
                            onMessageChange = { message = it },
                            isSending = isSending,
                            showSuccess = showSuccess
                        ) {
                            if (message.isNotBlank()) {
                                showConfirmDialog = true
                            }
                        }
                }
            }
        }
    }
}

@Composable
private fun RulesSection() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RuleCard(
                title = "Como Funciona a Pontuação?",
                description = "O objetivo é acertar o placar dos jogos. Existem dois níveis de acerto:",
                points =
                listOf(
                    "3 PONTOS: Acerto exato do placar (Ex: Você palpitou 2x1 e o jogo foi 2x1).",
                    "1 PONTO: Acerto do vencedor ou empate, mas erro no número de gols (Ex: Você palpitou 2x1, mas o jogo foi 1x0)."
                )
            )
        }
        item {
            RuleCard(
                title = "Tempo de Jogo",
                description = "Atenção! Vale apenas o resultado do tempo normal (90 minutos + acréscimos).",
                extra = "Gols em prorrogação ou disputa de pênaltis não são contabilizados no nosso app."
            )
        }
        item {
            RuleCard(
                title = "Prazo para Palpites",
                description = "Você pode enviar ou alterar seu palpite até 1 minuto antes do início oficial da partida.",
                extra = "Após o fechamento, os palpites ficam bloqueados para edição."
            )
        }
        item {
            RuleCard(
                title = "Criação de Bolões",
                description = "Qualquer usuário pode criar um bolão e convidar amigos através do código único gerado.",
                extra = "O criador do bolão é o Administrador e tem o poder de aceitar novos membros."
            )
        }
    }
}

@Composable
private fun FaqSection() {
    val faqs =
        listOf(
            "Como entro em um bolão?" to "Basta clicar em 'Entrar com código' na tela inicial e digitar o código " +
                "compartilhado pelo seu amigo.",
            "Meus pontos não atualizaram, o que fazer?" to "Os rankings são atualizados automaticamente alguns minutos " +
                "após o encerramento oficial do jogo pela nossa API. Caso haja um atraso incomum, o suporte do " +
                "aplicativo monitora e ajusta os placares reais para garantir a pontuação correta.",
            "Posso participar de quantos bolões?" to "Não há limite! Você pode participar de quantos bolões desejar " +
                "simultaneamente.",
            "Esqueci minha senha, como recupero?" to "Na tela de login, utilize a opção 'Esqueci minha senha' para " +
                "receber um link de redefinição no seu e-mail cadastrado."
        )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(faqs) { (question, answer) ->
            var expanded by remember { mutableStateOf(false) }
            Surface(
                color = NavyElevated,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder),
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = question,
                            modifier = Modifier.weight(1f),
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Icon(
                            imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = Neon
                        )
                    }
                    AnimatedVisibility(visible = expanded) {
                        Text(
                            text = answer,
                            color = TextMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            modifier = Modifier.padding(top = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportSection(
    message: String,
    onMessageChange: (String) -> Unit,
    isSending: Boolean,
    showSuccess: Boolean,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showSuccess) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.CheckCircle, null, tint = Neon, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Solicitação enviada!", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("Retornaremos em breve.", color = TextMuted, fontSize = 14.sp)
                }
            }
        } else {
            Box(
                modifier =
                Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Neon.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MailOutline, contentDescription = null, tint = Neon, modifier = Modifier.size(32.dp))
            }

            Spacer(Modifier.height(20.dp))

            Text(
                "Precisa de ajuda ou tem uma sugestão?",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                "Descreva sua situação abaixo e clique em enviar. Nossa equipe receberá sua mensagem diretamente.",
                color = TextMuted,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                lineHeight = 20.sp
            )

            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.fillMaxWidth().height(160.dp),
                placeholder = { Text("Reclamação, pedido de ajuda ou sugestão...", color = TextMuted) },
                enabled = !isSending,
                colors =
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Neon,
                    unfocusedBorderColor = GlassBorder,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Neon
                ),
                shape = RoundedCornerShape(16.dp)
            )

            Spacer(Modifier.height(24.dp))

            if (isSending) {
                CircularProgressIndicator(color = Neon)
            } else {
                BolaoButton(
                    text = stringResource(Res.string.help_screen_button_send_request),
                    onClick = onSend
                )
            }
        }
    }
}

@Composable
private fun RuleCard(title: String, description: String, points: List<String> = emptyList(), extra: String? = null) {
    Surface(
        color = NavyElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Neon, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(description, color = Color.White, fontSize = 14.sp, lineHeight = 20.sp)

            if (points.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                points.forEach { point ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text("•", color = Gold, modifier = Modifier.padding(end = 8.dp))
                        Text(point, color = TextMuted, fontSize = 13.sp, lineHeight = 18.sp)
                    }
                }
            }

            if (extra != null) {
                Spacer(Modifier.height(12.dp))
                Box(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Gold.copy(alpha = 0.1f))
                        .padding(12.dp)
                ) {
                    Text(extra, color = Gold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
