package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoConfirmDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoFullScreenLoading
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.theme.TextSubtle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.compose.koinInject

@Immutable
data class EditBolaoUiState(
    val bolao: Bolao? = null,
    val participants: List<com.lpstudio.bolaodagalera.domain.model.User> = emptyList(),
    val isLoading: Boolean = false,
    val isDeleted: Boolean = false,
    val showSuccessMessage: Boolean = false,
    val error: String? = null
)

class EditBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
    private val bolaoId: String,
    private val crashReporter: CrashReporter
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditBolaoUiState())
    val uiState: StateFlow<EditBolaoUiState> = _uiState.asStateFlow()

    private val _isKnockoutStarted = MutableStateFlow(false)
    val isKnockoutStarted: StateFlow<Boolean> = _isKnockoutStarted.asStateFlow()

    val currentUserId = authRepository.currentUser?.id

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val bolao = bolaoRepository.getBolao(bolaoId)
                val participants = authRepository.getUsers(bolao.participants)
                _uiState.update {
                    it.copy(
                        bolao = bolao,
                        participants = participants,
                        isLoading = false
                    )
                }

                // Check knockout status for this specific championship
                matchRepository.getMatches(bolao.championshipId).collect { matches ->
                    val now = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis()
                    val knockoutStarted =
                        matches.any {
                            it.phase != com.lpstudio.bolaodagalera.domain.model.Phase.GROUP_STAGE &&
                                it.phase != com.lpstudio.bolaodagalera.domain.model.Phase.FRIENDLIES &&
                                (it.isFinished || now >= it.matchDateMillis)
                        }
                    _isKnockoutStarted.value = knockoutStarted
                }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao carregar dados do bolão")
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun update(name: String, description: String, scope: BolaoScope, pointsExact: Int, pointsWinner: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                bolaoRepository.updateBolao(bolaoId, name, description, scope, pointsExact, pointsWinner)
                val updatedBolao = bolaoRepository.getBolao(bolaoId)
                _uiState.update { it.copy(bolao = updatedBolao, isLoading = false, showSuccessMessage = true) }
                kotlinx.coroutines.delay(3000)
                _uiState.update { it.copy(showSuccessMessage = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao atualizar bolão")
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun delete() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                withTimeout(10000) {
                    bolaoRepository.deleteBolao(bolaoId)
                }
                _uiState.update { it.copy(isDeleted = true, isLoading = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao excluir bolão")
                _uiState.update { it.copy(error = e.message ?: "Erro desconhecido ao excluir", isLoading = false) }
            }
        }
    }

    fun removeParticipant(userId: String) {
        viewModelScope.launch {
            try {
                bolaoRepository.removeParticipant(bolaoId, userId)
                // Refresh data
                val bolao = bolaoRepository.getBolao(bolaoId)
                val participants = authRepository.getUsers(bolao.participants)
                _uiState.update { it.copy(bolao = bolao, participants = participants) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao remover participante")
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBolaoScreen(
    bolaoId: String,
    onNavigateToAddParticipants: (String) -> Unit,
    onBolaoDeleted: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val bolaoRepository = koinInject<BolaoRepository>()
    val authRepository = koinInject<AuthRepository>()
    val matchRepository = koinInject<MatchRepository>()
    val crashReporter = koinInject<CrashReporter>()
    val viewModel = remember(bolaoId) { EditBolaoViewModel(bolaoRepository, authRepository, matchRepository, bolaoId, crashReporter) }
    val uiState by viewModel.uiState.collectAsState()
    val isKnockoutStarted by viewModel.isKnockoutStarted.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(BolaoScope.FULL) }
    var pointsExact by remember { mutableIntStateOf(3) }
    var pointsWinner by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var participantToRemove by remember { mutableStateOf<com.lpstudio.bolaodagalera.domain.model.User?>(null) }

    val nameError = if (name.isNotBlank() && name.trim().length < 10) "Mínimo 10 caracteres" else null
    val isFormValid = name.trim().length in 10..35

    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            snackbarHostState.showSnackbar("Configurações salvas com sucesso!")
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar("Erro: $it")
        }
    }

    LaunchedEffect(uiState.bolao) {
        uiState.bolao?.let {
            name = it.name
            description = it.description
            selectedScope = it.scope
            pointsExact = it.pointsExactScore
            pointsWinner = it.pointsWinnerOrDraw
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) onBolaoDeleted()
    }

    if (showDeleteDialog) {
        BolaoConfirmDialog(
            title = "Excluir Bolão?",
            message = "Esta ação não pode ser desfeita. Todos os participantes e palpites serão removidos.",
            confirmText = "Excluir",
            isDestructive = true,
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    participantToRemove?.let { user ->
        BolaoConfirmDialog(
            title = "Remover Participante?",
            message = "Tem certeza que deseja remover ${user.name} deste bolão? Ele perderá todos os palpites feitos.",
            confirmText = "Remover",
            isDestructive = true,
            onConfirm = {
                viewModel.removeParticipant(user.id)
                participantToRemove = null
            },
            onDismiss = { participantToRemove = null }
        )
    }

    Scaffold(
        containerColor = DeepNavy,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BolaoTopBar(
                title = "Configurações",
                onNavigateBack = onNavigateBack,
                actions = {
                    val isOwner = viewModel.currentUserId == uiState.bolao?.ownerId
                    if (isOwner) {
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(Icons.Default.Delete, "Excluir", tint = ErrorRed)
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.bolao == null) {
            BolaoFullScreenLoading()
        } else {
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                // Basic Info Section
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("DADOS GERAIS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        BolaoTextField(
                            value = name,
                            onValueChange = { if (it.length <= 35) name = it },
                            label = "Nome do Bolão",
                            isError = nameError != null
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            if (nameError != null) {
                                Text(nameError, color = ErrorRed, fontSize = 10.sp)
                            } else {
                                Spacer(Modifier.width(1.dp))
                            }
                            Text(
                                "${name.length}/35",
                                color = if (name.length < 10 || name.length > 35) ErrorRed else TextSubtle,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        OutlinedTextField(
                            value = description,
                            onValueChange = { if (it.length <= 115) description = it },
                            label = { Text("Descrição", color = TextMuted) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Neon,
                                unfocusedBorderColor = GlassBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = NavyElevated,
                                unfocusedContainerColor = NavyCard
                            )
                        )
                        Text(
                            "${description.length}/115",
                            color = if (description.length >= 115) ErrorRed else TextSubtle,
                            fontSize = 10.sp,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                            textAlign = TextAlign.End
                        )
                    }
                }

                // Scope Section
                uiState.bolao?.let { originalBolao ->
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("TIPO DO BOLÃO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)

                        val championship = Championship.fromId(originalBolao.championshipId)
                        val isOnlyGroups = originalBolao.scope == BolaoScope.ONLY_GROUPS
                        val isFull = originalBolao.scope == BolaoScope.FULL
                        val isLeague = originalBolao.scope == BolaoScope.PONTOS_CORRIDOS || championship.isPointsBased
                        val canEditScope = (isOnlyGroups || (isFull && !isKnockoutStarted)) && !isLeague && championship.isGroupsAndKnockout

                        Surface(
                            modifier =
                            Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (canEditScope) Neon.copy(alpha = 0.5f) else GlassBorder.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                ),
                            color = NavyCard
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        when {
                                            championship.isPointsBased -> "📈"
                                            selectedScope == BolaoScope.FULL -> "🏆"
                                            selectedScope == BolaoScope.ONLY_GROUPS -> "⚽"
                                            selectedScope == BolaoScope.ONLY_KNOCKOUT -> "⚔️"
                                            else -> "🏆"
                                        },
                                        fontSize = 18.sp
                                    )
                                    Text(
                                        if (championship.isPointsBased) "Pontos Corridos" else selectedScope.label,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (canEditScope) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Neon.copy(alpha = 0.1f))
                                            .clickable {
                                                selectedScope = if (selectedScope == BolaoScope.ONLY_GROUPS) {
                                                    BolaoScope.FULL
                                                } else {
                                                    BolaoScope.ONLY_GROUPS
                                                }
                                            }
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            "Incluir fase de Mata-Mata",
                                            color = Neon,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Switch(
                                            checked = selectedScope == BolaoScope.FULL,
                                            onCheckedChange = {
                                                selectedScope = if (it) BolaoScope.FULL else BolaoScope.ONLY_GROUPS
                                            },
                                            colors =
                                            SwitchDefaults.colors(
                                                checkedThumbColor = Neon,
                                                checkedTrackColor = Neon.copy(alpha = 0.3f)
                                            )
                                        )
                                    }
                                } else {
                                    val labelText =
                                        when {
                                            championship.isPointsBased -> "Este campeonato segue o formato de pontos corridos."
                                            isFull && isKnockoutStarted -> "O Mata-Mata já está em andamento e não pode ser removido."
                                            else -> "O tipo deste bolão não pode ser alterado."
                                        }
                                    Text(
                                        labelText,
                                        fontSize = 10.sp,
                                        color = TextSubtle,
                                        modifier = Modifier.padding(top = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Scoring System Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("SISTEMA DE PONTUAÇÃO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)

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

                    // Info sobre prorrogação e pênaltis
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("⏱️", fontSize = 14.sp)
                            Text(
                                "O placar válido é o do tempo normal + prorrogação. Pênaltis não contam para a pontuação.",
                                fontSize = 11.sp,
                                color = TextMuted,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    BolaoButton(
                        text = "Salvar Alterações",
                        isLoading = uiState.isLoading,
                        enabled = isFormValid && !uiState.isLoading,
                        modifier = Modifier.padding(top = 4.dp),
                        onClick = { viewModel.update(name, description, selectedScope, pointsExact, pointsWinner) }
                    )
                }

                // Participants Section
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("PARTICIPANTES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted, letterSpacing = 1.sp)
                        TextButton(
                            onClick = { onNavigateToAddParticipants(bolaoId) },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Neon)
                            Spacer(Modifier.width(4.dp))
                            Text("Adicionar", color = Neon, fontSize = 13.sp)
                        }
                    }

                    val sortedParticipants = uiState.participants.sortedBy { it.name.lowercase() }

                    sortedParticipants.forEach { participant ->
                        val isOwner = participant.id == uiState.bolao?.ownerId
                        val isSelf = participant.id == viewModel.currentUserId

                        Surface(
                            color = NavyCard,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isOwner) Neon else NavyElevated),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (isOwner) "👑" else "👤", fontSize = 14.sp)
                                }
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text =
                                        when {
                                            isOwner && isSelf -> "${participant.name} (Você/Dono)"
                                            isSelf -> "${participant.name} (Você)"
                                            else -> participant.name
                                        },
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (participant.nickname.isNotBlank()) {
                                        Text(
                                            text = "@${participant.nickname.lowercase()}",
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                if (!isOwner && viewModel.currentUserId == uiState.bolao?.ownerId) {
                                    IconButton(onClick = { participantToRemove = participant }) {
                                        Icon(Icons.Default.Delete, "Remover", tint = ErrorRed.copy(alpha = 0.7f))
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))
                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun ScoreInput(label: String, value: Int, onValueChange: (Int) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, fontSize = 12.sp, color = TextMuted)
        Row(
            modifier =
            Modifier
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
