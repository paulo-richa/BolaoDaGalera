package com.lpstudio.bolaodagalera.presentation.bolao

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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoGlassCard
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.GradientBg
import com.lpstudio.bolaodagalera.presentation.theme.GradientGold
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Immutable
data class JoinBolaoUiState(
    val isLoading: Boolean = false,
    val joinedBolao: Bolao? = null,
    val requestSent: Boolean = false,
    val alreadyMemberBolaoId: String? = null,
    val error: String? = null
)

class JoinBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {
    private val _uiState = MutableStateFlow(JoinBolaoUiState())
    val uiState: StateFlow<JoinBolaoUiState> = _uiState.asStateFlow()

    fun join(code: String) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, requestSent = false) }
            try {
                // Agora usamos requestJoinBolao para que o dono precise aceitar
                val bolao =
                    performanceMonitor.trace("join_bolao") {
                        bolaoRepository.requestJoinBolao(code.trim().uppercase(), userId)
                    }

                if (userId in bolao.participants) {
                    // Regra 4: Já é membro, sinaliza para navegar direto
                    _uiState.update { it.copy(alreadyMemberBolaoId = bolao.id, isLoading = false) }
                } else {
                    // Regra 3: Novo pedido enviado
                    analyticsTracker.logEvent("bolao_join_requested", mapOf("bolao_id" to bolao.id))
                    _uiState.update { it.copy(joinedBolao = bolao, requestSent = true, isLoading = false) }
                }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao entrar no bolão")
                _uiState.update { it.copy(error = e.message ?: "Código inválido.", isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JoinBolaoScreen(initialCode: String = "", onJoined: (String) -> Unit, onNavigateBack: () -> Unit) {
    val bolaoRepository = koinInject<BolaoRepository>()
    val authRepository = koinInject<AuthRepository>()
    val crashReporter = koinInject<CrashReporter>()
    val performanceMonitor = koinInject<PerformanceMonitor>()
    val analyticsTracker = koinInject<AnalyticsTracker>()
    val viewModel =
        remember { JoinBolaoViewModel(bolaoRepository, authRepository, crashReporter, performanceMonitor, analyticsTracker) }
    val uiState by viewModel.uiState.collectAsState()
    var code by remember(initialCode) { mutableStateOf(initialCode) }
    var codeTouched by remember(initialCode) { mutableStateOf(initialCode.isNotEmpty()) }

    val codeError = if (codeTouched && code.length < 6) "Código deve ter 6 caracteres" else null

    LaunchedEffect(initialCode) {
        if (initialCode.length == 6) {
            viewModel.join(initialCode)
        }
    }

    LaunchedEffect(uiState.alreadyMemberBolaoId) {
        uiState.alreadyMemberBolaoId?.let { bolaoId ->
            onJoined(bolaoId)
        }
    }

    if (uiState.requestSent) {
        AlertDialog(
            onDismissRequest = onNavigateBack,
            containerColor = DeepNavy,
            title = { Text("Solicitação Enviada!", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "O dono do bolão recebeu seu convite. Aguarde a aprovação dele para começar a palpitar!",
                    color = TextMuted
                )
            },
            confirmButton = {
                BolaoButton(
                    text = "OK",
                    onClick = onNavigateBack
                )
            }
        )
    }

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(GradientBg)
            .systemBarsPadding()
    ) {
        // Glow
        Box(
            modifier =
            Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .offset(y = (-80).dp)
                .background(
                    androidx.compose.ui.graphics.Brush.radialGradient(
                        listOf(Gold.copy(alpha = 0.08f), Color.Transparent)
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                BolaoTopBar(title = "Entrar em Bolão", onNavigateBack = onNavigateBack)
            }
        ) { padding ->
            val scrollState = rememberScrollState()
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 28.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("🔑", fontSize = 64.sp)
                Spacer(Modifier.height(16.dp))
                Text(
                    "Digite o código do bolão",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Peça o código de 6 caracteres para quem criou o bolão.",
                    fontSize = 14.sp,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(Modifier.height(36.dp))

                // Code input card
                BolaoGlassCard(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = code,
                        onValueChange = {
                            if (it.length <= 6) code = it.uppercase()
                            codeTouched = true
                        },
                        label = { Text("Código", color = if (codeError != null) ErrorRed else TextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        isError = codeError != null,
                        shape = RoundedCornerShape(12.dp),
                        colors =
                        OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color(0xFF2A3D55),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = Gold,
                            focusedContainerColor = NavyElevated,
                            unfocusedContainerColor = NavyCard,
                            errorBorderColor = ErrorRed,
                            errorLabelColor = ErrorRed
                        ),
                        keyboardOptions =
                        KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions =
                        KeyboardActions(
                            onDone = { if (code.length == 6) viewModel.join(code) }
                        ),
                        singleLine = true,
                        textStyle =
                        TextStyle(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 10.sp,
                            textAlign = TextAlign.Center,
                            color = Gold
                        )
                    )

                    codeError?.let { Text(it, color = ErrorRed, fontSize = 11.sp) }

                    // Char counter dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (i in 0 until 6) {
                            val filled = i < code.length
                            Box(
                                modifier =
                                Modifier
                                    .size(if (filled) 10.dp else 8.dp)
                                    .clip(RoundedCornerShape(50))
                                    .background(if (filled) Gold else NavyElevated)
                                    .border(1.dp, if (filled) Gold else GlassBorder, RoundedCornerShape(50))
                            )
                        }
                    }

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp, textAlign = TextAlign.Center)
                    }
                }

                Spacer(Modifier.height(24.dp))

                BolaoButton(
                    text = "Entrar no Bolão",
                    isLoading = uiState.isLoading,
                    enabled = code.length == 6 && !uiState.isLoading,
                    gradient = GradientGold,
                    onClick = { viewModel.join(code) }
                )

                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}
