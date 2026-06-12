package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.presentation.ranking.RankingScreen
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolaoDetailScreen(
    bolaoId: String,
    onNavigateToPrediction: (matchId: String) -> Unit,
    onNavigateToAllPredictions: (matchId: String) -> Unit,
    onNavigateToEdit: (bolaoId: String) -> Unit,
    onNavigateToAddParticipants: (bolaoId: String) -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: BolaoViewModel = koinInject(parameters = { parametersOf(bolaoId) })
    val uiState by viewModel.uiState.collectAsState()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val userId = authRepository.currentUser?.id ?: ""
    val launcherProvider = com.lpstudio.bolaodagalera.rememberLauncherProvider()

    LaunchedEffect(userId) { viewModel.setUserId(userId) }
    
    // Navegar de volta se o usuário sair do bolão com sucesso
    LaunchedEffect(uiState.isLeaveSuccess) {
        if (uiState.isLeaveSuccess) {
            onNavigateBack()
        }
    }

    BolaoDetailContent(
        bolaoId = bolaoId,
        uiState = uiState,
        userId = userId,
        isOwner = uiState.bolao?.ownerId == userId,
        launcherProvider = launcherProvider,
        onLeaveBolao = { viewModel.leaveBolao() },
        onNavigateToPrediction = onNavigateToPrediction,
        onNavigateToAllPredictions = onNavigateToAllPredictions,
        onNavigateToEdit = onNavigateToEdit,
        onNavigateToAddParticipants = onNavigateToAddParticipants,
        onAdminUpdateScore = { matchId, home, away ->
            viewModel.updateMatchScore(matchId, home, away)
        },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolaoDetailContent(
    bolaoId: String,
    uiState: BolaoUiState,
    userId: String,
    isOwner: Boolean,
    launcherProvider: com.lpstudio.bolaodagalera.LauncherProvider,
    onLeaveBolao: () -> Unit,
    onNavigateToPrediction: (matchId: String) -> Unit,
    onNavigateToAllPredictions: (matchId: String) -> Unit,
    onNavigateToEdit: (bolaoId: String) -> Unit,
    onNavigateToAddParticipants: (bolaoId: String) -> Unit,
    onAdminUpdateScore: (matchId: String, home: Int, away: Int) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    
    // Estados persistentes no nível da tela, agora vinculados ao bolaoId para resetar ao trocar de bolão
    val groupsListState = rememberLazyListState()
    val knockoutListState = rememberLazyListState()
    val expandedGroups = rememberSaveable(
        bolaoId,
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<String>() }

    var matchToUpdate by remember { mutableStateOf<Match?>(null) }

    if (matchToUpdate != null) {
        AdminScoreDialog(
            match = matchToUpdate!!,
            onDismiss = { matchToUpdate = null },
            onConfirm = { h: Int, a: Int ->
                onAdminUpdateScore(matchToUpdate!!.id, h, a)
                matchToUpdate = null
            }
        )
    }

    var selectedRound by rememberSaveable { mutableIntStateOf(1) }
    var selectedPhase by rememberSaveable { mutableStateOf<Phase?>(null) }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = NavyCard,
            title = { Text("Sair do Bolão?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { Text("Você perderá seus palpites e sua posição no ranking deste bolão.", color = TextMuted) },
            confirmButton = {
                TextButton(onClick = { 
                    showLeaveDialog = false
                    onLeaveBolao()
                }) {
                    Text("Sair", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLeaveDialog = false }) {
                    Text("Cancelar", color = TextMuted)
                }
            }
        )
    }

    // ── Participants Dialog ──────────────────────────────────────────────────
    if (showParticipantsSheet) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showParticipantsSheet = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.95f)
                    .fillMaxHeight(0.7f),
                color = NavyCard,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                ) {
                    Text(
                        "Participantes",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    Text(
                        "${uiState.participants.size} pessoas no bolão",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    val sortedParticipants = uiState.participants.sortedBy { it.userName.lowercase() }
                    
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            items(sortedParticipants) { participant ->
                                val isOwnerParticipant = participant.userId == uiState.bolao?.ownerId

                                Surface(
                                    color = NavyElevated,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            initials = participant.userName.getInitials(),
                                            size = 40.dp,
                                            fontSize = 14.sp,
                                            isOwner = isOwnerParticipant,
                                            borderColor = if (isOwnerParticipant) Gold else Neon.copy(alpha = 0.5f)
                                        )
                                        
                                        Spacer(Modifier.width(14.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = participant.userName,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            if (participant.userNickname.isNotBlank()) {
                                                Text(
                                                    text = "@${participant.userNickname.lowercase()}",
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Sombra/Blur no topo para indicar scroll
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(Brush.verticalGradient(listOf(NavyCard, Color.Transparent)))
                                .align(Alignment.TopCenter)
                        )

                        // Sombra/Blur na base para indicar scroll
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .background(Brush.verticalGradient(listOf(Color.Transparent, NavyCard)))
                                .align(Alignment.BottomCenter)
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = { showParticipantsSheet = false },
                            contentPadding = PaddingValues(horizontal = 16.dp)
                        ) {
                            Text("Fechar", color = Neon, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepNavy)
    ) {
        if (uiState.isLoading && uiState.matches.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Neon)
            }
        } else {
            Column(Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GradientHero)
                        .padding(top = 16.dp, bottom = 16.dp)
                ) {
                    Column(Modifier.padding(horizontal = 20.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp)) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Voltar",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Text(
                                uiState.bolao?.name ?: "Bolão",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    uiState.bolao?.let { bolao ->
                                        val inviteUrl = "https://bolaodagalera.app/invite?code=${bolao.code}"
                                        launcherProvider.shareText("Entre no meu bolão '${bolao.name}'! 🏆\n\nLink: $inviteUrl\n\nCódigo: ${bolao.code}")
                                    }
                                }, 
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = TextMuted, modifier = Modifier.size(20.dp))
                            }
                            
                            if (isOwner) {
                                IconButton(
                                    onClick = { onNavigateToAddParticipants(bolaoId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        contentDescription = "Adicionar Participantes",
                                        tint = Neon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { onNavigateToEdit(bolaoId) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar",
                                        tint = Neon,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            if (!isOwner) {
                                IconButton(
                                    onClick = { showLeaveDialog = true },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ExitToApp,
                                        contentDescription = "Sair do Bolão",
                                        tint = ErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        uiState.bolao?.let { bolao ->
                            if (bolao.description.isNotBlank()) {
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    bolao.description,
                                    fontSize = 12.sp,
                                    color = TextMuted,
                                    maxLines = 2,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                            }

                            // Alerta de Pedidos Pendentes para o Admin
                            if (isOwner && bolao.pendingParticipants.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Surface(
                                    color = Gold.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().clickable { /* Abrir lista de aprovação */ }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("⚠️", fontSize = 16.sp)
                                        Text(
                                            "${bolao.pendingParticipants.size} pessoas pediram para entrar no bolão.",
                                            color = Gold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("VER", color = Gold, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }

                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Code badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Gold.copy(alpha = 0.15f))
                                        .border(1.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text("🔑", fontSize = 12.sp)
                                    Text(
                                        bolao.code,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Gold,
                                        letterSpacing = 1.sp
                                    )
                                }
                                // Participants badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Neon.copy(alpha = 0.10f))
                                        .border(1.dp, Neon.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                        .clickable { showParticipantsSheet = true }
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text("👥", fontSize = 12.sp)
                                    Text(
                                        "${bolao.participants.size} participantes",
                                        fontSize = 12.sp,
                                        color = Neon,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                
                                // Championship badge
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White.copy(alpha = 0.05f))
                                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text("🏆", fontSize = 12.sp)
                                    Text(
                                        "Copa 2026",
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Pill tabs
                        val tabs = listOf("Grupos", "Mata-Mata", "Ranking")
                        
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NavyCard)
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tabs.forEachIndexed { index, label ->
                                val selected = selectedTab == index
                                val bg by animateColorAsState(
                                    if (selected) Neon else Color.Transparent,
                                    animationSpec = tween(200),
                                    label = "tab_bg_$index"
                                )
                                val textColor by animateColorAsState(
                                    if (selected) DeepNavy else TextMuted,
                                    animationSpec = tween(200),
                                    label = "tab_text_$index"
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(bg)
                                        .clickable { selectedTab = index }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        label,
                                        color = textColor,
                                        fontSize = 14.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                // ── Tab content ───────────────────────────────────────────────────
                val filteredMatches = remember(uiState.matches) {
                    uiState.matches.filter { it.phase != Phase.FRIENDLIES }
                }

                val groupMatches = remember(filteredMatches) { 
                    filteredMatches.filter { it.phase == Phase.GROUP_STAGE } 
                }

                when (selectedTab) {
                    0 -> GroupStageTab(
                        matches = groupMatches,
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isOwner,
                        selectedRound = selectedRound,
                        onRoundChange = { selectedRound = it },
                        listState = groupsListState,
                        expandedGroups = expandedGroups,
                        onMatchClick = { onNavigateToPrediction(it) },
                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                        onAdminUpdateScore = { matchToUpdate = it }
                    )
                    1 -> KnockoutTab(
                        matches = filteredMatches,
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isOwner,
                        selectedPhase = selectedPhase,
                        onPhaseChange = { selectedPhase = it },
                        listState = knockoutListState,
                        onMatchClick = { onNavigateToPrediction(it) },
                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                        onAdminUpdateScore = { matchToUpdate = it }
                    )
                    2 -> RankingScreen(bolaoId = bolaoId)
                }
            }
        }
    }
}

// Infere a rodada de um jogo de grupo a partir do ID (GS-X-1..6)
private fun Match.groupRound(): Int {
    val n = id.substringAfterLast("-").toIntOrNull() ?: return 0
    return when (n) { 1, 2 -> 1; 3, 4 -> 2; 5, 6 -> 3; else -> 0 }
}

// Rodadas da fase de grupos agora estão todas liberadas por padrão
private fun unlockedRounds(groupMatches: List<Match>): Set<Int> = setOf(1, 2, 3)

// ── Aba Fase de Grupos ────────────────────────────────────────────────────────

@Composable
private fun GroupStageTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    selectedRound: Int,
    onRoundChange: (Int) -> Unit,
    listState: LazyListState,
    expandedGroups: SnapshotStateList<String>,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onAdminUpdateScore: (Match) -> Unit,
    showRoundSelector: Boolean = true
) {
    val unlocked = remember(matches) { unlockedRounds(matches) }
    
    val roundMatches = remember(matches, selectedRound, showRoundSelector) {
        if (showRoundSelector) {
            matches.filter { it.groupRound() == selectedRound }
        } else {
            matches
        }
    }
    val byGroup = remember(roundMatches) {
        roundMatches.groupBy { it.group ?: "" }
    }

    // Auto-expandir inteligente: Sempre os próximos jogos a acontecer ou jogos AO VIVO
    LaunchedEffect(selectedRound, matches.isNotEmpty(), byGroup.keys) {
        if (matches.isEmpty()) return@LaunchedEffect
        
        val timezone = TimeZone.currentSystemDefault()
        val now = TimeSource.nowMillis()
        val twoHours = 2 * 60 * 60 * 1000L
        
        // Limpa expansões antigas ao trocar de rodada
        expandedGroups.clear()

        // 1. Identifica grupos com jogos AO VIVO (Iniciados há menos de 2h)
        val liveGroups = byGroup.filter { (_, gMatches) ->
            gMatches.any { now in it.matchDateMillis..(it.matchDateMillis + twoHours) }
        }.keys
        
        if (liveGroups.isNotEmpty()) {
            expandedGroups.addAll(liveGroups)
        }

        // 2. Encontra a data do próximo jogo que ainda não começou (ou o primeiro do dia se todos acabaram)
        val nextMatchDate = matches
            .filter { now < it.matchDateMillis }
            .minOfOrNull { it.matchDateMillis }

        if (nextMatchDate != null) {
            // Define o início do dia desse próximo jogo
            val nextMatchDayStart = Instant.fromEpochMilliseconds(nextMatchDate)
                .toLocalDateTime(timezone).date.atStartOfDayIn(timezone).toEpochMilliseconds()
            
            // Define o fim do dia seguinte (D+1) para pegar todos os jogos dessa "rodada diária"
            val windowEnd = nextMatchDayStart + (48 * 60 * 60 * 1000)

            // Abre todos os grupos que possuem jogos nesse intervalo (dia do próximo jogo + dia seguinte)
            val nextGroups = byGroup.filter { (_, gMatches) ->
                gMatches.any { it.matchDateMillis in nextMatchDayStart until windowEnd }
            }.keys
            
            nextGroups.forEach { if (!expandedGroups.contains(it)) expandedGroups.add(it) }
        } else if (expandedGroups.isEmpty()) {
            // Se não tem nada ao vivo nem no futuro, expande todos os grupos que têm jogos na rodada atual
            expandedGroups.addAll(byGroup.keys)
        }
    }

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            if (showRoundSelector) {
                item(key = "round-selector", contentType = "selector") {
                    RodadaSelector(
                        selected = selectedRound,
                        unlocked = unlocked,
                        onSelect = { if (it in unlocked) onRoundChange(it) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            byGroup.entries.sortedBy { it.key }.forEach { (group, groupMatches) ->
                val isExpanded = expandedGroups.contains(group)
                val isCompleted = groupMatches.all { predictions.containsKey(it.id) }
                
                item(key = "header-$group", contentType = "header") {
                    GroupHeader(
                        group = group,
                        isExpanded = isExpanded,
                        isCompleted = isCompleted,
                        enabled = true, // Sempre liberado para abrir
                        onToggle = { 
                            if (isExpanded) expandedGroups.remove(group) else expandedGroups.add(group)
                        }
                    )
                }

                items(groupMatches, key = { it.id }, contentType = { "match" }) { match ->
                    AnimatedVisibility(
                        visible = isExpanded,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        Column {
                            MatchCard(
                                match = match,
                                prediction = predictions[match.id],
                                isAdmin = isAdmin,
                                onClick = { onMatchClick(match.id) },
                                onShowAllPredictions = { onShowAllPredictions(match) },
                                onAdminUpdateScore = { onAdminUpdateScore(match) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                }
                item(key = "spacer-$group", contentType = "spacer") { Spacer(Modifier.height(4.dp)) }
            }
        }
        
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = Neon, trackColor = Color.Transparent
            )
        }
    }
}

// ── Aba Mata-Mata ─────────────────────────────────────────────────────────────

@Composable
private fun KnockoutTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    selectedPhase: Phase?,
    onPhaseChange: (Phase) -> Unit,
    listState: LazyListState,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onAdminUpdateScore: (Match) -> Unit
) {
    if (isLoading && matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Neon, strokeWidth = 2.dp)
        }
        return
    }

    val phaseOrder = remember(matches) {
        listOf(
            Phase.ROUND_OF_32, Phase.ROUND_OF_16,
            Phase.QUARTERFINALS, Phase.SEMIFINALS, Phase.THIRD_PLACE, Phase.FINAL
        ).filter { phase -> matches.any { it.phase == phase } }
    }

    // Auto-selecionar fase com jogos hoje ou próximos
    LaunchedEffect(matches) {
        if (selectedPhase != null) return@LaunchedEffect
        
        val knockoutMatches = matches.filter { it.phase != Phase.GROUP_STAGE }
        if (knockoutMatches.isEmpty()) return@LaunchedEffect
        
        val timezone = TimeZone.currentSystemDefault()
        val todayStart = Instant.fromEpochMilliseconds(TimeSource.nowMillis()).toLocalDateTime(timezone).date.atStartOfDayIn(timezone).toEpochMilliseconds()
        val todayEnd = todayStart + (24 * 60 * 60 * 1000)

        // 1. Tenta achar fase com jogo HOJE
        val todayPhase = knockoutMatches.find { 
            it.matchDateMillis in todayStart..todayEnd 
        }?.phase

        if (todayPhase != null) {
            onPhaseChange(todayPhase)
        } else {
            // 2. Tenta achar a próxima fase com jogos não finalizados
            val nextPhase = phaseOrder.find { phase ->
                knockoutMatches.any { it.phase == phase && !it.isFinished }
            }
            if (nextPhase != null) onPhaseChange(nextPhase)
        }
    }

    val timezone = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()

    // O Mata-mata libera no início do dia do último jogo da Fase de Grupos
    val groupStageLastMatchDay = matches
        .filter { it.phase == Phase.GROUP_STAGE }
        .maxOfOrNull { it.matchDateMillis }
        ?.let { millis ->
            Instant.fromEpochMilliseconds(millis)
                .toLocalDateTime(timezone)
                .date
                .atStartOfDayIn(timezone)
                .toEpochMilliseconds()
        } ?: 0L

    val isKnockoutUnlocked = now >= groupStageLastMatchDay

    Box(Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (phaseOrder.isNotEmpty()) {
                item {
                    val currentPhase = selectedPhase ?: phaseOrder.first()
                    KnockoutPhaseSelector(
                        phases = phaseOrder,
                        selected = currentPhase,
                        isUnlocked = isKnockoutUnlocked,
                        onSelect = { if (isKnockoutUnlocked) onPhaseChange(it) }
                    )
                    Spacer(Modifier.height(8.dp))
                }

                val currentPhase = selectedPhase ?: phaseOrder.first()
                val phaseMatches = matches.filter { it.phase == currentPhase }
                
                // Agrupa as partidas em pares para desenhar a conexão do chaveamento
                val pairs = phaseMatches.chunked(2)
                
                items(pairs.size) { index ->
                    val pair = pairs[index]
                    val m1 = pair[0]
                    val m2 = pair.getOrNull(1)
                    
                    KnockoutBracketPair(
                        match1 = m1,
                        match2 = m2,
                        prediction1 = predictions[m1.id],
                        prediction2 = m2?.let { predictions[it.id] },
                        isAdmin = isAdmin,
                        forceLocked = !isKnockoutUnlocked,
                        onMatchClick = onMatchClick,
                        onShowAllPredictions = onShowAllPredictions,
                        onAdminUpdateScore = onAdminUpdateScore
                    )
                    
                    if (index < pairs.size - 1) {
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
        
        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                color = Neon,
                trackColor = Color.Transparent
            )
        }
    }
}

@Composable
private fun KnockoutBracketPair(
    match1: Match,
    match2: Match?,
    prediction1: Prediction?,
    prediction2: Prediction?,
    isAdmin: Boolean,
    forceLocked: Boolean,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onAdminUpdateScore: (Match) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            MatchCard(
                match = match1,
                prediction = prediction1,
                isAdmin = isAdmin,
                forceLocked = forceLocked,
                onClick = { onMatchClick(match1.id) },
                onShowAllPredictions = { onShowAllPredictions(match1) },
                onAdminUpdateScore = { onAdminUpdateScore(match1) }
            )
            
            if (match2 != null) {
                Spacer(Modifier.height(12.dp))
                MatchCard(
                    match = match2,
                    prediction = prediction2,
                    isAdmin = isAdmin,
                    forceLocked = forceLocked,
                    onClick = { onMatchClick(match2.id) },
                    onShowAllPredictions = { onShowAllPredictions(match2) },
                    onAdminUpdateScore = { onAdminUpdateScore(match2) }
                )
            }
        }

        if (match2 != null) {
            // Desenha a "chave" de conexão
            Box(
                modifier = Modifier
                    .width(32.dp)
                    .height(120.dp) // Altura aproximada para cobrir os dois cards + spacer
                    .padding(start = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val path = androidx.compose.ui.graphics.Path().apply {
                        // Linha superior (meio do 1º card)
                        moveTo(0f, h * 0.25f)
                        lineTo(w * 0.6f, h * 0.25f)
                        // Linha vertical de conexão
                        lineTo(w * 0.6f, h * 0.75f)
                        // Linha inferior (meio do 2º card)
                        lineTo(0f, h * 0.75f)
                        // Linha que sai para a próxima fase
                        moveTo(w * 0.6f, h * 0.5f)
                        lineTo(w, h * 0.5f)
                    }
                    drawPath(
                        path = path,
                        color = Neon.copy(alpha = 0.4f),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round,
                            join = androidx.compose.ui.graphics.StrokeJoin.Round
                        )
                    )
                    
                    // Ponto de conexão
                    drawCircle(
                        color = Neon,
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(w, h * 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KnockoutPhaseSelector(
    phases: List<Phase>,
    selected: Phase,
    isUnlocked: Boolean,
    onSelect: (Phase) -> Unit
) {
    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(phases) { phase ->
            val isSelected = selected == phase

            val borderColor by animateColorAsState(
                when {
                    isSelected && isUnlocked -> Neon
                    isUnlocked -> GlassBorder
                    else -> Color.Transparent
                },
                label = "border_${phase.name}"
            )

            val containerColor by animateColorAsState(
                when {
                    isSelected && isUnlocked -> Neon.copy(alpha = 0.12f)
                    isUnlocked -> NavyElevated
                    else -> NavyCard.copy(alpha = 0.5f)
                },
                label = "bg_${phase.name}"
            )

            val textColor by animateColorAsState(
                when {
                    isSelected && isUnlocked -> Neon
                    isUnlocked -> Color.White
                    else -> TextMuted.copy(alpha = 0.4f)
                },
                label = "text_${phase.name}"
            )

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerColor)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .then(if (isUnlocked) Modifier.clickable { onSelect(phase) } else Modifier)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isUnlocked) {
                        Text("🔒", fontSize = 10.sp)
                    }
                    Text(
                        phase.label,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun RodadaSelector(selected: Int, unlocked: Set<Int>, onSelect: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(1, 2, 3).forEach { round ->
            val isSelected = selected == round
            val isUnlocked = round in unlocked
            
            val borderColor by animateColorAsState(
                when {
                    isSelected -> Neon
                    isUnlocked -> GlassBorder
                    else -> Color.Transparent
                },
                label = "border_$round"
            )

            val containerColor by animateColorAsState(
                when {
                    isSelected -> Neon.copy(alpha = 0.12f)
                    isUnlocked -> NavyElevated
                    else -> NavyCard.copy(alpha = 0.5f)
                },
                label = "bg_$round"
            )

            val textColor by animateColorAsState(
                when {
                    isSelected -> Neon
                    isUnlocked -> Color.White
                    else -> TextMuted.copy(alpha = 0.4f)
                },
                label = "text_$round"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(containerColor)
                    .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                    .then(if (isUnlocked) Modifier.clickable { onSelect(round) } else Modifier)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!isUnlocked) {
                        Text(
                            "🔒",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 1.dp)
                        )
                    }
                    Text(
                        "Rodada $round",
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupHeader(
    group: String, 
    isExpanded: Boolean, 
    isCompleted: Boolean,
    enabled: Boolean = true,
    onToggle: () -> Unit
) {
    val chevronRotation by animateFloatAsState(
        targetValue = if (isExpanded) 90f else 0f,
        animationSpec = tween(200),
        label = "chevron_$group"
    )

    val borderColor by animateColorAsState(
        when {
            !enabled -> Color.Transparent
            isExpanded -> Neon.copy(alpha = 0.3f)
            else -> GlassBorder
        },
        label = "header_border_$group"
    )

    val bgBrush = when {
        !enabled -> Brush.linearGradient(listOf(NavyCard.copy(alpha = 0.5f), NavyCard.copy(alpha = 0.5f)))
        isExpanded -> Brush.linearGradient(listOf(Neon.copy(alpha = 0.08f), Neon.copy(alpha = 0.02f)))
        else -> Brush.linearGradient(listOf(NavyElevated, NavyCard))
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bgBrush)
                .border(1.dp, borderColor, RoundedCornerShape(16.dp))
                .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            when {
                                !enabled -> TextMuted.copy(alpha = 0.3f)
                                isCompleted -> Neon
                                else -> Color(0xFFFFC107)
                            }
                        )
                )
                Text(
                    "Grupo $group",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (enabled) Color.White else TextMuted.copy(alpha = 0.5f)
                )
                if (enabled) {
                    if (isCompleted) {
                        Text("✅", fontSize = 12.sp)
                    } else {
                        Text("⏳", fontSize = 12.sp)
                    }
                } else {
                    Text("🔒", fontSize = 10.sp, modifier = Modifier.padding(bottom = 1.dp))
                }
            }
            if (enabled) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = if (isExpanded) "Colapsar" else "Expandir",
                    tint = TextMuted,
                    modifier = Modifier
                        .size(20.dp)
                        .rotate(chevronRotation)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

private fun formatMatchDate(millis: Long): String {
    val dt = Instant.fromEpochMilliseconds(millis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val day = when (dt.dayOfWeek) {
        DayOfWeek.MONDAY    -> "Seg"
        DayOfWeek.TUESDAY   -> "Ter"
        DayOfWeek.WEDNESDAY -> "Qua"
        DayOfWeek.THURSDAY  -> "Qui"
        DayOfWeek.FRIDAY    -> "Sex"
        DayOfWeek.SATURDAY  -> "Sáb"
        else                -> "Dom"
    }
    val month = when (dt.month) {
        Month.JANUARY   -> "jan"; Month.FEBRUARY  -> "fev"; Month.MARCH     -> "mar"
        Month.APRIL     -> "abr"; Month.MAY        -> "mai"; Month.JUNE      -> "jun"
        Month.JULY      -> "jul"; Month.AUGUST     -> "ago"; Month.SEPTEMBER -> "set"
        Month.OCTOBER   -> "out"; Month.NOVEMBER   -> "nov"; else            -> "dez"
    }
    val h = dt.hour.toString().padStart(2, '0')
    val m = dt.minute.toString().padStart(2, '0')
    return "$day, ${dt.dayOfMonth} $month · $h:$m"
}

@Composable
fun MatchCard(
    match: Match,
    prediction: Prediction?,
    isAdmin: Boolean = false,
    forceLocked: Boolean = false,
    onShowAllPredictions: () -> Unit = {},
    onAdminUpdateScore: () -> Unit = {},
    onClick: () -> Unit
) {
    val hasPrediction = prediction != null
    val isFinished = match.homeScore != null && match.awayScore != null
    val now = TimeSource.nowMillis()
    val matchStart = match.matchDateMillis
    val matchEnd = matchStart + 7200_000L // 2 horas de duração
    
    // Um jogo está em andamento se tem placar MAS o tempo atual ainda está dentro da janela de 2h do início
    val isLive = isFinished && now in matchStart..matchEnd
    val isActuallyFinished = isFinished && now > matchEnd

    val canPredict = !isFinished && now < (match.matchDateMillis - 60_000) && !forceLocked

    val borderColor = when {
        isLive -> GlassBorder // Borda neutra idêntica ao card de palpite futuro
        isActuallyFinished && hasPrediction -> {
            val hReal = match.homeScore ?: 0
            val aReal = match.awayScore ?: 0
            val hPred = prediction!!.homeScore
            val aPred = prediction.awayScore
            
            val points = when {
                hPred == hReal && aPred == aReal -> 3
                (hPred > aPred && hReal > aReal) || (hPred < aPred && hReal < aReal) || (hPred == aPred && hReal == aReal) -> 1
                else -> 0
            }
            
            when (points) {
                3 -> Neon
                1 -> Gold
                else -> ErrorRed
            }
        }
        hasPrediction -> Gold.copy(alpha = 0.4f)
        else -> GlassBorder
    }

    val isTbd = match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD"
    val isExpired = now >= (match.matchDateMillis - 60_000) || isFinished
    val isLocked = isExpired || forceLocked

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = NavyElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    enabled = canPredict || isLocked,
                    onClick = { 
                        if (canPredict) onClick() 
                        else if (isExpired || isAdmin) onShowAllPredictions() 
                    }
                )
        ) {
            // 1. VER PALPITES DA GALERA (Grudado no teto)
            // Só mostra se o tempo expirou, o jogo acabou ou se for Admin (desde que os times estejam definidos)
            val showGaleraBadge = (isExpired || isAdmin) && !isTbd
            if (showGaleraBadge) {
                Surface(
                    onClick = onShowAllPredictions,
                    color = OrangeNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, OrangeNeon.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-6).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Visibility,
                            contentDescription = null,
                            tint = OrangeNeon,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            "PALPITES DA GALERA",
                            color = OrangeNeon,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
            }

            // 2. DATA E HORA
            if (!(isActuallyFinished && hasPrediction)) {
                Text(
                    text = formatMatchDate(match.matchDateMillis),
                    fontSize = 9.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(if (canPredict && !showGaleraBadge) Alignment.TopCenter else Alignment.TopEnd)
                        .padding(top = 10.dp, end = if (canPredict && !showGaleraBadge) 0.dp else 12.dp),
                    letterSpacing = 0.2.sp
                )
            }

            // Badge de Pontuação (Canto Superior Direito)
            if (isActuallyFinished && hasPrediction) {
                val hReal = match.homeScore ?: 0
                val aReal = match.awayScore ?: 0
                val hPred = prediction.homeScore
                val aPred = prediction.awayScore

                val points = when {
                    hPred == hReal && aPred == aReal -> 3
                    (hPred > aPred && hReal > aReal) || (hPred < aPred && hReal < aReal) || (hPred == aPred && hReal == aReal) -> 1
                    else -> 0
                }

                Surface(
                    color = when (points) {
                        3 -> Neon.copy(alpha = 0.15f)
                        1 -> Gold.copy(alpha = 0.15f)
                        else -> ErrorRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(bottomStart = 10.dp, topEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (points == 1) "+1 PONTO" else "+$points PONTOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = when (points) {
                            3 -> Neon
                            1 -> Gold
                            else -> ErrorRed
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(12.dp)) {
                if (isLocked || canPredict) {
                    Spacer(Modifier.height(32.dp))
                } else {
                    Spacer(Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(match.homeTeamFlag, fontSize = 26.sp)
                        Text(
                            match.homeTeam,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        if (hasPrediction) {
                            val hReal = match.homeScore ?: 0
                            val aReal = match.awayScore ?: 0
                            val hPred = prediction.homeScore
                            val aPred = prediction.awayScore

                            val statusColor = when {
                                !isActuallyFinished && !canPredict -> TextMuted // Travado ou Em Andamento = Cinza Suave
                                !isLocked -> Gold
                                hPred == hReal && aPred == aReal -> Neon
                                (hPred > aPred && hReal > aReal) || (hPred < aPred && hReal < aReal) || (hPred == aPred && hReal == aReal) -> Gold
                                else -> ErrorRed
                            }

                            val isExactMatch = isActuallyFinished && hPred == hReal && aPred == aReal

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(statusColor.copy(0.15f), statusColor.copy(0.05f))))
                                    .then(
                                        if (isExactMatch) Modifier.border(2.dp, Neon, RoundedCornerShape(12.dp))
                                        else Modifier
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$hPred", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                                    Text("×", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = statusColor.copy(alpha = 0.6f), modifier = Modifier.padding(horizontal = 8.dp))
                                    Text("$aPred", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = statusColor)
                                }
                            }
                        }

                        if (!hasPrediction) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.linearGradient(listOf(GlassWhite, GlassWhite)))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("vs", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextMuted.copy(alpha = 0.7f))
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                    ) {
                        Text(match.awayTeam, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White, maxLines = 2, lineHeight = 16.sp)
                        Text(match.awayTeamFlag, fontSize = 26.sp)
                    }
                }

                if (canPredict) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(10.dp)).background(Neon.copy(alpha = 0.08f)).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp), tint = Neon)
                        Spacer(Modifier.width(8.dp))
                        Text(text = if (hasPrediction) "EDITAR PALPITE" else "TOQUE PARA PALPITAR", fontSize = 11.sp, color = Neon, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
                    }
                } else if (isLocked) {
                    val dividerColor = if (isLive) Neon.copy(alpha = 0.3f) else GlassBorder
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        val isTbd = match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD"
                        val showEmBreve = forceLocked && isTbd && !match.isFinished
                        if (showEmBreve) {
                            Text(text = "EM BREVE VOCÊ PODERÁ PALPITAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Neon.copy(alpha = 0.6f), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 8.dp))
                        } else {
                            val statusText = if (isActuallyFinished) "JOGO ENCERRADO" else "JOGO EM ANDAMENTO"
                            val accentColor = when {
                                isActuallyFinished -> Color.White
                                isLive -> Neon // Placar real em andamento em verde
                                else -> Neon
                            }
                            Text(text = statusText, fontSize = 9.sp, fontWeight = FontWeight.Black, color = accentColor.copy(alpha = 0.7f), letterSpacing = 0.8.sp, modifier = Modifier.padding(top = 4.dp))
                            Box(
                                modifier = Modifier.padding(top = 2.dp).clip(RoundedCornerShape(6.dp)).background(accentColor.copy(alpha = 0.08f))
                                    .then(if (isAdmin) Modifier.clickable { onAdminUpdateScore() } else Modifier)
                                    .padding(horizontal = 8.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${match.homeScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
                                    Text("×", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = accentColor.copy(alpha = 0.5f), modifier = Modifier.padding(horizontal = 4.dp))
                                    Text("${match.awayScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = accentColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScoreDialog(
    match: Match,
    onDismiss: () -> Unit,
    onConfirm: (home: Int, away: Int) -> Unit
) {
    var homeStr by remember { mutableStateOf(match.homeScore?.toString() ?: "0") }
    var awayStr by remember { mutableStateOf(match.awayScore?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Placar Oficial", color = Color.White) },
        text = {
            Column {
                Text(
                    "Defina o placar real de ${match.homeTeam} x ${match.awayTeam}",
                    fontSize = 14.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = homeStr,
                        onValueChange = { if (it.length <= 2) homeStr = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(64.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = GlassWhite,
                            unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                        )
                    )
                    Text("x", modifier = Modifier.padding(horizontal = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
                    TextField(
                        value = awayStr,
                        onValueChange = { if (it.length <= 2) awayStr = it.filter { c -> c.isDigit() } },
                        modifier = Modifier.width(64.dp),
                        textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center, fontWeight = FontWeight.Bold),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = GlassWhite,
                            unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val h = homeStr.toIntOrNull() ?: 0
                    val a = awayStr.toIntOrNull() ?: 0
                    onConfirm(h, a)
                }
            ) {
                Text("SALVAR", color = Neon, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCELAR", color = Color.White.copy(alpha = 0.6f))
            }
        },
        containerColor = DeepNavy,
        shape = RoundedCornerShape(16.dp)
    )
}

@Preview
@Composable
fun BolaoDetailScreenPreview() {
    val myUserId = "pauloricha"
    val mockBolao = com.lpstudio.bolaodagalera.domain.model.Bolao(
        id = "bolao-1",
        name = "Bolão da Copa 2026",
        description = "Participe do maior bolão da Copa do Mundo!",
        code = "COPA26",
        ownerId = myUserId,
        participants = listOf(myUserId, "user-2"),
        createdAtMillis = 1781136000000L
    )

    val mockParticipants = listOf(
        RankingEntry(myUserId, "Paulo George Moreira Richa", "Paulão", 10, 2, 4),
        RankingEntry("user-2", "Maria Silva", "Maria", 8, 1, 5)
    )

    val now = TimeSource.nowMillis()
    val mockMatches = listOf(
        Match(
            id = "GS-A-1", homeTeam = "Canadá", awayTeam = "Bósnia",
            homeTeamCode = "CAN", awayTeamCode = "BIH", homeTeamFlag = "🇨🇦", awayTeamFlag = "🇧🇦",
            matchDateMillis = now - (24 * 60 * 60 * 1000), phase = Phase.GROUP_STAGE, group = "A",
            homeScore = 1, awayScore = 0
        ),
        Match(
            id = "GS-A-2", homeTeam = "Brasil", awayTeam = "Espanha",
            homeTeamCode = "BRA", awayTeamCode = "ESP", homeTeamFlag = "🇧🇷", awayTeamFlag = "🇪🇸",
            matchDateMillis = now + 172800000, phase = Phase.GROUP_STAGE, group = "A"
        )
    )

    val mockPredictions = mapOf(
        "GS-A-1" to Prediction(userId = myUserId, matchId = "GS-A-1", homeScore = 1, awayScore = 0),
        "GS-A-2" to Prediction(userId = myUserId, matchId = "GS-A-2", homeScore = 2, awayScore = 1)
    )

    val uiState = BolaoUiState(
        bolao = mockBolao,
        matches = mockMatches,
        userPredictions = mockPredictions,
        participants = mockParticipants,
        isLoading = false
    )

    AppTheme {
        BolaoDetailContent(
            bolaoId = "bolao-1",
            uiState = uiState,
            userId = myUserId,
            isOwner = true,
            launcherProvider = object : com.lpstudio.bolaodagalera.LauncherProvider {
                override fun shareText(text: String) {}
                override fun sendEmail(address: String, subject: String, body: String) {}
                override fun sendWhatsApp(phone: String, text: String) {}
            },
            onLeaveBolao = {},
            onNavigateToPrediction = {},
            onNavigateToAllPredictions = {},
            onNavigateToEdit = {},
            onNavigateToAddParticipants = {},
            onAdminUpdateScore = { _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
