package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.presentation.ranking.RankingScreen
import com.lpstudio.bolaodagalera.presentation.theme.*
import com.lpstudio.bolaodagalera.presentation.components.BolaoButton
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.util.resolveDisplayName
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder

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
    val currentUser = authRepository.currentUser
    val userId = currentUser?.id ?: ""
    val isAppOwner = currentUser?.username == "pauloricha" || userId == "pauloricha" || currentUser?.email == "paulo.richa@hotmail.com"
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
        isAppOwner = isAppOwner,
        launcherProvider = launcherProvider,
        onLeaveBolao = { viewModel.leaveBolao() },
        onApproveJoin = { userId, approve -> viewModel.approveParticipant(userId, approve) },
        onApproveLeave = { userId, approve -> viewModel.approveLeaveRequest(userId, approve) },
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
    isAppOwner: Boolean,
    launcherProvider: com.lpstudio.bolaodagalera.LauncherProvider,
    onLeaveBolao: () -> Unit,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit,
    onNavigateToPrediction: (matchId: String) -> Unit,
    onNavigateToAllPredictions: (matchId: String) -> Unit,
    onNavigateToEdit: (bolaoId: String) -> Unit,
    onNavigateToAddParticipants: (bolaoId: String) -> Unit,
    onAdminUpdateScore: (matchId: String, home: Int?, away: Int?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    var lastInteractedMatchId by rememberSaveable { mutableStateOf<String?>(null) }

    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    
    val championship = com.lpstudio.bolaodagalera.domain.model.Championship.fromId(uiState.bolao?.championshipId)

    val tabs = remember(uiState.bolao?.scope, uiState.bolao?.championshipId) {
        when (uiState.bolao?.scope) {
            com.lpstudio.bolaodagalera.domain.model.BolaoScope.ONLY_GROUPS -> listOf("Grupos", "Ranking")
            com.lpstudio.bolaodagalera.domain.model.BolaoScope.ONLY_KNOCKOUT -> listOf("Mata-Mata", "Ranking")
            com.lpstudio.bolaodagalera.domain.model.BolaoScope.PONTOS_CORRIDOS -> {
                val list = mutableListOf("Pontos Corridos", "Ranking")
                if (championship.hasStandings) list.add("Tabela")
                list
            }
            else -> {
                // Caso Escopo Total (FULL)
                if (championship.isPointsBased) {
                    val list = mutableListOf("Pontos Corridos", "Ranking")
                    if (championship.hasStandings) list.add("Tabela")
                    list
                } else if (championship.isGroupsAndKnockout) {
                    listOf("Grupos", "Mata-Mata", "Ranking")
                } else {
                    // Fallback para campeonatos apenas mata-mata (ex: Copa do Brasil)
                    listOf("Mata-Mata", "Ranking")
                }
            }
        }
    }

    var selectedRound by rememberSaveable { mutableIntStateOf(0) } // 0 = HOJE
    var selectedPhase by rememberSaveable { mutableStateOf<Phase?>(Phase.FRIENDLIES) } // FRIENDLIES como marker para HOJE

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
            onConfirm = { h: Int?, a: Int? ->
                onAdminUpdateScore(matchToUpdate!!.id, h, a)
                matchToUpdate = null
            }
        )
    }

    // Lógica para interceptar o botão voltar do sistema
    val isFirstTab = selectedTab == 0

    // Lógica para definir a Rodada "Padrão" (evita telas vazias ao abrir ou voltar)
    val defaultRound = remember(uiState.matches) {
        val matchesGroupStage = uiState.matches.filter { it.phase == Phase.GROUP_STAGE }
        if (matchesGroupStage.isEmpty()) return@remember 0
        
        val tz = TimeZone.currentSystemDefault()
        val now = TimeSource.nowMillis()
        val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
        
        val hasMatchToday = matchesGroupStage.any { 
            Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate 
        }

        if (hasMatchToday) 0 
        else {
            val upcomingRound = matchesGroupStage
                .filter { !it.isFinished && it.matchDateMillis > now }
                .minByOrNull { it.matchDateMillis }
                ?.groupRound()
            upcomingRound ?: matchesGroupStage.maxByOrNull { it.matchDateMillis }?.groupRound() ?: 1
        }
    }

    // A aba principal é a primeira aba na visualização padrão (Hoje ou Rodada Atual)
    val isMainTab = isFirstTab && selectedRound == defaultRound && 
                   (selectedPhase == com.lpstudio.bolaodagalera.domain.model.Phase.FRIENDLIES || selectedPhase == null)
    
    com.lpstudio.bolaodagalera.CommonBackHandler(enabled = !isMainTab) {
        if (!isFirstTab) {
            selectedTab = 0
        }
        
        // Garante que os filtros voltem para o padrão (Rodada atual com jogos)
        if (selectedRound != defaultRound) {
            selectedRound = defaultRound
        }
        if (selectedPhase != com.lpstudio.bolaodagalera.domain.model.Phase.FRIENDLIES && selectedPhase != null) {
            selectedPhase = com.lpstudio.bolaodagalera.domain.model.Phase.FRIENDLIES
        }
    }

    var hasAutoSelectedTab by rememberSaveable(bolaoId) { mutableStateOf(false) }

    // Auto-selecionar Ranking se todos os jogos acabaram, ou a rodada "mais relevante" para agora
    LaunchedEffect(uiState.matches) {
        if (uiState.matches.isEmpty()) return@LaunchedEffect
        
        // 1. Se todos os jogos acabaram, vai direto pro Ranking (apenas na primeira carga)
        if (!hasAutoSelectedTab) {
            val allFinished = uiState.matches.all { it.isFinished }
            if (allFinished) {
                val rankingIdx = tabs.indexOf("Ranking")
                if (rankingIdx != -1) {
                    selectedTab = rankingIdx
                    hasAutoSelectedTab = true
                    return@LaunchedEffect
                }
            }
            hasAutoSelectedTab = true
        }

        // 2. Lógica de Rodada Inteligente (Executa apenas na primeira carga)
        if (selectedRound == 0 && defaultRound != 0) {
            selectedRound = defaultRound
        }
    }

    if (showLeaveDialog) {
        AlertDialog(
            onDismissRequest = { showLeaveDialog = false },
            containerColor = NavyCard,
            title = { Text("Sair do Bolão?", color = Color.White, fontWeight = FontWeight.Bold) },
            text = { 
                Text(
                    if (isOwner) "Você é o dono deste bolão. Se sair, o bolão continuará existindo mas ficará sem administrador."
                    else "O administrador precisará confirmar sua saída para que você seja removido do ranking.", 
                    color = TextMuted
                ) 
            },
            confirmButton = {
                TextButton(onClick = { 
                    showLeaveDialog = false
                    onLeaveBolao()
                }) {
                    Text(if (isOwner) "Sair" else "Pedir para sair", color = ErrorRed, fontWeight = FontWeight.Bold)
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
                            // --- SEÇÃO DE SOLICITAÇÕES (Apenas para Admin) ---
                            if (isOwner && (uiState.pendingJoinUsers.isNotEmpty() || uiState.pendingExitUsers.isNotEmpty())) {
                                item {
                                    Text("Solicitações Pendentes", color = Gold, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 8.dp))
                                }
                                
                                items(uiState.pendingJoinUsers) { user ->
                                    PendingRequestItem(
                                        user = user,
                                        label = "Quer entrar",
                                        onApprove = { onApproveJoin(user.id, true) },
                                        onDeny = { onApproveJoin(user.id, false) }
                                    )
                                }
                                
                                items(uiState.pendingExitUsers) { user ->
                                    PendingRequestItem(
                                        user = user,
                                        label = "Quer sair",
                                        accentColor = ErrorRed,
                                        onApprove = { onApproveLeave(user.id, true) },
                                        onDeny = { onApproveLeave(user.id, false) }
                                    )
                                }

                                item {
                                    Spacer(Modifier.height(16.dp))
                                    Text("Participantes", color = TextMuted, fontSize = 14.sp, fontWeight = FontWeight.Black, modifier = Modifier.padding(vertical = 8.dp))
                                }
                            }

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
                                            .padding(horizontal = 12.dp, vertical = 12.dp),
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
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = onNavigateBack, 
                                modifier = Modifier
                                    .size(36.dp)
                                    .offset(x = (-10).dp)
                            ) {
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
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 4.dp)
                                    .offset(x = (-8).dp)
                            )
                            if (isOwner) {
                                IconButton(
                                    onClick = {
                                        uiState.bolao?.let { bolao ->
                                            val webUrl = "https://bolaodagalera-bb002.web.app/invite?code=${bolao.code}"
                                            val appUrl = "bolaodagalera://invite?code=${bolao.code}"
                                            launcherProvider.shareText("Entre no meu bolão '${bolao.name}'! 🏆\n\nLink: $webUrl\n\nSe o link não abrir o app automaticamente, use este: $appUrl\n\nCódigo: ${bolao.code}")
                                        }
                                    }, 
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Compartilhar", tint = TextMuted, modifier = Modifier.size(20.dp))
                                }
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
                            } else {
                                Box {
                                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(36.dp)) {
                                        Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false },
                                        modifier = Modifier.background(NavyCard).border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("Compartilhar", color = Color.White) },
                                            leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, tint = TextMuted, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                uiState.bolao?.let { bolao ->
                                                    val webUrl = "https://bolaodagalera-bb002.web.app/invite?code=${bolao.code}"
                                                    val appUrl = "bolaodagalera://invite?code=${bolao.code}"
                                                    launcherProvider.shareText("Entre no meu bolão '${bolao.name}'! 🏆\n\nLink: $webUrl\n\nSe o link não abrir o app automaticamente, use este: $appUrl\n\nCódigo: ${bolao.code}")
                                                }
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Sair do Bolão", color = ErrorRed) },
                                            leadingIcon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, tint = ErrorRed, modifier = Modifier.size(18.dp)) },
                                            onClick = {
                                                showMenu = false
                                                showLeaveDialog = true
                                            }
                                        )
                                    }
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
                            if (isOwner && (bolao.pendingParticipants.isNotEmpty() || bolao.pendingExits.isNotEmpty())) {
                                Spacer(Modifier.height(12.dp))
                                val pendingCount = bolao.pendingParticipants.size + bolao.pendingExits.size
                                Surface(
                                    color = Gold.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth().clickable { showParticipantsSheet = true }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Text("⚠️", fontSize = 16.sp)
                                        Text(
                                            "$pendingCount solicitações pendentes (Entrada/Saída).",
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
                                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
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
                                        com.lpstudio.bolaodagalera.domain.model.Championship.fromId(bolao.championshipId).displayName,
                                        fontSize = 12.sp,
                                        color = TextMuted,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // Pill tabs
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

                val isSocialEnabled = true
                val currentTab = tabs.getOrNull(selectedTab) ?: "Grupos"

                when (currentTab) {
                    "Grupos", "Jogos", "Rodadas", "Pontos Corridos" -> GroupStageTab(
                        matches = groupMatches.ifEmpty { filteredMatches },
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isAppOwner,
                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                        selectedRound = selectedRound,
                        showSocialBadge = isSocialEnabled,
                        onRoundChange = { selectedRound = it },
                        listState = groupsListState,
                        expandedGroups = expandedGroups,
                        lastInteractedMatchId = lastInteractedMatchId,
                        onClearLastMatchId = { lastInteractedMatchId = null },
                        onMatchClick = { 
                            lastInteractedMatchId = it
                            onNavigateToPrediction(it) 
                        },
                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                        onAdminUpdateScore = { matchToUpdate = it },
                        showRoundSelector = true
                    )
                    "Mata-Mata" -> KnockoutTab(
                        matches = filteredMatches,
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isAppOwner,
                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                        selectedPhase = selectedPhase,
                        showSocialBadge = isSocialEnabled,
                        onPhaseChange = { selectedPhase = it },
                        listState = knockoutListState,
                        lastInteractedMatchId = lastInteractedMatchId,
                        onClearLastMatchId = { lastInteractedMatchId = null },
                        onMatchClick = { 
                            lastInteractedMatchId = it
                            onNavigateToPrediction(it) 
                        },
                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                        onAdminUpdateScore = { matchToUpdate = it },
                        championship = championship
                    )
                    "Ranking" -> RankingScreen(bolaoId = bolaoId)
                    "Tabela" -> {
                        val champId = uiState.bolao?.championshipId ?: "UNKNOWN"
                        val champMatches = remember(uiState.allMatches, champId) {
                            uiState.allMatches.filter { it.championshipId == champId }
                        }
                        StandingsTab(matches = champMatches)
                    }
                }
            }
        }
    }
}

// Rodadas da fase de grupos agora são baseadas nos jogos filtrados
private fun unlockedRounds(groupMatches: List<Match>): Set<Int> = 
    groupMatches.map { it.groupRound() }.toSet()

// ── Aba Tabela de Classificação ─────────────────────────────────────────────

@Composable
private fun StandingsTab(matches: List<Match>) {
    val standings = remember(matches) {
        com.lpstudio.bolaodagalera.domain.model.StandingsCalculator.calculate(matches)
    }

    if (standings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Aguardando início dos jogos...", color = TextMuted)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", modifier = Modifier.width(24.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("TIME", modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("P", "J", "V", "SG").forEach {
                            Text(it, modifier = Modifier.width(35.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            items(standings.size) { index ->
                val team = standings[index]
                val (name, flag, crest) = remember(team.teamName, team.teamFlag, team.teamCrest, matches) {
                    resolveDisplayName("", team.teamName, team.teamFlag, matches, true)
                }

                val isG4 = index < 4
                val isG5 = index == 4
                val isZ4 = index >= standings.size - 4 && standings.size > 5
                
                val accentColor = when {
                    isG4 -> Neon
                    isG5 -> Gold
                    isZ4 -> ErrorRed
                    else -> null
                }

                Surface(
                    color = when {
                        isG4 -> Neon.copy(alpha = 0.05f)
                        isG5 -> Gold.copy(alpha = 0.05f)
                        isZ4 -> ErrorRed.copy(alpha = 0.05f)
                        else -> NavyCard
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp, 
                        when {
                            isG4 -> Neon.copy(alpha = 0.2f)
                            isG5 -> Gold.copy(alpha = 0.2f)
                            isZ4 -> ErrorRed.copy(alpha = 0.2f)
                            else -> GlassBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${index + 1}",
                            modifier = Modifier.width(24.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor ?: TextMuted
                        )
                        
                        TeamIcon(crestUrl = crest ?: team.teamCrest, flag = AnnotatedString(flag), isTbd = false, size = 24.dp)
                        
                        Spacer(Modifier.width(10.dp))
                        
                        Text(
                            name,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )

                        Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${team.points}", modifier = Modifier.width(35.dp), fontSize = 13.sp, fontWeight = FontWeight.Black, color = accentColor ?: Color.White, textAlign = TextAlign.Center)
                            Text("${team.played}", modifier = Modifier.width(35.dp), fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Text("${team.won}", modifier = Modifier.width(35.dp), fontSize = 12.sp, color = TextMuted, textAlign = TextAlign.Center)
                            Text("${team.goalDifference}", modifier = Modifier.width(35.dp), fontSize = 12.sp, color = if (team.goalDifference > 0) Neon else if (team.goalDifference < 0) ErrorRed else TextMuted, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }
}

// ── Aba Fase de Grupos ────────────────────────────────────────────────────────

@Composable
private fun GroupStageTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    selectedRound: Int,
    showSocialBadge: Boolean = true,
    onRoundChange: (Int) -> Unit,
    listState: LazyListState,
    expandedGroups: SnapshotStateList<String>,
    lastInteractedMatchId: String?,
    onClearLastMatchId: () -> Unit,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onAdminUpdateScore: (Match) -> Unit,
    showRoundSelector: Boolean = true
) {
    val unlocked = remember(matches) { unlockedRounds(matches) }
    
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date

    val hasMatchToday = remember(matches, todayDate) {
        matches.any { 
            val mDate = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date
            mDate == todayDate 
        }
    }

    val roundMatches = remember(matches, selectedRound, showRoundSelector, todayDate, now) {
        when {
            !showRoundSelector -> matches.sortedBy { it.matchDateMillis }
            selectedRound == 0 -> {
                matches.filter { m ->
                    val mDate = Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz).date
                    // Na aba HOJE, mantém apenas jogos de hoje (mesmo encerrados, para visualização de resultados)
                    mDate == todayDate || (now in m.matchDateMillis..(m.matchDateMillis + 3 * 3600_000L))
                }.sortedBy { it.matchDateMillis }
            }
            // Nas Rodadas 1, 2 e 3, mostramos TODOS os jogos da rodada novamente
            else -> matches.filter { it.groupRound() == selectedRound }.sortedBy { it.matchDateMillis }
        }
    }
    val byGroup = remember(roundMatches) {
        roundMatches.groupBy { it.group ?: "" }
    }

    val showShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    // Flag para controlar se já fizemos o scroll inicial desta rodada
    var hasHandledInitialScroll by rememberSaveable(selectedRound) { mutableStateOf(false) }

    LaunchedEffect(selectedRound, matches.isNotEmpty(), byGroup, lastInteractedMatchId) {
        if (matches.isEmpty() || byGroup.isEmpty()) return@LaunchedEffect
        
        val sortedEntries = byGroup.entries.sortedBy { it.key }

        // 1. PRIORIDADE TOTAL: Se o usuário acabou de voltar de um palpite
        if (lastInteractedMatchId != null) {
            val targetMatch = matches.find { it.id == lastInteractedMatchId }
            if (targetMatch != null) {
                // Se estamos na aba HOJE, não expandimos grupos nem scrollamos
                // para manter a visualização simplificada do "Hoje"
                if (selectedRound == 0) {
                    onClearLastMatchId()
                    return@LaunchedEffect
                }

                val groupOfMatch = targetMatch.group ?: ""
                
                // Abre o grupo se estiver fechado
                if (!expandedGroups.contains(groupOfMatch)) {
                    expandedGroups.add(groupOfMatch)
                    kotlinx.coroutines.delay(200) // Tempo para o Compose processar a abertura
                }

                var targetIndex = 0
                for (entry in sortedEntries) {
                    if (entry.key == groupOfMatch) {
                        val matchIdx = entry.value.indexOfFirst { it.id == lastInteractedMatchId }
                        targetIndex += 1 + (if (matchIdx != -1) matchIdx else 0)
                        break
                    }
                    targetIndex += 1 + entry.value.size + 1
                }
                
                listState.scrollToItem(targetIndex)
                hasHandledInitialScroll = true // Marca como resolvido para não rodar a lógica de "Hoje"
                onClearLastMatchId()
                return@LaunchedEffect
            }
        }

        // 2. LÓGICA DE NAVEGAÇÃO INICIAL: Só roda se for a primeira vez carregando a rodada
        if (!hasHandledInitialScroll) {
            expandedGroups.clear()

            // Verifica se todos os jogos desta rodada já terminaram
            val isRoundFinished = roundMatches.isNotEmpty() && roundMatches.all { it.isFinished }

            // Se a rodada já acabou (e não é a aba "Hoje"), mantém tudo fechado por padrão
            if (isRoundFinished && selectedRound != 0) {
                listState.scrollToItem(0)
                hasHandledInitialScroll = true
                return@LaunchedEffect
            }

            // 1. Encontrar o jogo "Foco": Em andamento > Próximo hoje > Próximo geral
            val matchWindow = 2 * 60 * 60 * 1000L + (30 * 60 * 1000L) // Janela de jogo em andamento
            
            val focusMatch = matches.filter { it.phase == Phase.GROUP_STAGE }.let { allGroupMatches ->
                // Prioridade 1: Em andamento
                allGroupMatches.find { now in it.matchDateMillis..(it.matchDateMillis + matchWindow) }
                    ?: // Prioridade 2: Próximo hoje
                    allGroupMatches.filter { 
                        val mDate = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date
                        mDate == todayDate && it.matchDateMillis > now 
                    }.minByOrNull { it.matchDateMillis }
                    ?: // Prioridade 3: Próximo geral (amanhã ou depois)
                    allGroupMatches.filter { it.matchDateMillis > now }.minByOrNull { it.matchDateMillis }
            }

            if (focusMatch != null) {
                val groupOfFocus = focusMatch.group ?: ""
                val roundOfFocus = focusMatch.groupRound()

                // Aba HOJE: Abre TODOS os grupos que têm jogo hoje
                if (selectedRound == 0) {
                    expandedGroups.addAll(byGroup.keys)
                    listState.scrollToItem(0)
                } 
                // Abas de Rodada: Lógica inteligente de abertura
                else {
                    // Identifica se a rodada selecionada tem o jogo em foco
                    if (selectedRound == roundOfFocus) {
                        // 1. Abre o grupo do jogo foco
                        expandedGroups.add(groupOfFocus)
                        
                        // 2. Abre também outros grupos desta rodada que têm jogos HOJE e NÃO encerraram
                        val otherGroupsToday = matches.filter { 
                            it.groupRound() == selectedRound && 
                            Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate &&
                            it.matchDateMillis + matchWindow >= now &&
                            !it.isFinished
                        }.mapNotNull { it.group }
                        expandedGroups.addAll(otherGroupsToday)
                        
                        // 3. Cálculo de scroll para abas de Rodada
                        var targetIndex = 0
                        for (entry in sortedEntries) {
                            if (entry.key == groupOfFocus) break
                            targetIndex += 1 + entry.value.size + 1
                        }
                        listState.scrollToItem(targetIndex)
                    } else {
                        // Se estiver em uma rodada sem jogo foco (ex: rodada futura), abre apenas o primeiro grupo
                        sortedEntries.firstOrNull()?.key?.let { expandedGroups.add(it) }
                        listState.scrollToItem(0)
                    }
                }
            } else {
                // Caso não encontre nenhum jogo futuro (fim da fase de grupos), abre o primeiro
                if (selectedRound == 0) expandedGroups.addAll(byGroup.keys)
                else sortedEntries.firstOrNull()?.key?.let { expandedGroups.add(it) }

                listState.scrollToItem(0)
            }
            hasHandledInitialScroll = true
        }
    }

    Column(Modifier.fillMaxSize()) {
        if (showRoundSelector) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavy)
                    .padding(vertical = 8.dp)
            ) {
                RodadaSelector(
                    selected = selectedRound,
                    unlocked = unlocked,
                    showHoje = hasMatchToday,
                    onSelect = { if (it == 0 || it in unlocked) onRoundChange(it) }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (roundMatches.isEmpty() && selectedRound == 0) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum jogo programado para hoje.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }

                if (selectedRound == 0 && roundMatches.isNotEmpty()) {
                    items(roundMatches, key = { it.id }, contentType = { "match" }) { match ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                        ) {
                            MatchCard(
                                match = match,
                                prediction = predictions[match.id],
                                isAdmin = isAdmin,
                                bolaoCreatedAt = bolaoCreatedAt,
                                showSocialBadge = showSocialBadge,
                                allMatches = matches,
                                onClick = { onMatchClick(match.id) },
                                onShowAllPredictions = { onShowAllPredictions(match) },
                                onAdminUpdateScore = { onAdminUpdateScore(match) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
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
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp) // Removido horizontal = 8.dp para alinhar com abas
                                ) {
                                    MatchCard(
                                        match = match,
                                        prediction = predictions[match.id],
                                        isAdmin = isAdmin,
                                        bolaoCreatedAt = bolaoCreatedAt,
                                        showSocialBadge = showSocialBadge,
                                        allMatches = matches,
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
            }

            // Sombra de Scroll
            androidx.compose.animation.AnimatedVisibility(
                visible = showShadow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }
            
            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    color = Neon, trackColor = Color.Transparent
                )
            }
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
    bolaoCreatedAt: Long,
    selectedPhase: Phase?,
    showSocialBadge: Boolean = true,
    onPhaseChange: (Phase?) -> Unit,
    listState: LazyListState,
    lastInteractedMatchId: String?,
    onClearLastMatchId: () -> Unit,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onAdminUpdateScore: (Match) -> Unit,
    championship: Championship = Championship.DEFAULT
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

    val isTwoLegged = championship.isTwoLegged
    val tz = TimeZone.currentSystemDefault()
    val now = com.lpstudio.bolaodagalera.util.TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date

    val hasMatchToday = remember(matches, todayDate) {
        matches.filter { it.phase != Phase.GROUP_STAGE }.any { 
            val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
            val mDate = mTime.date
            val isStrictlyToday = mDate == todayDate
            val isEarlyTomorrow = (mDate.toEpochDays() == todayDate.toEpochDays() + 1) && mTime.hour < 4
            isStrictlyToday || isEarlyTomorrow || (now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L))
        }
    }

    // Gerencia as abas (Labels) para campeonatos com Ida e Volta
    val labels = remember(phaseOrder, isTwoLegged) {
        if (isTwoLegged) {
            phaseOrder.flatMap { phase ->
                if (phase == Phase.FINAL || phase == Phase.THIRD_PLACE) listOf(phase.label)
                else listOf("${phase.label} - Ida", "${phase.label} - Volta")
            }
        } else {
            phaseOrder.map { it.label }
        }
    }

    var selectedLabel by rememberSaveable(championship.id) { mutableStateOf<String?>(null) }

    // Sincroniza o label quando a fase muda (ex: via lógica de "Hoje" ou scroll)
    LaunchedEffect(selectedPhase, labels) {
        if (selectedPhase == Phase.FRIENDLIES) {
            selectedLabel = "⚽️ HOJE"
        } else if (selectedPhase != null) {
            if (isTwoLegged) {
                if (selectedLabel?.startsWith(selectedPhase.label) != true) {
                    selectedLabel = labels.find { it.startsWith(selectedPhase.label) }
                }
            } else {
                selectedLabel = selectedPhase.label
            }
        }
    }

    // Auto-selecionar HOJE se houver jogos, senão a próxima fase
    LaunchedEffect(matches, lastInteractedMatchId) {
        if (lastInteractedMatchId != null) {
            val targetMatch = matches.find { it.id == lastInteractedMatchId }
            if (targetMatch != null && targetMatch.phase != Phase.GROUP_STAGE) {
                if (selectedPhase == Phase.FRIENDLIES) {
                    onClearLastMatchId()
                    return@LaunchedEffect
                }

                onPhaseChange(targetMatch.phase)
                
                if (isTwoLegged) {
                    val leg = if (targetMatch.id.contains("-L2")) "Volta" else "Ida"
                    selectedLabel = "${targetMatch.phase.label} - $leg"
                }

                kotlinx.coroutines.delay(100)
                
                val currentPhaseMatches = matches.filter { 
                    if (isTwoLegged) {
                        it.phase == targetMatch.phase && 
                        (if (targetMatch.id.contains("-L2")) it.id.contains("-L2") else it.id.contains("-L1"))
                    } else {
                        it.phase == targetMatch.phase 
                    }
                }.sortedBy { it.id.split("-").lastOrNull()?.toIntOrNull() ?: 0 }
                
                val pairs = currentPhaseMatches.chunked(2)
                val pairIndex = pairs.indexOfFirst { pair -> pair.any { it.id == lastInteractedMatchId } }
                if (pairIndex != -1) {
                    listState.scrollToItem(pairIndex)
                }
                onClearLastMatchId()
                return@LaunchedEffect
            }
        }

        if (selectedPhase != Phase.FRIENDLIES && selectedPhase != null) return@LaunchedEffect
        
        if (hasMatchToday) {
            onPhaseChange(Phase.FRIENDLIES)
        } else {
            val nextPhase = phaseOrder.find { p -> matches.any { it.phase == p && !it.isFinished } }
            if (nextPhase != null) onPhaseChange(nextPhase)
        }
    }

    val isKnockoutUnlocked = true // Liberado para palpites conforme solicitação

    val showShadow by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }

    Column(Modifier.fillMaxSize()) {
        if (labels.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepNavy)
                    .padding(vertical = 8.dp)
            ) {
                KnockoutPhaseSelector(
                    labels = labels,
                    selectedLabel = selectedLabel,
                    isUnlocked = isKnockoutUnlocked,
                    showHoje = hasMatchToday,
                    onSelect = { label ->
                        if (label == "⚽️ HOJE") {
                            onPhaseChange(Phase.FRIENDLIES)
                        } else {
                            val phaseName = label?.substringBefore(" - ")
                            val phase = Phase.entries.find { p -> p.label == phaseName }
                            selectedLabel = label
                            onPhaseChange(phase)
                        }
                    }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            val phaseMatches = remember(matches, selectedPhase, selectedLabel, isTwoLegged, todayDate, now) {
                if (selectedPhase == Phase.FRIENDLIES) {
                    matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
                        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz)
                        val mDate = mTime.date
                        val isStrictlyToday = mDate == todayDate
                        val isEarlyTomorrow = (mDate.toEpochDays() == todayDate.toEpochDays() + 1) && mTime.hour < 4
                        isStrictlyToday || isEarlyTomorrow || (now in m.matchDateMillis..(m.matchDateMillis + 3 * 3600_000L))
                    }.sortedWith(
                        compareByDescending<Match> { 
                            val isLiveStatus = it.status in listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
                            val isLocked = now >= (it.matchDateMillis - 60_000)
                            val isNotFinished = it.status != "FINISHED"
                            
                            if (isLiveStatus || (isLocked && isNotFinished)) 2 else if (isNotFinished) 1 else 0
                        }
                        .thenBy { it.matchDateMillis }
                    )
                } else if (isTwoLegged && selectedLabel != null && selectedLabel != "⚽️ HOJE") {
                    val basePhaseName = selectedLabel!!.substringBefore(" - ")
                    val isVoltaTab = selectedLabel!!.contains("Volta")
                    
                    // 1. Pega todos os jogos daquela fase exata (evita que Semifinal apareça na Final)
                    val phaseMatches = matches.filter { 
                        it.phase.label.equals(basePhaseName, ignoreCase = true)
                    }

                    // 2. Agrupa os jogos por confronto (Mesma chave de mata-mata)
                    val confrontationGroups = phaseMatches.groupBy { 
                        if (it.matchOrder > 0) {
                            it.matchOrder.toString()
                        } else {
                            // Se não tem matchOrder, tentamos agrupar por times (ordem alfabética dos códigos)
                            // Isso une o jogo de Ida (A x B) e Volta (B x A) na mesma chave.
                            val t1 = it.homeTeamCode
                            val t2 = it.awayTeamCode
                            if (t1 != "TBD" && t2 != "TBD" && t1.isNotBlank() && t2.isNotBlank()) {
                                listOf(t1, t2).sorted().joinToString("-")
                            } else {
                                // Fallback para TBD: usa o ID removendo sufixos de perna (-L1, -L2)
                                it.id.substringBefore("-L")
                            }
                        }
                    }

                    // 3. Para cada par, seleciona apenas um baseado na aba atual
                    confrontationGroups.values.flatMap { pair ->
                        if (isVoltaTab) {
                            pair.filter { it.id.contains("-L2") }
                        } else {
                            pair.filter { !it.id.contains("-L2") }
                        }
                    }.sortedBy { it.matchOrder.takeIf { o -> o > 0 } ?: 99 }
                } else {
                    matches.filter { it.phase == selectedPhase }
                        .sortedBy { it.matchDateMillis }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (phaseMatches.isEmpty() && selectedPhase == Phase.FRIENDLIES) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            Text("Nenhum jogo de mata-mata hoje.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }

                items(phaseMatches, key = { it.id }) { match ->
                    MatchCard(
                        match = match,
                        prediction = predictions[match.id],
                        isAdmin = isAdmin,
                        bolaoCreatedAt = bolaoCreatedAt,
                        forceLocked = !isKnockoutUnlocked,
                        showSocialBadge = showSocialBadge,
                        allMatches = matches,
                        onClick = { onMatchClick(match.id) },
                        onShowAllPredictions = { onShowAllPredictions(match) },
                        onAdminUpdateScore = { onAdminUpdateScore(match) }
                    )
                }
            }

            // Sombra de Scroll
            androidx.compose.animation.AnimatedVisibility(
                visible = showShadow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
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
}


@Composable
private fun KnockoutPhaseSelector(
    labels: List<String>,
    selectedLabel: String?,
    isUnlocked: Boolean,
    showHoje: Boolean,
    onSelect: (String?) -> Unit
) {
    val listState = rememberLazyListState()

    // Removido LaunchedEffect de auto-scroll para manter a barra de abas estática.
    // Agora ela só se move se o usuário arrastar.
    
    // Verifica se há conteúdo para scrollar para a esquerda ou direita
    val canScrollBackward by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 }
    }
    val canScrollForward by remember {
        derivedStateOf {
            val lastItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            if (lastItem == null) false
            else lastItem.index < listState.layoutInfo.totalItemsCount - 1 || 
                 (lastItem.offset + lastItem.size) > listState.layoutInfo.viewportEndOffset
        }
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.lazy.LazyRow(
            state = listState,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (showHoje) {
                item {
                    FilterChip(
                        label = "⚽️ HOJE",
                        isSelected = selectedLabel == "⚽️ HOJE",
                        isUnlocked = true,
                        onClick = { onSelect("⚽️ HOJE") }
                    )
                }
            }
            items(labels) { label ->
                FilterChip(
                    label = label,
                    isSelected = selectedLabel == label,
                    isUnlocked = isUnlocked,
                    onClick = { onSelect(label) }
                )
            }
        }

        // Gradiente de sombra à esquerda (apenas se houver scroll para trás)
        if (canScrollBackward) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .width(40.dp)
                    .matchParentSize()
                    .background(Brush.horizontalGradient(listOf(DeepNavy, Color.Transparent)))
            )
        }

        // Gradiente de sombra à direita (apenas se houver scroll para frente)
        if (canScrollForward) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .width(40.dp)
                    .matchParentSize()
                    .background(Brush.horizontalGradient(listOf(Color.Transparent, DeepNavy)))
            )
        }
    }
}

@Composable
private fun RodadaSelector(
    selected: Int, 
    unlocked: Set<Int>, 
    showHoje: Boolean,
    onSelect: (Int) -> Unit
) {
    val sortedRounds = remember(unlocked) { unlocked.sorted() }
    val listState = rememberLazyListState()

    // Faz o scroll para a rodada selecionada ficar à esquerda
    LaunchedEffect(selected, sortedRounds) {
        if (sortedRounds.isEmpty()) return@LaunchedEffect
        val targetIndex = when {
            selected == 0 && showHoje -> 0
            selected > 0 -> {
                val idx = sortedRounds.indexOf(selected)
                if (idx != -1) (if (showHoje) idx + 1 else idx) else -1
            }
            else -> -1
        }
        if (targetIndex != -1) {
            listState.animateScrollToItem(targetIndex)
        }
    }

    androidx.compose.foundation.lazy.LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        // Opção HOJE
        if (showHoje) {
            item {
                FilterChip(
                    label = "⚽️ HOJE",
                    isSelected = selected == 0,
                    isUnlocked = true,
                    onClick = { onSelect(0) }
                )
            }
        }
        
        items(sortedRounds) { round ->
            FilterChip(
                label = "Rodada $round",
                isSelected = selected == round,
                isUnlocked = true, // Já vem filtrado do ViewModel
                onClick = { onSelect(round) }
            )
        }
    }
}

@Composable
private fun FilterChip(
    label: String,
    isSelected: Boolean,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isUnlocked -> GlassBorder
            else -> Color.Transparent
        },
        label = "border_$label"
    )

    val containerColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon.copy(alpha = 0.12f)
            isUnlocked -> NavyElevated
            else -> NavyCard.copy(alpha = 0.5f)
        },
        label = "bg_$label"
    )

    val textColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isUnlocked -> Color.White
            else -> TextMuted.copy(alpha = 0.4f)
        },
        label = "text_$label"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(14.dp))
            .then(if (isUnlocked) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            softWrap = false
        )
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
private fun TeamNameText(
    name: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start
) {
    var fontSize by remember(name) { mutableStateOf(13.sp) }
    var readyToDraw by remember(name) { mutableStateOf(false) }

    Text(
        text = name,
        modifier = modifier.drawWithContent {
            if (readyToDraw) drawContent()
        },
        fontSize = fontSize,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        maxLines = 2,
        lineHeight = if (fontSize < 13.sp) (fontSize.value + 2).sp else 16.sp,
        textAlign = textAlign,
        softWrap = true,
        onTextLayout = { result ->
            if (result.hasVisualOverflow && fontSize > 9.sp) {
                fontSize = (fontSize.value - 0.5f).sp
            } else {
                readyToDraw = true
            }
        }
    )
}

@Composable
private fun TeamIcon(
    crestUrl: String?,
    flag: AnnotatedString,
    isTbd: Boolean,
    size: androidx.compose.ui.unit.Dp = 32.dp
) {
    val hasCrest = !crestUrl.isNullOrBlank()
    
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(if (hasCrest) Color.Transparent else NavyElevated.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCrest) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(crestUrl)
                    .decoderFactory(SvgDecoder.Factory())
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f),
                loading = {
                    CircularProgressIndicator(modifier = Modifier.size(size * 0.5f), strokeWidth = 1.dp, color = Neon)
                },
                error = {
                    Text(
                        text = flag,
                        fontSize = (size.value * 0.7f).sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            )
        } else {
            // Se não tem escudo, mostra a bandeira/emojis (sempre para garantir visibilidade)
            Text(
                text = flag,
                fontSize = if (isTbd) (size.value * 0.5f).sp else (size.value * 0.7f).sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun MatchCard(
    match: Match,
    prediction: Prediction?,
    isAdmin: Boolean = false,
    bolaoCreatedAt: Long = 0L,
    forceLocked: Boolean = false,
    showSocialBadge: Boolean = true,
    allMatches: List<Match> = emptyList(),
    onShowAllPredictions: () -> Unit = {},
    onAdminUpdateScore: () -> Unit = {},
    onClick: () -> Unit
) {
    val hasPrediction = prediction != null
    val isFinished = match.isFinished
    val now = TimeSource.nowMillis()
    val matchStart = match.matchDateMillis
    
    // Resolve nomes e bandeiras para mata-mata TBD
    val (homeDisplayName, homeDisplayFlag, homeCrest) = remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
        resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
    }
    val (awayDisplayName, awayDisplayFlag, awayCrest) = remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
        resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
    }

    // Lógica para Placar de Ida (Mata-Mata com Ida/Volta)
    val championship = Championship.fromId(match.championshipId)
    val isTwoLegged = championship.isTwoLegged
    val isVolta = match.id.contains("-L2")
    val idaScore = remember(match.id, allMatches, isTwoLegged, isVolta) {
        if (isTwoLegged && isVolta) {
            // Tenta encontrar o jogo de ida pelo ID ou pelos times (invertidos)
            val idaMatch = allMatches.find { m ->
                m.championshipId == match.championshipId &&
                m.phase == match.phase &&
                m.id != match.id &&
                !m.id.contains("-L2") &&
                (
                    // Critério 1: Mesmo matchOrder
                    (match.matchOrder > 0 && m.matchOrder == match.matchOrder) ||
                    // Critério 2: ID correlacionado (ex: mesmo ID da API mas sufixo diferente)
                    m.id.replace("-L1", "") == match.id.replace("-L2", "") ||
                    // Critério 3: Times invertidos (O mais genérico)
                    (m.homeTeamCode == match.awayTeamCode && m.awayTeamCode == match.homeTeamCode)
                )
            }
            
            if (idaMatch != null && idaMatch.homeScore != null && idaMatch.awayScore != null) {
                "${idaMatch.homeScore}×${idaMatch.awayScore}"
            } else null
        } else null
    }

    val homeAnnotatedFlag = remember(homeDisplayFlag) {
        val parts = homeDisplayFlag.split(" ou ")
        if (parts.size > 1) {
            buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index < parts.size - 1) {
                        withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                            append(" ou ")
                        }
                    }
                }
            }
        } else {
            AnnotatedString(homeDisplayFlag)
        }
    }

    val awayAnnotatedFlag = remember(awayDisplayFlag) {
        val parts = awayDisplayFlag.split(" ou ")
        if (parts.size > 1) {
            buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                    append(part)
                    if (index < parts.size - 1) {
                        withStyle(style = SpanStyle(fontSize = 12.sp, fontWeight = FontWeight.Normal)) {
                            append(" ou ")
                        }
                    }
                }
            }
        } else {
            AnnotatedString(awayDisplayFlag)
        }
    }

    // Agora usamos o status real vindo da API + verificação de tempo para garantir o destaque
    val isActuallyFinished = match.status == "FINISHED" || match.status == "PENALTIES" || match.status == "PAUSED_PENALTIES" || (isFinished && now > (matchStart + 7200_000L))
    val isLive = (match.status in listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")) || 
                 (now >= (matchStart - 60_000) && !isActuallyFinished)
    
    // Novo: Um jogo "fantasma" é aquele que aconteceu antes do bolão ser criado.
    // Ninguém poderia ter palpitado nele, então ele deve ser travado.
    val isGhostMatch = matchStart < bolaoCreatedAt

    val isTbd = (match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD") || 
                homeDisplayFlag.contains("ou") || 
                awayDisplayFlag.contains("ou")

    val canPredict = !isFinished && now < (match.matchDateMillis - 60_000) && !forceLocked && !isTbd

    val borderColor = when {
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

    val isExpired = now >= (match.matchDateMillis - 60_000) || isFinished
    val isLocked = isExpired || forceLocked || isGhostMatch || isTbd

    val cardBg = if (isLive) {
        Brush.verticalGradient(
            colors = listOf(NavyElevated, DeepNavy)
        )
    } else {
        null
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isLive) Color.Transparent else NavyElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLive) Neon.copy(alpha = 0.5f) else borderColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .then(if (cardBg != null) Modifier.background(cardBg) else Modifier)
                .clickable(
                    enabled = when {
                        isGhostMatch -> isAdmin 
                        canPredict -> true
                        isActuallyFinished -> isAdmin
                        isExpired -> (!isAdmin && showSocialBadge) || isAdmin
                        else -> false
                    },
                    onClick = { 
                        if (canPredict) onClick() 
                        else if (isAdmin) onAdminUpdateScore()
                        else if (isExpired && showSocialBadge) onShowAllPredictions()
                    }
                )
        ) {
            // 1. VER PALPITES DA GALERA (Grudado no teto)
            // Para Admin, mostramos sempre (mas com restrição de visualização se não começou)
            // Para os demais, apenas após o jogo travar/começar
            val showGaleraBadge = showSocialBadge && (isAdmin || isExpired) && !isTbd && !isGhostMatch
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
                            Icons.Default.Check,
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

            // 2. DATA E HORA e IDA SCORE
            if (!(isActuallyFinished && hasPrediction)) {
                Row(
                    modifier = Modifier
                        .align(if (!showGaleraBadge) Alignment.TopCenter else Alignment.TopEnd)
                        .padding(top = 10.dp, end = if (!showGaleraBadge) 0.dp else 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (idaScore != null) {
                        Surface(
                            color = Gold.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, Gold.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "IDA: $idaScore",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Gold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = formatMatchDate(match.matchDateMillis),
                        fontSize = 9.sp,
                        color = Color.White,
                        letterSpacing = 0.2.sp
                    )
                }
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
                        horizontalArrangement = if (homeDisplayName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp)
                    ) {
                        TeamIcon(
                            crestUrl = homeCrest ?: match.homeTeamCrest,
                            flag = homeAnnotatedFlag,
                            isTbd = isTbd,
                            size = 32.dp
                        )
                        if (homeDisplayName.isNotEmpty()) {
                            TeamNameText(
                                name = homeDisplayName,
                                modifier = Modifier.weight(1f)
                            )
                        }
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
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
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
                        horizontalArrangement = if (awayDisplayName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        if (awayDisplayName.isNotEmpty()) {
                            TeamNameText(
                                name = awayDisplayName,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.End
                            )
                        }
                        TeamIcon(
                            crestUrl = awayCrest ?: match.awayTeamCrest,
                            flag = awayAnnotatedFlag,
                            isTbd = isTbd,
                            size = 32.dp
                        )
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
                        val showEmBreve = (forceLocked || isTbd) && !match.isFinished
                        if (showEmBreve) {
                            Text(text = "EM BREVE VOCÊ PODERÁ PALPITAR", fontSize = 9.sp, fontWeight = FontWeight.Black, color = Neon.copy(alpha = 0.6f), letterSpacing = 0.5.sp, modifier = Modifier.padding(top = 8.dp))
                        } else {
                            val statusText = when {
                                isActuallyFinished -> "JOGO ENCERRADO"
                                match.status == "EXTRA_TIME" -> "PRORROGAÇÃO"
                                match.status == "PENALTIES" -> "PÊNALTIS"
                                match.status == "PAUSED_EXTRA_TIME" -> "INDO PARA PRORROGAÇÃO"
                                match.status == "PAUSED_PENALTIES" -> "INDO PARA PÊNALTIS"
                                match.status == "PAUSED" -> "INTERVALO"
                                else -> "JOGO EM ANDAMENTO"
                            }
                            val accentColor = when {
                                isActuallyFinished -> Color.White
                                isLive -> Neon // Placar real em andamento em verde
                                else -> Neon
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (isLive) {
                                    val infiniteTransition = rememberInfiniteTransition()
                                    val alpha by infiniteTransition.animateFloat(
                                        initialValue = 0.3f,
                                        targetValue = 1f,
                                        animationSpec = infiniteRepeatable(
                                            animation = tween(800, easing = LinearEasing),
                                            repeatMode = RepeatMode.Reverse
                                        )
                                    )
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Neon.copy(alpha = alpha))
                                    )
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    text = statusText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor.copy(alpha = 0.7f),
                                    letterSpacing = 0.8.sp
                                )
                            }

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
    onConfirm: (home: Int?, away: Int?) -> Unit
) {
    var homeStr by remember { mutableStateOf(match.homeScore?.toString() ?: "0") }
    var awayStr by remember { mutableStateOf(match.awayScore?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Ajustar Placar Oficial", color = Color.White) },
        text = {
            Column(modifier = Modifier.imePadding().padding(bottom = 24.dp)) {
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
                    val h = homeStr.toIntOrNull()
                    val a = awayStr.toIntOrNull()
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

@Composable
private fun PendingRequestItem(
    user: com.lpstudio.bolaodagalera.domain.model.User,
    label: String,
    accentColor: Color = Neon,
    onApprove: () -> Unit,
    onDeny: () -> Unit
) {
    Surface(
        color = NavyElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initials = user.name.getInitials(),
                size = 40.dp,
                fontSize = 14.sp,
                borderColor = accentColor.copy(alpha = 0.5f)
            )
            
            Spacer(Modifier.width(14.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.name,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = label,
                    color = accentColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDeny, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Negar", tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onApprove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Check, contentDescription = "Aprovar", tint = Neon, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Preview
@Composable
fun BolaoDetailScreenPreview() {
    val myUserId = "pauloricha"
    val mockBolao = com.lpstudio.bolaodagalera.domain.model.Bolao(
        id = "bolao-1",
        name = "Bolão da Libertadores",
        description = "Participe do maior bolão de futebol!",
        code = "LIB26",
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
            id = "GS-A-1", homeTeam = "River Plate", awayTeam = "Nacional",
            homeTeamCode = "RIV", awayTeamCode = "NAC", homeTeamFlag = "🇦🇷", awayTeamFlag = "🇺🇾",
            matchDateMillis = now - (2 * 60 * 60 * 1000), phase = Phase.GROUP_STAGE, group = "A",
            homeScore = 1, awayScore = 0
        ),
        Match(
            id = "GS-A-2", homeTeam = "Palmeiras", awayTeam = "River Plate",
            homeTeamCode = "PAL", awayTeamCode = "RIV", homeTeamFlag = "🐷", awayTeamFlag = "⚪️",
            matchDateMillis = now + (30 * 60 * 1000), phase = Phase.GROUP_STAGE, group = "A"
        ),
        Match(
            id = "GS-B-1", homeTeam = "Flamengo", awayTeam = "Peñarol",
            homeTeamCode = "FLA", awayTeamCode = "PEN", homeTeamFlag = "🔴", awayTeamFlag = "🟡",
            matchDateMillis = now + (24 * 60 * 60 * 1000), phase = Phase.GROUP_STAGE, group = "B"
        ),
        Match(
            id = "KO-1", homeTeam = "Atlético-MG", awayTeam = "Boca Juniors",
            homeTeamCode = "CAM", awayTeamCode = "BOC", homeTeamFlag = "🐔", awayTeamFlag = "🟦",
            matchDateMillis = now + (25 * 60 * 60 * 1000), phase = Phase.ROUND_OF_16
        )
    )

    val mockPredictions = mapOf(
        "GS-A-1" to Prediction(userId = myUserId, matchId = "GS-A-1", homeScore = 1, awayScore = 0)
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
            isAppOwner = true,
            launcherProvider = object : com.lpstudio.bolaodagalera.LauncherProvider {
                override fun shareText(text: String) {}
                override fun sendEmail(address: String, subject: String, body: String) {}
                override fun sendWhatsApp(phone: String, text: String) {}
            },
            onLeaveBolao = {},
            onApproveJoin = { _, _ -> },
            onApproveLeave = { _, _ -> },
            onNavigateToPrediction = {},
            onNavigateToAllPredictions = {},
            onNavigateToEdit = {},
            onNavigateToAddParticipants = {},
            onAdminUpdateScore = { _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
