package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Immutable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextAlign
import com.lpstudio.bolaodagalera.LauncherProvider
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.presentation.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Immutable
data class CreateBolaoUiState(
    val isLoading: Boolean = false,
    val createdBolao: Bolao? = null,
    val error: String? = null
)

class CreateBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateBolaoUiState())
    val uiState: StateFlow<CreateBolaoUiState> = _uiState.asStateFlow()

    fun create(name: String, description: String, championshipId: String, pointsExact: Int, pointsWinner: Int) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bolao = bolaoRepository.createBolao(
                    name.trim(), 
                    description.trim(), 
                    userId, 
                    championshipId,
                    pointsExactScore = pointsExact,
                    pointsWinnerOrDraw = pointsWinner
                )
                _uiState.update { it.copy(createdBolao = bolao, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message ?: "Erro ao criar bolão", isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBolaoScreen(
    onCreated: (String) -> Unit,
    onNavigateToAddParticipants: (String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val bolaoRepository = koinInject<BolaoRepository>()
    val authRepository = koinInject<AuthRepository>()
    val viewModel = remember { CreateBolaoViewModel(bolaoRepository, authRepository) }
    val uiState by viewModel.uiState.collectAsState()
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedChampionshipId by remember { mutableStateOf("COPA_2026") }
    var pointsExact by remember { mutableIntStateOf(3) }
    var pointsWinner by remember { mutableIntStateOf(1) }
    
    var nameTouched by remember { mutableStateOf(false) }
    
    val focusManager = LocalFocusManager.current
    val launcherProvider = rememberLauncherProvider()
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Helpers de Validação
    val nameError = if (nameTouched && name.trim().length < 10) "Nome muito curto (mín. 10)" else null
    val isFormValid = name.trim().length in 10..35

    LaunchedEffect(uiState.createdBolao) {
        if (uiState.createdBolao != null) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog && uiState.createdBolao != null) {
        val bolao = uiState.createdBolao!!
        AlertDialog(
            onDismissRequest = { 
                showSuccessDialog = false
                onCreated(bolao.id)
            },
            containerColor = NavyCard,
            title = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Bolão Criado!",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Seu código de convite é:",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Gold.copy(alpha = 0.15f))
                            .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 24.dp, vertical = 12.dp)
                    ) {
                        Text(
                            bolao.code,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Gold,
                            letterSpacing = 2.sp
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Escolha como quer começar:",
                        color = TextMuted,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BolaoButton(
                        text = "Adicionar Participantes",
                        onClick = {
                            showSuccessDialog = false
                            onNavigateToAddParticipants(bolao.id)
                        }
                    )
                    OutlinedButton(
                        onClick = {
                            val inviteUrl = "https://bolaodagalera.app/invite?code=${bolao.code}"
                            launcherProvider.shareText("Entre no meu bolão '${bolao.name}'! 🏆\n\nLink: $inviteUrl\n\nCódigo: ${bolao.code}")
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Neon)
                    ) {
                        Text("Compartilhar Código", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                    }
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showSuccessDialog = false
                            onCreated(bolao.id)
                        }
                    ) {
                        Text(
                            "Ir para o bolão",
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            dismissButton = null
        )
    }

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
                        Text("Criar Bolão", fontWeight = FontWeight.Bold, color = Color.White)
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
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                Spacer(Modifier.height(16.dp))

                // Hero section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 56.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Novo Bolão",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Crie seu bolão e convide amigos com um código único",
                        fontSize = 13.sp,
                        color = TextMuted
                    )
                }

                Spacer(Modifier.height(32.dp))

                // Form card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Campeonato
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Escolha o Campeonato",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        
                        val championships = listOf(
                            "COPA_2026" to ("Copa do Mundo 2026" to "🏆")
                        )

                        championships.forEach { (id, data) ->
                            val (label, emoji) = data
                            val isSelected = selectedChampionshipId == id
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) NavyElevated else NavyCard)
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Neon else GlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedChampionshipId = id }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                    Text(
                                        label,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextMuted
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Neon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Column {
                        BolaoTextField(
                            value = name,
                            onValueChange = { 
                                if (it.length <= 35) {
                                    name = it 
                                    nameTouched = true
                                }
                            },
                            label = "Nome do bolão *",
                            isError = nameError != null,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (nameError != null) {
                                Text(nameError, color = ErrorRed, fontSize = 11.sp)
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }
                            Text(
                                "${name.length}/35",
                                color = if (name.length < 10 || name.length > 35) ErrorRed else TextSubtle,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Column {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 115) description = it },
                            label = { Text("Descrição (opcional)", color = TextMuted, fontSize = 13.sp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Neon,
                                unfocusedBorderColor = Color(0xFF2A3D55),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Neon,
                                focusedContainerColor = NavyElevated,
                                unfocusedContainerColor = NavyCard,
                            ),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            minLines = 2,
                            maxLines = 3
                        )
                        Text(
                            "${description.length}/115",
                            color = if (description.length >= 115) ErrorRed else TextSubtle,
                            fontSize = 11.sp,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // Scoring System Section
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Sistema de Pontuação",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ScoreInput(
                                label = "🎯 Placar Exato",
                                value = pointsExact,
                                onValueChange = { pointsExact = it },
                                modifier = Modifier.weight(1f)
                            )
                            ScoreInput(
                                label = "✅ Resultado Certo",
                                value = pointsWinner,
                                onValueChange = { pointsWinner = it },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp)
                    }

                    // Info chip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Gold.copy(alpha = 0.08f))
                            .border(1.dp, Gold.copy(alpha = 0.2f), RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💡", fontSize = 14.sp)
                        Text(
                            "Após criar, você receberá um código de 6 caracteres para convidar amigos.",
                            fontSize = 12.sp,
                            color = Gold.copy(alpha = 0.8f),
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                BolaoButton(
                    text = "Criar Bolão",
                    isLoading = uiState.isLoading,
                    enabled = isFormValid && !uiState.isLoading,
                    onClick = { viewModel.create(name, description, selectedChampionshipId, pointsExact, pointsWinner) }
                )

                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun ScoreInput(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(NavyCard)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Text("-", color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = if (value == 1) "ponto" else "pontos",
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(36.dp)
            ) {
                Text("+", color = Neon, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
