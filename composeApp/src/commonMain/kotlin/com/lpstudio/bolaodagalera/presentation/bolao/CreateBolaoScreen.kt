package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.BolaoTextField
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.GlassWhite
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.GradientBg
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.presentation.theme.TextSubtle
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Immutable
data class CreateBolaoUiState(val isLoading: Boolean = false, val createdBolao: Bolao? = null, val error: String? = null)

class CreateBolaoViewModel(
    private val bolaoRepository: BolaoRepository,
    private val authRepository: AuthRepository,
    private val matchRepository: MatchRepository,
    private val crashReporter: CrashReporter,
    private val performanceMonitor: PerformanceMonitor
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreateBolaoUiState())
    val uiState: StateFlow<CreateBolaoUiState> = _uiState.asStateFlow()

    private val _allMatches = MutableStateFlow<List<Match>>(emptyList())
    val allMatches: StateFlow<List<Match>> = _allMatches.asStateFlow()

    init {
        loadMatchesData()
    }

    private fun loadMatchesData() {
        viewModelScope.launch {
            matchRepository.getAllMatches().collect { matches ->
                _allMatches.value = matches
            }
        }
    }

    fun isPhaseAvailable(championshipId: String, phase: Phase): Boolean {
        val matches = _allMatches.value.filter { it.championshipId == championshipId && it.phase == phase }
        if (matches.isEmpty()) return false

        val now = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis()
        // A fase está disponível se nenhum jogo começou ainda
        return matches.all { it.matchDateMillis > now }
    }

    fun isKnockoutAvailable(championshipId: String): Boolean {
        val matches =
            _allMatches.value.filter {
                it.championshipId == championshipId &&
                    it.phase != Phase.GROUP_STAGE &&
                    it.phase != Phase.FRIENDLIES
            }
        if (matches.isEmpty()) return false

        val now = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis()
        // O mata-mata está disponível se nenhum jogo dele começou ainda
        return matches.all { it.matchDateMillis > now }
    }

    fun create(
        name: String,
        description: String,
        championshipId: String,
        scope: BolaoScope,
        specificMatchId: String?,
        pointsExact: Int,
        pointsWinner: Int
    ) {
        val userId = authRepository.currentUser?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val bolao =
                    performanceMonitor.trace("create_bolao") {
                        bolaoRepository.createBolao(
                            name.trim(),
                            description.trim(),
                            userId,
                            championshipId,
                            scope = scope,
                            specificMatchId = specificMatchId,
                            pointsExactScore = pointsExact,
                            pointsWinnerOrDraw = pointsWinner
                        )
                    }
                _uiState.update { it.copy(createdBolao = bolao, isLoading = false) }
            } catch (e: Exception) {
                crashReporter.recordException(e, "Erro ao criar bolão")
                _uiState.update { it.copy(error = e.message ?: "Erro ao criar bolão", isLoading = false) }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateBolaoScreen(onCreated: (String) -> Unit, onNavigateToAddParticipants: (String) -> Unit, onNavigateBack: () -> Unit) {
    val bolaoRepository = koinInject<BolaoRepository>()
    val authRepository = koinInject<AuthRepository>()
    val matchRepository = koinInject<MatchRepository>()
    val crashReporter = koinInject<CrashReporter>()
    val performanceMonitor = koinInject<PerformanceMonitor>()
    val viewModel =
        remember { CreateBolaoViewModel(bolaoRepository, authRepository, matchRepository, crashReporter, performanceMonitor) }
    val uiState by viewModel.uiState.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedChampionshipId by remember { mutableStateOf("UNKNOWN") }

    // Auto-selecionar o primeiro disponível quando carregar
    LaunchedEffect(Championship.getAll()) {
        if (selectedChampionshipId == "UNKNOWN") {
            selectedChampionshipId = Championship.getAll().find { it.isAvailable }?.id ?: "UNKNOWN"
        }
    }

    // Calcula disponibilidade reativamente
    val isGroupStageAvailable =
        remember(allMatches, selectedChampionshipId) {
            viewModel.isPhaseAvailable(selectedChampionshipId, Phase.GROUP_STAGE)
        }
    val isKnockoutAvailable =
        remember(allMatches, selectedChampionshipId) {
            viewModel.isKnockoutAvailable(selectedChampionshipId)
        }
    var selectedScope by remember { mutableStateOf(BolaoScope.FULL) }
    var selectedMatchId by remember { mutableStateOf<String?>(null) }

    // Ajuste inicial do scope baseado no campeonato selecionado
    LaunchedEffect(selectedChampionshipId) {
        val championship = Championship.fromId(selectedChampionshipId)
        when {
            championship.isPointsBased -> {
                selectedScope = BolaoScope.PONTOS_CORRIDOS
                selectedMatchId = null
            }
            !championship.isGroupsAndKnockout -> {
                // Se não tem a mistura (ex: apenas mata-mata como Copa do Brasil)
                selectedScope = BolaoScope.ONLY_KNOCKOUT
                selectedMatchId = null
            }
            championship.isGroupsAndKnockout -> {
                if (!isGroupStageAvailable) {
                    selectedScope = BolaoScope.ONLY_KNOCKOUT
                } else {
                    selectedScope = BolaoScope.FULL
                }
                selectedMatchId = null
            }
        }
    }

    var pointsExact by remember { mutableIntStateOf(3) }
    var pointsWinner by remember { mutableIntStateOf(1) }

    var nameTouched by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val launcherProvider = rememberLauncherProvider()
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Auto-ajuste do scope se a fase de grupos ou mata-mata acabar/começar
    LaunchedEffect(isGroupStageAvailable, isKnockoutAvailable, selectedChampionshipId) {
        val champ = Championship.fromId(selectedChampionshipId)
        if (champ.isPointsBased) {
            selectedScope = BolaoScope.PONTOS_CORRIDOS
            return@LaunchedEffect
        }

        val isFullValid = isGroupStageAvailable && isKnockoutAvailable
        val isOnlyGroupsValid = isGroupStageAvailable
        val isOnlyKnockoutValid = isKnockoutAvailable

        when (selectedScope) {
            BolaoScope.FULL ->
                if (!isFullValid) {
                    selectedScope =
                        if (isOnlyGroupsValid) {
                            BolaoScope.ONLY_GROUPS
                        } else if (isOnlyKnockoutValid) {
                            BolaoScope.ONLY_KNOCKOUT
                        } else {
                            BolaoScope.FULL
                        }
                }
            BolaoScope.ONLY_GROUPS ->
                if (!isOnlyGroupsValid) {
                    selectedScope = if (isOnlyKnockoutValid) BolaoScope.ONLY_KNOCKOUT else BolaoScope.ONLY_GROUPS
                }
            BolaoScope.ONLY_KNOCKOUT ->
                if (!isOnlyKnockoutValid) {
                    selectedScope = if (isOnlyGroupsValid) BolaoScope.ONLY_GROUPS else BolaoScope.ONLY_KNOCKOUT
                }
            else -> {}
        }
    }

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
                        modifier =
                        Modifier
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
                            val inviteUrl = "https://bolaodagalera-bb002.web.app/invite?code=${bolao.code}"
                            launcherProvider.shareText(
                                "Entre no meu bolão '${bolao.name}'! 🏆\n\nLink: $inviteUrl\n\nCódigo: ${bolao.code}"
                            )
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
                        Text("Novo Bolão", fontWeight = FontWeight.Bold, color = Color.White)
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
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    windowInsets = WindowInsets(0, 0, 0, 0)
                )
            }
        ) { padding ->
            Column(
                modifier =
                Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .imePadding()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Hero section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("🏆", fontSize = 48.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Crie seu bolão e convide amigos com um código único",
                        fontSize = 13.sp,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(Modifier.height(20.dp))

                // Form card
                Column(
                    modifier =
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(GlassWhite)
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 20.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Campeonato
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            "Escolha o Campeonato",
                            fontSize = 12.sp,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        val championships = Championship.getAll()

                        championships.forEach { championship ->
                            val id = championship.id
                            val label = championship.displayName
                            val isAvailable = championship.isAvailable
                            val isSelected = selectedChampionshipId == id

                            Surface(
                                modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Neon else GlassBorder.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .alpha(if (isAvailable) 1f else 0.5f)
                                    .clickable(enabled = isAvailable) { selectedChampionshipId = id },
                                color = if (isSelected) NavyElevated else NavyCard.copy(alpha = 0.7f)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Column {
                                                Text(
                                                    label,
                                                    fontSize = 15.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                    color = if (isSelected) Color.White else TextMuted
                                                )
                                                if (!isAvailable) {
                                                    Text(
                                                        "Em breve",
                                                        fontSize = 10.sp,
                                                        color = Neon.copy(alpha = 0.7f),
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
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

                                    // Aqui é onde as opções (Radio Buttons) aparecem apenas para o selecionado
                                    val showScopeOptions = isSelected && championship.isGroupsAndKnockout

                                    if (showScopeOptions) {
                                        Spacer(Modifier.height(12.dp))
                                        HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                                        Spacer(Modifier.height(12.dp))

                                        Text(
                                            "Fases:",
                                            fontSize = 11.sp,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                            BolaoScope.entries
                                                .filter { scope ->
                                                    // Filtros de visibilidade do escopo baseados no campeonato e datas
                                                    when (scope) {
                                                        BolaoScope.ONLY_GROUPS ->
                                                            championship.isGroupsAndKnockout && isGroupStageAvailable
                                                        BolaoScope.ONLY_KNOCKOUT ->
                                                            (championship.isGroupsAndKnockout || !championship.isPointsBased) &&
                                                                isKnockoutAvailable
                                                        BolaoScope.FULL ->
                                                            championship.isGroupsAndKnockout &&
                                                                isGroupStageAvailable &&
                                                                isKnockoutAvailable
                                                        else -> true
                                                    }
                                                }
                                                .forEach { scope ->
                                                    val isScopeEnabled =
                                                        when (scope) {
                                                            BolaoScope.FULL -> isGroupStageAvailable && isKnockoutAvailable
                                                            BolaoScope.ONLY_GROUPS -> isGroupStageAvailable
                                                            BolaoScope.ONLY_KNOCKOUT -> isKnockoutAvailable
                                                            BolaoScope.PONTOS_CORRIDOS -> true
                                                        }
                                                    val isScopeSelected = selectedScope == scope && isScopeEnabled
                                                    val scopeEmoji =
                                                        when (scope) {
                                                            BolaoScope.FULL -> "🏆"
                                                            BolaoScope.ONLY_GROUPS -> "⚽"
                                                            BolaoScope.ONLY_KNOCKOUT -> "⚔️"
                                                            BolaoScope.PONTOS_CORRIDOS -> "📈"
                                                        }

                                                    Row(
                                                        modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clip(RoundedCornerShape(10.dp))
                                                            .background(
                                                                if (isScopeSelected) Neon.copy(alpha = 0.1f) else Color.Transparent
                                                            )
                                                            .border(
                                                                1.dp,
                                                                if (isScopeSelected) {
                                                                    Neon.copy(
                                                                        alpha = 0.5f
                                                                    )
                                                                } else {
                                                                    GlassBorder.copy(alpha = 0.5f)
                                                                },
                                                                RoundedCornerShape(10.dp)
                                                            )
                                                            .clickable(enabled = isScopeEnabled) {
                                                                selectedScope = scope
                                                                selectedMatchId = null
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            scopeEmoji,
                                                            fontSize = 14.sp,
                                                            modifier = Modifier.alpha(if (isScopeEnabled) 1f else 0.3f)
                                                        )
                                                        Spacer(Modifier.width(12.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            Text(
                                                                scope.label,
                                                                fontSize = 13.sp,
                                                                color = if (isScopeSelected) Color.White else TextMuted,
                                                                fontWeight = if (isScopeSelected) FontWeight.Bold else FontWeight.Normal,
                                                                modifier = Modifier.alpha(if (isScopeEnabled) 1f else 0.3f)
                                                            )

                                                            val errorMsg =
                                                                when {
                                                                    (scope == BolaoScope.FULL || scope == BolaoScope.ONLY_GROUPS) &&
                                                                        !isGroupStageAvailable -> "(Fase encerrada)"
                                                                    scope == BolaoScope.ONLY_KNOCKOUT && !isScopeEnabled ->
                                                                        "(Mata-mata encerrado)"
                                                                    else -> null
                                                                }

                                                            if (errorMsg != null) {
                                                                Text(
                                                                    errorMsg,
                                                                    fontSize = 10.sp,
                                                                    color = ErrorRed.copy(alpha = 0.7f),
                                                                    lineHeight = 12.sp
                                                                )
                                                            }
                                                        }

                                                        RadioButton(
                                                            enabled = isScopeEnabled,
                                                            selected = isScopeSelected,
                                                            onClick = {
                                                                if (isScopeEnabled) {
                                                                    selectedScope = scope
                                                                    selectedMatchId = null
                                                                }
                                                            },
                                                            colors =
                                                            RadioButtonDefaults.colors(
                                                                selectedColor = Neon,
                                                                unselectedColor = TextMuted
                                                            ),
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                    }
                                                }
                                        }
                                    }
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
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
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
                            colors =
                            OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Neon,
                                unfocusedBorderColor = Color(0xFF2A3D55),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Neon,
                                focusedContainerColor = NavyElevated,
                                unfocusedContainerColor = NavyCard
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
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
                            textAlign = TextAlign.End
                        )
                    }

                    // Scoring System Section
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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

                        // Info sobre prorrogação e pênaltis
                        Surface(
                            color = Color.White.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(10.dp),
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
                    }

                    uiState.error?.let {
                        Text(it, color = ErrorRed, fontSize = 12.sp)
                    }

                    // Info chip
                    Row(
                        modifier =
                        Modifier
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

                Spacer(Modifier.height(16.dp))

                BolaoButton(
                    text = "Criar Bolão",
                    isLoading = uiState.isLoading,
                    enabled = isFormValid && !uiState.isLoading,
                    onClick = {
                        viewModel.create(
                            name,
                            description,
                            selectedChampionshipId,
                            selectedScope,
                            selectedMatchId,
                            pointsExact,
                            pointsWinner
                        )
                    }
                )

                Spacer(Modifier.height(32.dp))
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
