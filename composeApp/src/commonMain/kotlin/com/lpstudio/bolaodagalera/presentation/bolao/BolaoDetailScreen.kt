package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import com.lpstudio.bolaodagalera.ADMOB_ANDROID_BANNER_ID
import com.lpstudio.bolaodagalera.ADMOB_IOS_BANNER_ID
import com.lpstudio.bolaodagalera.CommonBackHandler
import com.lpstudio.bolaodagalera.LauncherProvider
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.StandingsCalculator
import com.lpstudio.bolaodagalera.domain.model.User
import com.lpstudio.bolaodagalera.getPlatform
import com.lpstudio.bolaodagalera.presentation.components.AdBanner
import com.lpstudio.bolaodagalera.presentation.components.UserAvatar
import com.lpstudio.bolaodagalera.presentation.ranking.RankingScreen
import com.lpstudio.bolaodagalera.presentation.theme.AppTheme
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.GlassWhite
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.GradientHero
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.NavyElevated
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.OrangeNeon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

@Composable
fun BolaoDetailScreen(
    bolaoId: String,
    onNavigateToPrediction: (matchId: String) -> Unit,
    onNavigateToAllPredictions: (matchId: String) -> Unit,
    onNavigateToEdit: (bolaoId: String) -> Unit,
    onNavigateToAddParticipants: (bolaoId: String) -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel: BolaoViewModel = koinInject(parameters = { parametersOf(bolaoId) })
    val uiState by viewModel.uiState.collectAsState()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val userId = authRepository.currentUser?.id ?: ""
    val currentUser = authRepository.currentUser
    val isAppOwner =
        (currentUser?.username == "pauloricha") ||
            (currentUser?.email == "paulo.richa@hotmail.com") ||
            (userId == "pauloricha")
    val launcherProvider = rememberLauncherProvider()

    LaunchedEffect(userId) { viewModel.setUserId(userId) }
    LaunchedEffect(uiState.isLeaveSuccess) { if (uiState.isLeaveSuccess) onNavigateBack() }

    BolaoDetailContent(
        bolaoId = bolaoId, uiState = uiState, isOwner = uiState.bolao?.ownerId == userId,
        isAppOwner = isAppOwner, launcherProvider = launcherProvider, onLeaveBolao = { viewModel.leaveBolao() },
        onApproveJoin = { u, a -> viewModel.approveParticipant(u, a) }, onApproveLeave = { u, a -> viewModel.approveLeaveRequest(u, a) },
        onNavigateToPrediction = onNavigateToPrediction, onNavigateToAllPredictions = onNavigateToAllPredictions,
        onNavigateToEdit = onNavigateToEdit, onNavigateToAddParticipants = onNavigateToAddParticipants,
        onNavigateToHelp = onNavigateToHelp,
        onSaveAdminScore = { m, h, a -> viewModel.updateMatchScore(m, h, a) }, onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BolaoDetailContent(
    bolaoId: String,
    uiState: BolaoUiState,
    isOwner: Boolean,
    isAppOwner: Boolean,
    launcherProvider: LauncherProvider,
    onLeaveBolao: () -> Unit,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit,
    onNavigateToPrediction: (String) -> Unit,
    onNavigateToAllPredictions: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToAddParticipants: (String) -> Unit,
    onNavigateToHelp: () -> Unit,
    onSaveAdminScore: (String, Int?, Int?) -> Unit,
    onNavigateBack: () -> Unit
) {
    var showLeaveDialog by remember { mutableStateOf(false) }
    var showParticipantsSheet by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var lastInteractedMatchId by rememberSaveable { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val championship = Championship.fromId(uiState.bolao?.championshipId)

    val tabs =
        remember(uiState.bolao?.scope, uiState.bolao?.championshipId, championship) {
            when (uiState.bolao?.scope) {
                BolaoScope.ONLY_GROUPS -> listOf("Grupos", "Ranking")
                BolaoScope.ONLY_KNOCKOUT -> listOf("Mata-Mata", "Ranking")
                BolaoScope.PONTOS_CORRIDOS -> {
                    val list = mutableListOf("Pontos Corridos", "Ranking")
                    if (championship.hasStandings) list.add("Tabela")
                    list
                }
                else -> {
                    if (championship.isPointsBased) {
                        val list = mutableListOf("Pontos Corridos", "Ranking")
                        if (championship.hasStandings) list.add("Tabela")
                        list
                    } else if (championship.isGroupsAndKnockout) {
                        listOf("Grupos", "Mata-Mata", "Ranking")
                    } else {
                        listOf("Mata-Mata", "Ranking")
                    }
                }
            }
        }

    var selectedRound by rememberSaveable { mutableIntStateOf(0) }
    var selectedPhase by rememberSaveable { mutableStateOf<Phase?>(Phase.FRIENDLIES) }
    var selectedLabel by rememberSaveable(bolaoId) { mutableStateOf<String?>(null) }
    val groupsListState = rememberLazyListState()
    val knockoutListState = rememberLazyListState()
    val expandedGroups =
        rememberSaveable(
            bolaoId,
            saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
        ) { mutableStateListOf<String>() }
    var matchToUpdate by remember { mutableStateOf<Match?>(null) }

    matchToUpdate?.let { match ->
        AdminScoreDialog(
            match = match,
            onDismiss = { matchToUpdate = null },
            onConfirm = { h, a ->
                onSaveAdminScore(match.id, h, a)
                matchToUpdate = null
            }
        )
    }

    val knockoutDefaults = remember(uiState.matches, championship.isTwoLegged) {
        val phases = listOf(
            Phase.ROUND_OF_32,
            Phase.ROUND_OF_16,
            Phase.QUARTERFINALS,
            Phase.SEMIFINALS,
            Phase.THIRD_PLACE,
            Phase.FINAL
        )
        val phaseOrder = phases.filter { p -> uiState.matches.any { it.phase == p } }
        val labels = if (championship.isTwoLegged) {
            phaseOrder.flatMap { p ->
                if (p == Phase.FINAL || p == Phase.THIRD_PLACE) {
                    listOf(p.label)
                } else {
                    listOf("${p.label} - Ida", "${p.label} - Volta")
                }
            }
        } else {
            phaseOrder.map { it.label }
        }

        val tz = TimeZone.currentSystemDefault()
        val now = TimeSource.nowMillis()
        val today = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date

        val hasTodayKo = uiState.matches.filter { it.phase != Phase.GROUP_STAGE }.any {
            val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
            val mDate = mTime.date
            val isRecentlyFinished = now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L)
            mDate == today || (mDate.toEpochDays() == today.toEpochDays() + 1 && mTime.hour < 4) || isRecentlyFinished
        }

        if (hasTodayKo) {
            Phase.FRIENDLIES to "⚽️ HOJE"
        } else {
            val next = labels.find { l ->
                val base = l.substringBefore(" - ")
                val isV = l.contains("Volta")
                uiState.matches.any { m ->
                    m.phase.label == base && (if (isV) m.id.contains("-L2") else !m.id.contains("-L2")) && !m.isFinished
                }
            } ?: labels.lastOrNull()

            if (next != null) {
                val p = Phase.entries.find { it.label == next.substringBefore(" - ") }
                p to next
            } else {
                Phase.FRIENDLIES to "⚽️ HOJE"
            }
        }
    }
    val defaultPhase = knockoutDefaults.first
    val defaultLabel = knockoutDefaults.second

    val isFirstTab = selectedTab == 0
    val defaultRound =
        remember(uiState.matches) {
            val matchesGroupStage = uiState.matches.filter { it.phase == Phase.GROUP_STAGE }
            if (matchesGroupStage.isEmpty()) return@remember 0
            val tz = TimeZone.currentSystemDefault()
            val now = TimeSource.nowMillis()
            val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
            val hasMatchToday = matchesGroupStage.any {
                Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate
            }
            if (hasMatchToday) {
                0
            } else {
                val upcoming = matchesGroupStage
                    .filter { !it.isFinished && it.matchDateMillis > now }
                    .minByOrNull { it.matchDateMillis }
                    ?.groupRound()
                val lastR = matchesGroupStage.maxByOrNull {
                    it.matchDateMillis
                }?.groupRound() ?: 1
                upcoming ?: lastR
            }
        }

    val isInDefaultState = remember(
        selectedTab,
        selectedRound,
        defaultRound,
        selectedPhase,
        defaultPhase,
        selectedLabel,
        defaultLabel,
        tabs
    ) {
        val currentTabLabel = tabs.getOrNull(selectedTab)
        when (currentTabLabel) {
            "Grupos", "Jogos", "Rodadas", "Pontos Corridos" -> selectedRound == defaultRound
            "Mata-Mata" -> {
                selectedPhase == defaultPhase && (selectedLabel == defaultLabel || selectedLabel == null)
            }
            else -> false
        }
    }

    CommonBackHandler(enabled = !isFirstTab || !isInDefaultState) {
        if (!isFirstTab) {
            selectedTab = 0
        } else {
            val currentTabLabel = tabs.getOrNull(selectedTab)
            when (currentTabLabel) {
                "Grupos", "Jogos", "Rodadas", "Pontos Corridos" -> {
                    selectedRound = defaultRound
                }
                "Mata-Mata" -> {
                    selectedPhase = defaultPhase
                    selectedLabel = defaultLabel
                }
            }
        }
    }

    var hasAutoSelectedTab by rememberSaveable(bolaoId) { mutableStateOf(false) }
    LaunchedEffect(uiState.matches, defaultPhase, defaultLabel) {
        if (uiState.matches.isEmpty()) return@LaunchedEffect
        if (!hasAutoSelectedTab) {
            if (uiState.matches.all { it.isFinished }) {
                tabs.indexOf("Ranking").takeIf { it != -1 }?.let {
                    selectedTab = it
                    hasAutoSelectedTab = true
                    return@LaunchedEffect
                }
            }

            // Knockout auto-selection on first load
            if (selectedPhase == Phase.FRIENDLIES && defaultPhase != Phase.FRIENDLIES && defaultPhase != null) {
                selectedPhase = defaultPhase
                selectedLabel = defaultLabel
            }

            hasAutoSelectedTab = true
        }
        if (selectedRound == 0 && defaultRound != 0) selectedRound = defaultRound
    }

    if (showLeaveDialog) {
        AlertDialog(onDismissRequest = {
            showLeaveDialog = false
        }, containerColor = NavyCard, title = {
            Text("Sair do Bolão?", color = Color.White, fontWeight = FontWeight.Bold)
        }, text = {
            val msg = if (isOwner) {
                "Você é o dono deste bolão. Se sair, o bolão continuará existindo mas ficará sem administrador."
            } else {
                "O administrador precisará confirmar sua saída para que você seja removido do ranking."
            }
            Text(text = msg, color = TextMuted)
        }, confirmButton = {
            TextButton(onClick = {
                showLeaveDialog = false
                onLeaveBolao()
            }) {
                Text(
                    text = if (isOwner) "Sair" else "Pedir para sair",
                    color = ErrorRed,
                    fontWeight = FontWeight.Bold
                )
            }
        }, dismissButton = {
            TextButton(onClick = { showLeaveDialog = false }) {
                Text("Cancelar", color = TextMuted)
            }
        })
    }

    if (showParticipantsSheet) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showParticipantsSheet = false }) {
            Surface(
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.7f),
                color = NavyCard,
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    Text("Participantes", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White)
                    Text(
                        "${uiState.participants.size} pessoas no bolão",
                        fontSize = 13.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(vertical = 8.dp)
                        ) {
                            if (isOwner && (uiState.pendingJoinUsers.isNotEmpty() || uiState.pendingExitUsers.isNotEmpty())) {
                                item {
                                    Text(
                                        "Solicitações Pendentes",
                                        color = Gold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(
                                    uiState.pendingJoinUsers
                                ) { user ->
                                    PendingRequestItem(user = user, label = "Quer entrar", onApprove = {
                                        onApproveJoin(user.id, true)
                                    }, onDeny = { onApproveJoin(user.id, false) })
                                }
                                items(
                                    uiState.pendingExitUsers
                                ) { user ->
                                    PendingRequestItem(user = user, label = "Quer sair", accentColor = ErrorRed, onApprove = {
                                        onApproveLeave(user.id, true)
                                    }, onDeny = { onApproveLeave(user.id, false) })
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    Text(
                                        "Participantes",
                                        color = TextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                            items(uiState.participants.sortedBy { it.userName.lowercase() }) { p ->
                                val isOwnerP = p.userId == uiState.bolao?.ownerId
                                Surface(
                                    color = NavyElevated,
                                    shape = RoundedCornerShape(16.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        UserAvatar(
                                            initials = p.userName.getInitials(),
                                            size = 40.dp,
                                            fontSize = 14.sp,
                                            isOwner = isOwnerP,
                                            borderColor = if (isOwnerP) Gold else Neon.copy(alpha = 0.5f)
                                        )
                                        Spacer(Modifier.width(14.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = p.userName,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            if (p.userNickname.isNotBlank()) {
                                                Text(
                                                    text = "@${p.userNickname.lowercase()}",
                                                    color = TextMuted,
                                                    fontSize = 11.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Box(
                            modifier =
                            Modifier.fillMaxWidth().height(
                                20.dp
                            ).background(Brush.verticalGradient(listOf(NavyCard, Color.Transparent))).align(Alignment.TopCenter)
                        )
                        Box(
                            modifier =
                            Modifier.fillMaxWidth().height(
                                20.dp
                            ).background(Brush.verticalGradient(listOf(Color.Transparent, NavyCard))).align(Alignment.BottomCenter)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(
                            onClick = {
                                showParticipantsSheet = false
                            },
                            contentPadding =
                            PaddingValues(
                                horizontal = 16.dp
                            )
                        ) { Text("Fechar", color = Neon, fontWeight = FontWeight.Bold, fontSize = 15.sp) }
                    }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.matches.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Neon)
                    }
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth().background(GradientHero).padding(top = 16.dp, bottom = 16.dp)) {
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp).offset(x = (-10).dp)) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            "Voltar",
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    Text(
                                        uiState.bolao?.name ?: "Bolão",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f).padding(top = 4.dp).offset(x = (-8).dp)
                                    )

                                    IconButton(onClick = onNavigateToHelp, modifier = Modifier.size(36.dp)) {
                                        Icon(
                                            Icons.AutoMirrored.Outlined.HelpOutline,
                                            "Ajuda",
                                            tint = TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (isOwner) {
                                        IconButton(
                                            onClick = {
                                                uiState.bolao?.let { b ->
                                                    val web = "https://bolaodagalera-bb002.web.app/invite?code=${b.code}"
                                                    val app = "bolaodagalera://invite?code=${b.code}"
                                                    launcherProvider.shareText(
                                                        "Entre no meu bolão '${b.name}'! 🏆\n\nLink: $web\n\n" +
                                                            "Se o link não abrir o app automaticamente, use este: $app\n\n" +
                                                            "Código: ${b.code}"
                                                    )
                                                }
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) {
                                            Icon(
                                                Icons.Default.Share,
                                                "Compartilhar",
                                                tint = TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                onNavigateToAddParticipants(bolaoId)
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) { Icon(Icons.Default.PersonAdd, "Adicionar", tint = Neon, modifier = Modifier.size(20.dp)) }
                                        IconButton(
                                            onClick = {
                                                onNavigateToEdit(bolaoId)
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) { Icon(Icons.Default.Edit, "Editar", tint = Neon, modifier = Modifier.size(20.dp)) }
                                    } else {
                                        Box {
                                            IconButton(
                                                onClick = {
                                                    showMenu = true
                                                },
                                                modifier =
                                                Modifier.size(
                                                    36.dp
                                                )
                                            ) {
                                                Icon(
                                                    Icons.Default.MoreVert,
                                                    "Menu",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            DropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = {
                                                    showMenu = false
                                                },
                                                modifier =
                                                Modifier.background(
                                                    NavyCard
                                                ).border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                            ) {
                                                DropdownMenuItem(text = {
                                                    Text("Compartilhar", color = Color.White)
                                                }, leadingIcon = {
                                                    Icon(
                                                        Icons.Default.Share,
                                                        null,
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }, onClick = {
                                                    showMenu = false
                                                    uiState.bolao?.let { b ->
                                                        val web = "https://bolaodagalera-bb002.web.app/invite?code=${b.code}"
                                                        val app = "bolaodagalera://invite?code=${b.code}"
                                                        launcherProvider.shareText(
                                                            "Entre no meu bolão '${b.name}'! 🏆\n\nLink: $web\n\n" +
                                                                "Se o link não abrir o app automaticamente, use este: $app\n\n" +
                                                                "Código: ${b.code}"
                                                        )
                                                    }
                                                })
                                                DropdownMenuItem(text = {
                                                    Text("Sair do Bolão", color = ErrorRed)
                                                }, leadingIcon = {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.ExitToApp,
                                                        null,
                                                        tint = ErrorRed,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }, onClick = {
                                                    showMenu = false
                                                    showLeaveDialog = true
                                                })
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
                                    if (isOwner && (bolao.pendingParticipants.isNotEmpty() || bolao.pendingExits.isNotEmpty())) {
                                        Spacer(Modifier.height(12.dp))
                                        val pCount = bolao.pendingParticipants.size + bolao.pendingExits.size
                                        Surface(
                                            color =
                                            Gold.copy(
                                                alpha = 0.1f
                                            ),
                                            shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),
                                            border =
                                            androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                Gold.copy(alpha = 0.3f)
                                            ),
                                            modifier =
                                            Modifier.fillMaxWidth().clickable {
                                                showParticipantsSheet = true
                                            }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                                            ) {
                                                Text("⚠️", fontSize = 16.sp)
                                                Text(
                                                    "$pCount solicitações pendentes.",
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
                                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.Start),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(8.dp)
                                            ).background(
                                                Gold.copy(alpha = 0.15f)
                                            ).border(
                                                1.dp,
                                                Gold.copy(alpha = 0.4f),
                                                RoundedCornerShape(8.dp)
                                            ).padding(horizontal = 10.dp, vertical = 5.dp),
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
                                        Row(
                                            modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(8.dp)
                                            ).background(
                                                Neon.copy(alpha = 0.10f)
                                            ).border(1.dp, Neon.copy(alpha = 0.3f), RoundedCornerShape(8.dp)).clickable {
                                                showParticipantsSheet = true
                                            }.padding(
                                                horizontal = 10.dp,
                                                vertical = 5.dp
                                            ),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement =
                                            Arrangement.spacedBy(
                                                5.dp
                                            )
                                        ) {
                                            Text("👥", fontSize = 12.sp)
                                            Text(
                                                "${bolao.participants.size} participantes",
                                                fontSize = 12.sp,
                                                color = Neon,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        Row(
                                            modifier =
                                            Modifier.clip(
                                                RoundedCornerShape(8.dp)
                                            ).background(
                                                Color.White.copy(alpha = 0.05f)
                                            ).border(
                                                1.dp,
                                                GlassBorder,
                                                RoundedCornerShape(8.dp)
                                            ).padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Text("🏆", fontSize = 12.sp)
                                            Text(
                                                championship.displayName,
                                                fontSize = 12.sp,
                                                color = TextMuted,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                                Spacer(Modifier.height(12.dp))
                                Row(
                                    modifier =
                                    Modifier.fillMaxWidth().height(
                                        IntrinsicSize.Min
                                    ).clip(
                                        RoundedCornerShape(12.dp)
                                    ).background(NavyCard).border(1.dp, GlassBorder, RoundedCornerShape(12.dp)).padding(4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    tabs.forEachIndexed { index, label ->
                                        val selected = selectedTab == index
                                        val bg by animateColorAsState(
                                            if (selected) Neon else Color.Transparent,
                                            tween(200),
                                            label = "tab_bg_$index"
                                        )
                                        val txtColor by animateColorAsState(
                                            if (selected) DeepNavy else TextMuted,
                                            tween(200),
                                            label = "tab_text_$index"
                                        )
                                        Box(
                                            modifier =
                                            Modifier.weight(
                                                1f
                                            ).fillMaxHeight().clip(RoundedCornerShape(9.dp)).background(bg).clickable {
                                                selectedTab = index
                                            }.padding(vertical = 10.dp, horizontal = 4.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                label,
                                                color = txtColor,
                                                fontSize = if (tabs.size > 2 && label.length > 10) 13.sp else 14.sp,
                                                textAlign = TextAlign.Center,
                                                lineHeight = 16.sp,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        val filtered = remember(uiState.matches) {
                            uiState.matches.filter { it.phase != Phase.FRIENDLIES }
                        }
                        val groups = remember(filtered) {
                            filtered.filter { it.phase == Phase.GROUP_STAGE }
                        }
                        Box(Modifier.weight(1f)) {
                            when (tabs.getOrNull(selectedTab) ?: "Grupos") {
                                "Grupos", "Jogos", "Rodadas", "Pontos Corridos" ->
                                    GroupStageTab(
                                        matches = groups.ifEmpty { filtered },
                                        predictions = uiState.userPredictions,
                                        isLoading = uiState.isLoading,
                                        isAdmin = isAppOwner,
                                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                                        selectedRound = selectedRound,
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
                                        onOpenAdminScoreDialog = { matchToUpdate = it }
                                    )
                                "Mata-Mata" ->
                                    KnockoutTab(
                                        matches = filtered,
                                        predictions = uiState.userPredictions,
                                        isLoading = uiState.isLoading,
                                        isAdmin = isAppOwner,
                                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                                        selectedPhase = selectedPhase,
                                        onPhaseChange = { selectedPhase = it },
                                        selectedLabel = selectedLabel,
                                        onLabelChange = { selectedLabel = it },
                                        listState = knockoutListState,
                                        lastInteractedMatchId = lastInteractedMatchId,
                                        onClearLastMatchId = { lastInteractedMatchId = null },
                                        onMatchClick = {
                                            lastInteractedMatchId = it
                                            onNavigateToPrediction(it)
                                        },
                                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                                        onOpenAdminScoreDialog = { matchToUpdate = it },
                                        championship = championship
                                    )
                                "Ranking" -> RankingScreen(bolaoId = bolaoId)
                                "Tabela" -> {
                                    val champId = uiState.bolao?.championshipId ?: "UNKNOWN"
                                    StandingsTab(matches = uiState.allMatches.filter { it.championshipId == champId })
                                }
                            }
                        }
                    }
                }
            }
            val adId = if (getPlatform().name.lowercase().contains("android")) ADMOB_ANDROID_BANNER_ID else ADMOB_IOS_BANNER_ID
            AdBanner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy), adId = adId)
        }
    }
}

@Composable
private fun StandingsTab(matches: List<Match>) {
    val standings = remember(matches) { StandingsCalculator.calculate(matches) }
    if (standings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aguardando início dos jogos...", color = TextMuted) }
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
                            Text(
                                it,
                                modifier = Modifier.width(35.dp),
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            items(standings.size) { index ->
                val team = standings[index]
                val (name, flag, crest) =
                    remember(team.teamName, team.teamFlag, team.teamCrest, matches) {
                        resolveDisplayName("", team.teamName, team.teamFlag, matches, true)
                    }
                val isG4 = index < 4
                val isG5 = index == 4
                val isZ4 = index >= standings.size - 4 && standings.size > 5
                val accentColor =
                    when {
                        isG4 -> Neon
                        isG5 -> Gold
                        isZ4 -> ErrorRed
                        else -> null
                    }
                Surface(
                    color =
                    when {
                        isG4 -> Neon.copy(alpha = 0.05f)
                        isG5 -> Gold.copy(alpha = 0.05f)
                        isZ4 -> ErrorRed.copy(alpha = 0.05f)
                        else -> NavyCard
                    },
                    shape = RoundedCornerShape(12.dp),
                    border =
                    androidx.compose.foundation.BorderStroke(
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
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
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
                            Text(
                                "${team.points}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor ?: Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.played}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.won}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.goalDifference}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
                                color =
                                if (team.goalDifference > 0) {
                                    Neon
                                } else if (team.goalDifference < 0) {
                                    ErrorRed
                                } else {
                                    TextMuted
                                },
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Sentinela de [selectedRound] para a aba "Amanhã" (0 já é usado por "Hoje"). */
private const val TOMORROW_ROUND = -1

@Composable
private fun GroupStageTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    selectedRound: Int,
    onRoundChange: (Int) -> Unit,
    listState: LazyListState,
    expandedGroups: SnapshotStateList<String>,
    lastInteractedMatchId: String?,
    onClearLastMatchId: () -> Unit,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onOpenAdminScoreDialog: (Match) -> Unit
) {
    val unlocked = remember(matches) { matches.map { it.groupRound() }.toSet() }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val tomorrowDate = remember(todayDate) { kotlinx.datetime.LocalDate.fromEpochDays(todayDate.toEpochDays() + 1) }
    val hasMatchToday =
        remember(
            matches,
            todayDate
        ) { matches.any { Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate } }
    val hasMatchTomorrow =
        remember(
            matches,
            tomorrowDate
        ) { matches.any { Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == tomorrowDate } }
    val currentRound =
        remember(matches, now) {
            val upcoming = matches.filter { !it.isFinished && it.matchDateMillis > now }
                .minByOrNull { it.matchDateMillis }?.groupRound()
            upcoming ?: matches.maxByOrNull { it.matchDateMillis }?.groupRound() ?: 0
        }
    val roundMatches =
        remember(matches, selectedRound, todayDate, tomorrowDate, now) {
            if (selectedRound == 0) {
                matches.filter {
                    val mDate = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date
                    mDate == todayDate || (now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L))
                }.sortedBy { it.matchDateMillis }
            } else if (selectedRound == TOMORROW_ROUND) {
                matches.filter {
                    Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == tomorrowDate
                }.sortedBy { it.matchDateMillis }
            } else {
                matches.filter { it.groupRound() == selectedRound }.sortedBy { it.matchDateMillis }
            }
        }
    val byGroup = remember(roundMatches) { roundMatches.groupBy { it.group ?: "" } }
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    var hasHandledScroll by rememberSaveable(selectedRound) { mutableStateOf(false) }
    LaunchedEffect(selectedRound, matches.isNotEmpty(), byGroup, lastInteractedMatchId) {
        if (matches.isEmpty() || byGroup.isEmpty()) return@LaunchedEffect
        val sorted = byGroup.entries.sortedBy { it.key }
        if (lastInteractedMatchId != null) {
            val target = matches.find { it.id == lastInteractedMatchId }
            if (target != null) {
                if (selectedRound == 0 || selectedRound == TOMORROW_ROUND) {
                    onClearLastMatchId()
                    return@LaunchedEffect
                }
                val group = target.group ?: ""
                if (!expandedGroups.contains(group)) {
                    expandedGroups.add(group)
                    kotlinx.coroutines.delay(200.milliseconds)
                }
                var targetIdx = 0
                for (entry in sorted) {
                    if (entry.key == group) {
                        val ms = entry.value
                        val matchIdx = ms.indexOfFirst {
                            it.id == lastInteractedMatchId
                        }
                        targetIdx += 1 + (if (matchIdx != -1) matchIdx else 0)
                        break
                    }
                    targetIdx += 1 + entry.value.size + 1
                }
                listState.scrollToItem(targetIdx)
                hasHandledScroll = true
                onClearLastMatchId()
                return@LaunchedEffect
            }
        }
        if (!hasHandledScroll) {
            expandedGroups.clear()
            if (roundMatches.isNotEmpty() && roundMatches.all { it.isFinished } && selectedRound != 0) {
                listState.scrollToItem(0)
                hasHandledScroll = true
                return@LaunchedEffect
            }
            val window = 2 * 60 * 60 * 1000L + (30 * 60 * 1000L)
            val focus =
                matches.filter { it.phase == Phase.GROUP_STAGE }.let { all ->
                    all.find { now in it.matchDateMillis..(it.matchDateMillis + window) }
                        ?: all.filter {
                            val matchDate = Instant.fromEpochMilliseconds(it.matchDateMillis)
                                .toLocalDateTime(tz).date
                            matchDate == todayDate && it.matchDateMillis > now
                        }.minByOrNull {
                            it.matchDateMillis
                        } ?: all.filter {
                        it.matchDateMillis > now
                    }.minByOrNull { it.matchDateMillis }
                }
            if (focus != null) {
                val group = focus.group ?: ""
                val round = focus.groupRound()
                if (selectedRound == 0 || selectedRound == TOMORROW_ROUND) {
                    expandedGroups.addAll(byGroup.keys)
                    listState.scrollToItem(0)
                } else if (selectedRound == round) {
                    expandedGroups.add(group)
                    val actG = matches.filter {
                        val sR = it.groupRound() == selectedRound
                        val isT = Instant.fromEpochMilliseconds(it.matchDateMillis)
                            .toLocalDateTime(tz).date == todayDate
                        val isV = it.matchDateMillis + window >= now
                        sR && isT && isV && !it.isFinished
                    }.mapNotNull { it.group }
                    expandedGroups.addAll(actG)
                    var targetIdx = 0
                    for (entry in sorted) {
                        if (entry.key == group) break
                        targetIdx += 1 + entry.value.size + 1
                    }
                    listState.scrollToItem(targetIdx)
                } else {
                    sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
                    listState.scrollToItem(0)
                }
            } else {
                if (selectedRound == 0 || selectedRound == TOMORROW_ROUND) {
                    expandedGroups.addAll(byGroup.keys)
                } else {
                    sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
                }
                listState.scrollToItem(0)
            }
            hasHandledScroll = true
        }
    }
    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = 8.dp)) {
            RodadaSelector(
                selected = selectedRound,
                unlocked = unlocked,
                showHoje = hasMatchToday,
                showAmanha = hasMatchTomorrow,
                currentRound = currentRound,
                onSelect = {
                    if (it == 0 || it == TOMORROW_ROUND || it in unlocked) onRoundChange(it)
                }
            )
        }
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (roundMatches.isEmpty() && (selectedRound == 0 || selectedRound == TOMORROW_ROUND)) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            val msg = if (selectedRound == 0) {
                                "Nenhum jogo programado para hoje."
                            } else {
                                "Nenhum jogo programado para amanhã."
                            }
                            Text(msg, color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }
                if ((selectedRound == 0 || selectedRound == TOMORROW_ROUND) && roundMatches.isNotEmpty()) {
                    items(roundMatches, key = { it.id }) { m ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            MatchCard(
                                match = m,
                                prediction = predictions[m.id],
                                isAdmin = isAdmin,
                                bolaoCreatedAt = bolaoCreatedAt,
                                showSocialBadge = true,
                                allMatches = matches,
                                isTwoLegged = false,
                                onClick = {
                                    onMatchClick(m.id)
                                },
                                onShowAllPredictions = { onShowAllPredictions(m) },
                                onOpenAdminScoreDialog = { onOpenAdminScoreDialog(m) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    byGroup.entries.sortedBy { it.key }.forEach { (g, ms) ->
                        val isExp = expandedGroups.contains(g)
                        val isComp = ms.all { it.isFinished || predictions.containsKey(it.id) }
                        item(key = "header-$g") {
                            GroupHeader(group = g, isExpanded = isExp, isCompleted = isComp, enabled = true, onToggle = {
                                if (isExp) expandedGroups.remove(g) else expandedGroups.add(g)
                            })
                        }
                        items(ms, key = { it.id }) { m ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isExp,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    MatchCard(
                                        match = m,
                                        prediction = predictions[m.id],
                                        isAdmin = isAdmin,
                                        bolaoCreatedAt = bolaoCreatedAt,
                                        showSocialBadge = true,
                                        allMatches = matches,
                                        isTwoLegged = false, // Group stage is never two-legged for labels here
                                        onClick = { onMatchClick(m.id) },
                                        onShowAllPredictions = {
                                            onShowAllPredictions(m)
                                        },
                                        onOpenAdminScoreDialog = {
                                            onOpenAdminScoreDialog(m)
                                        }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                        item(key = "spacer-$g") { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = showShadow, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier =
                    Modifier.fillMaxWidth().height(
                        12.dp
                    ).background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)))
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
private fun KnockoutTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    selectedPhase: Phase?,
    onPhaseChange: (Phase?) -> Unit,
    selectedLabel: String?,
    onLabelChange: (String?) -> Unit,
    listState: LazyListState,
    lastInteractedMatchId: String?,
    onClearLastMatchId: () -> Unit,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onOpenAdminScoreDialog: (Match) -> Unit,
    championship: Championship = Championship.DEFAULT
) {
    if (isLoading && matches.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(color = Neon, strokeWidth = 2.dp) }
        return
    }
    val phaseOrder = remember(matches) {
        val allPhases = listOf(
            Phase.ROUND_OF_32,
            Phase.ROUND_OF_16,
            Phase.QUARTERFINALS,
            Phase.SEMIFINALS,
            Phase.THIRD_PLACE,
            Phase.FINAL
        )
        allPhases.filter { phase -> matches.any { it.phase == phase } }
    }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val hasMatchToday =
        remember(matches, todayDate) {
            matches.filter { it.phase != Phase.GROUP_STAGE }.any {
                val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
                val mDate = mTime.date
                mDate == todayDate ||
                    (mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < 4) ||
                    (now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L))
            }
        }
    val labels =
        remember(phaseOrder, championship.isTwoLegged) {
            if (championship.isTwoLegged) {
                phaseOrder.flatMap { phase ->
                    if (phase == Phase.FINAL || phase == Phase.THIRD_PLACE) {
                        listOf(
                            phase.label
                        )
                    } else {
                        listOf("${phase.label} - Ida", "${phase.label} - Volta")
                    }
                }
            } else {
                phaseOrder.map { it.label }
            }
        }
    val selLabel = selectedLabel
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }

    LaunchedEffect(matches, lastInteractedMatchId, selectedPhase) {
        // 1. Prioridade para Interação do Usuário (Volta de palpite)
        if (lastInteractedMatchId != null) {
            val target = matches.find { it.id == lastInteractedMatchId }
            if (target != null && target.phase != Phase.GROUP_STAGE) {
                if (selectedPhase == Phase.FRIENDLIES) {
                    onClearLastMatchId()
                    return@LaunchedEffect
                }
                onPhaseChange(target.phase)
                val newLabel =
                    if (championship.isTwoLegged) {
                        "${target.phase.label} - ${if (target.id.contains("-L2")) "Volta" else "Ida"}"
                    } else {
                        target.phase.label
                    }
                onLabelChange(newLabel)
                kotlinx.coroutines.delay(100.milliseconds)
                val current =
                    matches.filter {
                        if (championship.isTwoLegged) {
                            it.phase == target.phase && (if (target.id.contains("-L2")) it.id.contains("-L2") else !it.id.contains("-L2"))
                        } else {
                            it.phase == target.phase
                        }
                    }.sortedBy { it.id.split("-").lastOrNull()?.toIntOrNull() ?: 0 }
                val pairs = current.chunked(2)
                val pairIdx = pairs.indexOfFirst { pair -> pair.any { it.id == lastInteractedMatchId } }
                if (pairIdx != -1) listState.scrollToItem(pairIdx)
                onClearLastMatchId()
                return@LaunchedEffect
            }
        }

        // 2. Lógica de Auto-Seleção Inteligente
        val isFirstLoad = selLabel == null
        val isOnStartMarker = selectedPhase == Phase.FRIENDLIES

        if (isOnStartMarker || isFirstLoad) {
            if (hasMatchToday) {
                onLabelChange("⚽️ HOJE")
                onPhaseChange(Phase.FRIENDLIES)
            } else {
                val nextRelevantLabel =
                    labels.find { label ->
                        val base = label.substringBefore(" - ")
                        val isVolta = label.contains("Volta")
                        matches.any { m ->
                            m.phase.label == base &&
                                (if (isVolta) m.id.contains("-L2") else !m.id.contains("-L2")) &&
                                !m.isFinished
                        }
                    } ?: labels.lastOrNull()

                if (nextRelevantLabel != null) {
                    onLabelChange(nextRelevantLabel)
                    val base = nextRelevantLabel.substringBefore(" - ")
                    val phase = Phase.entries.find { it.label == base }
                    onPhaseChange(phase)
                }
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (labels.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = 8.dp)) {
                KnockoutPhaseSelector(
                    labels = labels,
                    selectedLabel = selectedLabel,
                    isUnlocked = true,
                    showHoje = hasMatchToday,
                    onSelect = { label ->
                        onLabelChange(label)
                        if (label == "⚽️ HOJE") {
                            onPhaseChange(Phase.FRIENDLIES)
                        } else {
                            val phaseName = label?.substringBefore(" - ")
                            val phase = Phase.entries.find { p -> p.label == phaseName }
                            onPhaseChange(phase)
                        }
                    }
                )
            }
        }

        Box(Modifier.weight(1f)) {
            val phaseMatches = remember(
                matches,
                selectedPhase,
                selectedLabel,
                championship.isTwoLegged,
                todayDate,
                now
            ) {
                if (selectedPhase == Phase.FRIENDLIES) {
                    matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
                        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis)
                            .toLocalDateTime(tz)
                        val mDate = mTime.date
                        val window = 3 * 3600_000L
                        val isTomorrowEarly = mDate.toEpochDays() == todayDate.toEpochDays() + 1 &&
                            mTime.hour < 4
                        val isRecentlyFinished = now in m.matchDateMillis..(m.matchDateMillis + window)
                        mDate == todayDate || isTomorrowEarly || isRecentlyFinished
                    }.sortedWith(
                        compareByDescending<Match> {
                            val statusLive = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
                            val isLive = it.status in statusLive
                            val isLocked = now >= (it.matchDateMillis - 60_000)
                            val isNotFin = it.status != "FINISHED"
                            if (isLive || (isLocked && isNotFin)) {
                                2
                            } else if (isNotFin) {
                                1
                            } else {
                                0
                            }
                        }.thenBy { it.matchDateMillis }
                    )
                } else if (championship.isTwoLegged &&
                    selectedLabel != null &&
                    selectedLabel != "⚽️ HOJE"
                ) {
                    val currentLabel = selectedLabel
                    val base = currentLabel.substringBefore(" - ")
                    val isVolta = currentLabel.contains("Volta")
                    val phaseMatchesFiltered = matches.filter { it.phase.label.equals(base, true) }
                    phaseMatchesFiltered.groupBy {
                        if (it.matchOrder > 0) {
                            it.matchOrder.toString()
                        } else {
                            val t1 = it.homeTeamCode
                            val t2 = it.awayTeamCode
                            val codes = listOf(t1, t2)
                            if (t1 != "TBD" &&
                                t2 != "TBD" &&
                                t1.isNotBlank() &&
                                t2.isNotBlank()
                            ) {
                                codes.sorted().joinToString("-")
                            } else {
                                it.id.substringBefore("-L")
                            }
                        }
                    }.values.mapNotNull { pair ->
                        if (isVolta) {
                            pair.filter { it.id.contains("-L2") }.maxByOrNull {
                                if (it.status == "FINISHED") {
                                    3
                                } else if (it.homeScore != null) {
                                    2
                                } else if (it.id.startsWith("CLI-2026")) {
                                    1
                                } else {
                                    0
                                }
                            }
                        } else {
                            pair.filter { !it.id.contains("-L2") }.maxByOrNull {
                                if (it.status == "FINISHED") {
                                    3
                                } else if (it.homeScore != null) {
                                    2
                                } else if (it.id.startsWith("CLI-2026")) {
                                    1
                                } else {
                                    0
                                }
                            }
                        }
                    }.sortedBy { it.matchOrder.takeIf { o -> o > 0 } ?: 99 }
                } else {
                    matches.filter { it.phase == selectedPhase }.sortedBy { it.matchDateMillis }
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
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Nenhum jogo de mata-mata hoje.", color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }
                items(phaseMatches, key = { it.id }) { m ->
                    MatchCard(
                        match = m,
                        prediction = predictions[m.id],
                        isAdmin = isAdmin,
                        bolaoCreatedAt = bolaoCreatedAt,
                        forceLocked = false,
                        showSocialBadge = true,
                        allMatches = matches,
                        isTwoLegged = championship.isTwoLegged,
                        onClick = { onMatchClick(m.id) },
                        onShowAllPredictions = {
                            onShowAllPredictions(m)
                        },
                        onOpenAdminScoreDialog = {
                            onOpenAdminScoreDialog(m)
                        }
                    )
                }
            }

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
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = Neon,
                        trackColor = Color.Transparent
                    )
                }
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
    val canScrollB by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    val canScrollF by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            if (last == null) {
                false
            } else {
                last.index < listState.layoutInfo.totalItemsCount - 1 || (last.offset + last.size) > listState.layoutInfo.viewportEndOffset
            }
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
            items(
                labels
            ) { l -> FilterChip(label = l, isSelected = selectedLabel == l, isUnlocked = isUnlocked, onClick = { onSelect(l) }) }
        }
        if (canScrollB) {
            Box(
                modifier =
                Modifier.align(
                    Alignment.CenterStart
                ).width(40.dp).matchParentSize().background(Brush.horizontalGradient(listOf(DeepNavy, Color.Transparent)))
            )
        }
        if (canScrollF) {
            Box(
                modifier =
                Modifier.align(
                    Alignment.CenterEnd
                ).width(40.dp).matchParentSize().background(Brush.horizontalGradient(listOf(Color.Transparent, DeepNavy)))
            )
        }
    }
}

@Composable
private fun RodadaSelector(
    selected: Int,
    unlocked: Set<Int>,
    showHoje: Boolean,
    showAmanha: Boolean,
    currentRound: Int,
    onSelect: (Int) -> Unit
) {
    val sorted = remember(unlocked) { unlocked.sorted() }
    val leadingTabs = (if (showHoje) 1 else 0) + (if (showAmanha) 1 else 0)
    val listState = rememberLazyListState()
    LaunchedEffect(selected, sorted) {
        if (sorted.isEmpty()) return@LaunchedEffect
        val target =
            when {
                selected == 0 && showHoje -> 0
                selected == TOMORROW_ROUND && showAmanha -> if (showHoje) 1 else 0
                selected > 0 -> {
                    val idx = sorted.indexOf(selected)
                    if (idx != -1) idx + leadingTabs else -1
                } else -> -1
            }
        if (target != -1) listState.animateScrollToItem(target)
    }
    androidx.compose.foundation.lazy.LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (showHoje) item { FilterChip(label = "⚽️ HOJE", isSelected = selected == 0, isUnlocked = true, onClick = { onSelect(0) }) }
        if (showAmanha) {
            item {
                FilterChip(
                    label = "AMANHÃ",
                    isSelected = selected == TOMORROW_ROUND,
                    isUnlocked = true,
                    onClick = { onSelect(TOMORROW_ROUND) }
                )
            }
        }
        items(sorted) { r ->
            FilterChip(
                label = "Rodada $r",
                isSelected = selected == r,
                isUnlocked = true,
                isPast = r < currentRound,
                isCurrent = r == currentRound,
                onClick = { onSelect(r) }
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
    isPast: Boolean = false,
    isCurrent: Boolean = false,
    onClick: () -> Unit
) {
    val bColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isCurrent -> Gold
            isPast -> Color.Transparent
            isUnlocked -> GlassBorder
            else -> Color.Transparent
        },
        label = "border_$label"
    )
    val cColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon.copy(alpha = 0.12f)
            isCurrent -> Gold.copy(alpha = 0.12f)
            isPast -> DeepNavy
            isUnlocked -> NavyElevated
            else -> NavyCard.copy(alpha = 0.5f)
        },
        label = "bg_$label"
    )
    val tColor by animateColorAsState(
        when {
            isSelected && isUnlocked -> Neon
            isCurrent -> Gold
            isPast -> TextMuted.copy(alpha = 0.55f)
            isUnlocked -> Color.White
            else -> TextMuted.copy(alpha = 0.4f)
        },
        label = "text_$label"
    )
    Box(
        modifier =
        modifier.clip(
            RoundedCornerShape(14.dp)
        ).background(cColor).border(1.dp, bColor, RoundedCornerShape(14.dp)).then(
            if (isUnlocked) {
                Modifier.clickable {
                    onClick()
                }
            } else {
                Modifier
            }
        ).padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            label,
            color = tColor,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
            maxLines = 1,
            textAlign = TextAlign.Center,
            softWrap = false
        )
    }
}

@Composable
private fun GroupHeader(group: String, isExpanded: Boolean, isCompleted: Boolean, enabled: Boolean = true, onToggle: () -> Unit) {
    val rot by animateFloatAsState(if (isExpanded) 90f else 0f, tween(200), label = "chevron_$group")
    val bColor by animateColorAsState(
        when {
            !enabled -> Color.Transparent
            isExpanded -> Neon.copy(alpha = 0.3f)
            else -> GlassBorder
        },
        label = "header_border_$group"
    )
    val bg =
        when {
            !enabled -> Brush.linearGradient(listOf(NavyCard.copy(alpha = 0.5f), NavyCard.copy(alpha = 0.5f)))
            isExpanded -> Brush.linearGradient(listOf(Neon.copy(alpha = 0.08f), Neon.copy(alpha = 0.02f)))
            else -> Brush.linearGradient(listOf(NavyElevated, NavyCard))
        }
    Column {
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(bg)
                .border(1.dp, bColor, RoundedCornerShape(16.dp))
                .then(if (enabled) Modifier.clickable(onClick = onToggle) else Modifier)
                .padding(horizontal = 18.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier =
                    Modifier.width(3.dp).height(16.dp).clip(RoundedCornerShape(2.dp)).background(
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
                    if (isCompleted) Text("✅", fontSize = 12.sp) else Text("⏳", fontSize = 12.sp)
                } else {
                    Text("🔒", fontSize = 10.sp, modifier = Modifier.padding(bottom = 1.dp))
                }
            }
            if (enabled) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    null,
                    tint = TextMuted,
                    modifier = Modifier.size(20.dp).rotate(rot)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun TeamNameText(name: String, modifier: Modifier = Modifier, textAlign: TextAlign = TextAlign.Start) {
    var fontSize by remember(name) { mutableIntStateOf(13) }
    var ready by remember(name) { mutableStateOf(false) }
    Text(
        text = name,
        modifier = modifier.drawWithContent {
            if (ready) drawContent()
        },
        fontSize = fontSize.sp,
        fontWeight = FontWeight.SemiBold,
        color = Color.White,
        maxLines = 1,
        softWrap = false,
        textAlign = textAlign,
        onTextLayout = { res ->
            if (res.hasVisualOverflow && fontSize > 8) fontSize -= 1 else ready = true
        }
    )
}

@Composable
private fun TeamIcon(crestUrl: String?, flag: AnnotatedString, isTbd: Boolean, size: androidx.compose.ui.unit.Dp = 32.dp) {
    val hasCrest = !crestUrl.isNullOrBlank()
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(if (hasCrest) Color.Transparent else NavyElevated.copy(alpha = 0.6f)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCrest) {
            SubcomposeAsyncImage(
                model =
                ImageRequest.Builder(
                    LocalPlatformContext.current
                ).data(
                    crestUrl
                ).decoderFactory(
                    SvgDecoder.Factory()
                ).crossfade(true).build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(0.8f),
                loading = {
                    CircularProgressIndicator(modifier = Modifier.size(size * 0.5f), strokeWidth = 1.dp, color = Neon)
                },
                error = { Text(text = flag, fontSize = (size.value * 0.7f).sp, fontWeight = FontWeight.Bold, color = Color.White) }
            )
        } else {
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
    isTwoLegged: Boolean = false,
    onShowAllPredictions: () -> Unit = {},
    onOpenAdminScoreDialog: () -> Unit = {},
    onClick: () -> Unit
) {
    val hasPrediction = prediction != null
    val isFinished = match.isFinished
    val now = TimeSource.nowMillis()
    val start = match.matchDateMillis
    val (hName, hFlag, hCrest) =
        remember(match.id, match.homeTeam, match.homeTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.homeTeam, match.homeTeamFlag, allMatches, true)
        }
    val (aName, aFlag, aCrest) =
        remember(match.id, match.awayTeam, match.awayTeamFlag, allMatches) {
            resolveDisplayName(match.id, match.awayTeam, match.awayTeamFlag, allMatches, false)
        }
    val isVolta = match.id.contains("-L2")
    val ida =
        remember(match.id, allMatches, isTwoLegged, isVolta) {
            if (isTwoLegged && isVolta) {
                val m =
                    allMatches.find { m ->
                        m.championshipId == match.championshipId &&
                            m.phase == match.phase &&
                            m.id != match.id &&
                            !m.id.contains("-L2") &&
                            (
                                (match.matchOrder > 0 && m.matchOrder == match.matchOrder) ||
                                    m.id.replace("-L1", "") == match.id.replace("-L2", "") ||
                                    (m.homeTeamCode == match.awayTeamCode && m.awayTeamCode == match.homeTeamCode)
                                )
                    }
                if (m != null && m.homeScore != null && m.awayScore != null) "${m.homeScore}×${m.awayScore}" else null
            } else {
                null
            }
        }
    val hAnn =
        remember(hFlag) {
            val p = hFlag.split(" ou ")
            if (p.size > 1) {
                buildAnnotatedString {
                    p.forEachIndexed { i, _ ->
                        append(p[i])
                        if (i < p.size - 1) {
                            withStyle(style = SpanStyle(fontSize = 12.sp)) {
                                append(" ou ")
                            }
                        }
                    }
                }
            } else {
                AnnotatedString(hFlag)
            }
        }
    val aAnn =
        remember(aFlag) {
            val p = aFlag.split(" ou ")
            if (p.size > 1) {
                buildAnnotatedString {
                    p.forEachIndexed { i, _ ->
                        append(p[i])
                        if (i < p.size - 1) {
                            withStyle(style = SpanStyle(fontSize = 12.sp)) {
                                append(" ou ")
                            }
                        }
                    }
                }
            } else {
                AnnotatedString(aFlag)
            }
        }
    val isFin = match.status == "FINISHED" ||
        match.status == "PENALTIES" ||
        match.status == "PAUSED_PENALTIES" ||
        (
            match.homeScore != null &&
                match.awayScore != null &&
                now > (start + 3 * 3600_000L)
            )
    val statusLive = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
    val isLive = !isFin &&
        (
            match.status in statusLive ||
                (now >= (start - 60_000) && now < (start + 3 * 3600_000L))
            )
    val isGhost = start < bolaoCreatedAt
    val isTbd = (match.homeTeamCode == "TBD" || match.awayTeamCode == "TBD") || hFlag.contains("ou") || aFlag.contains("ou")
    val canPred = !isFinished && now < (match.matchDateMillis - 60_000) && !forceLocked && !isTbd
    val bColor =
        when {
            isFin && hasPrediction -> {
                val hR = match.homeScore ?: 0
                val aR = match.awayScore ?: 0
                val hP = prediction.homeScore
                val aP = prediction.awayScore
                val pts = when {
                    hP == hR && aP == aR -> 3
                    (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> 1
                    else -> 0
                }
                when (pts) {
                    3 -> Neon
                    1 -> Gold
                    else -> ErrorRed
                }
            }
            hasPrediction -> Gold.copy(alpha = 0.4f)
            else -> GlassBorder
        }
    val isExp = now >= (match.matchDateMillis - 60_000) || isFinished
    val isLock = isExp || forceLocked || isGhost || isTbd
    val cardBg = if (isLive) Brush.verticalGradient(listOf(NavyElevated, DeepNavy)) else null
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (isLive) Color.Transparent else NavyElevated,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isLive) Neon.copy(alpha = 0.5f) else bColor)
    ) {
        Box(
            modifier =
            Modifier.fillMaxWidth().then(if (cardBg != null) Modifier.background(cardBg) else Modifier).clickable(
                enabled =
                when {
                    isGhost -> isAdmin
                    canPred -> true
                    isFin -> isAdmin
                    isExp -> (!isAdmin && showSocialBadge) || isAdmin
                    else -> false
                },
                onClick = {
                    if (canPred) {
                        onClick()
                    } else if (isAdmin) {
                        onOpenAdminScoreDialog()
                    } else if (isExp && showSocialBadge) {
                        onShowAllPredictions()
                    }
                }
            )
        ) {
            val showGalera = showSocialBadge && (isAdmin || isExp) && !isTbd && !isGhost
            if (showGalera) {
                Surface(
                    onClick = onShowAllPredictions,
                    color = OrangeNeon.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(
                        bottomStart = 10.dp,
                        bottomEnd = 10.dp
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        OrangeNeon.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-6).dp)
                ) {
                    Row(
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 5.dp
                        ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Check, null, tint = OrangeNeon, modifier = Modifier.size(12.dp))
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

            if (ida != null) {
                Surface(
                    color = Gold.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(bottomEnd = 10.dp, topStart = 12.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Gold.copy(alpha = 0.3f)),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "IDA: $ida",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Gold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            if (!(isFin && hasPrediction)) {
                Text(
                    // Enquanto o confronto não estiver confirmado (times TBD), não
                    // mostra a data mesmo que a API já tenha publicado uma para o
                    // "slot" da fase - evita sugerir um confronto que ainda não existe.
                    text = if (isTbd) "Data a definir" else formatMatchDate(match.matchDateMillis),
                    fontSize = 9.sp,
                    color = Color.White,
                    letterSpacing = 0.2.sp,
                    modifier =
                    Modifier
                        .align(if (!showGalera) Alignment.TopCenter else Alignment.TopEnd)
                        .padding(top = 10.dp, end = if (!showGalera) 0.dp else 12.dp)
                )
            }
            if (isFin && hasPrediction) {
                val hR = match.homeScore ?: 0
                val aR = match.awayScore ?: 0
                val hP = prediction.homeScore
                val aP = prediction.awayScore
                val pts =
                    when {
                        hP == hR && aP == aR -> 3
                        (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> 1
                        else -> 0
                    }
                Surface(
                    color =
                    when (pts) {
                        3 -> Neon.copy(alpha = 0.15f)
                        1 -> Gold.copy(alpha = 0.15f)
                        else -> ErrorRed.copy(alpha = 0.1f)
                    },
                    shape = RoundedCornerShape(bottomStart = 10.dp, topEnd = 16.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (pts == 1) "+1 PONTO" else "+$pts PONTOS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color =
                        when (pts) {
                            3 -> Neon
                            1 -> Gold
                            else -> ErrorRed
                        },
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Spacer(Modifier.height(if (isLock || canPred) 32.dp else 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = if (hName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp)
                    ) {
                        TeamIcon(crestUrl = hCrest ?: match.homeTeamCrest, flag = hAnn, isTbd = isTbd, size = 32.dp)
                        if (hName.isNotEmpty()) TeamNameText(name = hName, modifier = Modifier.weight(1f))
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                        if (hasPrediction) {
                            val hR = match.homeScore ?: 0
                            val aR = match.awayScore ?: 0
                            val hP = prediction.homeScore
                            val aP = prediction.awayScore
                            val sColor = when {
                                !isFin && !canPred -> TextMuted
                                !isLock -> Gold
                                hP == hR && aP == aR -> Neon
                                (hP > aP && hR > aR) || (hP < aP && hR < aR) || (hP == aP && hR == aR) -> Gold
                                else -> ErrorRed
                            }
                            val isExact = isFin && hP == hR && aP == aR
                            Box(
                                modifier =
                                Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Brush.linearGradient(listOf(sColor.copy(0.15f), sColor.copy(0.05f))))
                                    .then(if (isExact) Modifier.border(2.dp, Neon, RoundedCornerShape(12.dp)) else Modifier)
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("$hP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = sColor)
                                    Text(
                                        "×",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = sColor.copy(alpha = 0.6f),
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                    Text("$aP", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = sColor)
                                }
                            }
                        } else {
                            Box(
                                modifier =
                                Modifier
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
                        horizontalArrangement = if (aName.isEmpty()) Arrangement.Center else Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        if (aName.isNotEmpty()) TeamNameText(name = aName, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        TeamIcon(crestUrl = aCrest ?: match.awayTeamCrest, flag = aAnn, isTbd = isTbd, size = 32.dp)
                    }
                }
                if (canPred) {
                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier =
                        Modifier.fillMaxWidth().clip(
                            RoundedCornerShape(10.dp)
                        ).background(Neon.copy(alpha = 0.08f)).padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, null, modifier = Modifier.size(13.dp), tint = Neon)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (hasPrediction) "EDITAR PALPITE" else "TOQUE PARA PALPITAR",
                            fontSize = 11.sp,
                            color = Neon,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                } else if (isLock) {
                    val dColor = if (isLive) Neon.copy(alpha = 0.3f) else GlassBorder
                    Spacer(Modifier.height(14.dp))
                    HorizontalDivider(color = dColor, thickness = 0.5.dp)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if ((forceLocked || isTbd) && !match.isFinished) {
                            Text(
                                text = "EM BREVE VOCÊ PODERÁ PALPITAR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Neon.copy(alpha = 0.6f),
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else {
                            val sT =
                                when {
                                    isFin -> "JOGO ENCERRADO"
                                    match.status == "EXTRA_TIME" -> "PRORROGAÇÃO"
                                    match.status == "PENALTIES" -> "PÊNALTIS"
                                    match.status == "PAUSED_EXTRA_TIME" -> "INDO PARA PRORROGAÇÃO"
                                    match.status == "PAUSED_PENALTIES" -> "INDO PARA PÊNALTIS"
                                    match.status == "PAUSED" -> "INTERVALO"
                                    else -> "JOGO EM ANDAMENTO"
                                }
                            val aC = if (isFin) Color.White else Neon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                if (isLive) {
                                    val inf = rememberInfiniteTransition()
                                    val alpha by inf.animateFloat(
                                        0.3f,
                                        1f,
                                        infiniteRepeatable(tween(800, easing = LinearEasing), RepeatMode.Reverse)
                                    )
                                    Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Neon.copy(alpha = alpha)))
                                    Spacer(Modifier.width(6.dp))
                                }
                                Text(
                                    text = sT,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = aC.copy(alpha = 0.7f),
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Box(
                                modifier =
                                Modifier.padding(
                                    top = 2.dp
                                ).clip(RoundedCornerShape(6.dp)).background(aC.copy(alpha = 0.08f)).then(
                                    if (isAdmin) {
                                        Modifier.clickable {
                                            onOpenAdminScoreDialog()
                                        }
                                    } else {
                                        Modifier
                                    }
                                ).padding(horizontal = 8.dp, vertical = 1.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("${match.homeScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = aC)
                                    Text(
                                        "×",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = aC.copy(alpha = 0.5f),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )
                                    Text("${match.awayScore ?: 0}", fontSize = 16.sp, fontWeight = FontWeight.Black, color = aC)
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
fun AdminScoreDialog(match: Match, onDismiss: () -> Unit, onConfirm: (Int?, Int?) -> Unit) {
    var hS by remember { mutableStateOf(match.homeScore?.toString() ?: "0") }
    var aS by remember { mutableStateOf(match.awayScore?.toString() ?: "0") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Ajustar Placar Oficial", color = Color.White) }, text = {
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
                    value = hS,
                    onValueChange = {
                        if (it.length <= 2) {
                            hS =
                                it.filter { c ->
                                    c.isDigit()
                                }
                        }
                    },
                    modifier =
                    Modifier.width(
                        64.dp
                    ),
                    textStyle =
                    LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                    )
                )
                Text("x", modifier = Modifier.padding(horizontal = 16.dp), color = Color.White, fontWeight = FontWeight.Bold)
                TextField(
                    value = aS,
                    onValueChange = {
                        if (it.length <= 2) {
                            aS =
                                it.filter { c ->
                                    c.isDigit()
                                }
                        }
                    },
                    modifier =
                    Modifier.width(
                        64.dp
                    ),
                    textStyle =
                    LocalTextStyle.current.copy(
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    ),
                    colors =
                    TextFieldDefaults.colors(
                        focusedContainerColor = GlassWhite,
                        unfocusedContainerColor = GlassWhite.copy(alpha = 0.5f)
                    )
                )
            }
        }
    }, confirmButton = {
        TextButton(onClick = {
            onConfirm(hS.toIntOrNull() ?: 0, aS.toIntOrNull() ?: 0)
        }) {
            Text("SALVAR", color = Neon, fontWeight = FontWeight.Bold)
        }
    }, dismissButton = {
        TextButton(onClick = onDismiss) {
            Text("CANCELAR", color = Color.White.copy(alpha = 0.6f))
        }
    }, containerColor = DeepNavy, shape = RoundedCornerShape(16.dp))
}

@Composable
private fun PendingRequestItem(user: User, label: String, accentColor: Color = Neon, onApprove: () -> Unit, onDeny: () -> Unit) {
    Surface(
        color = NavyElevated,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                initials = user.name.take(1).uppercase(),
                size = 40.dp,
                fontSize = 14.sp,
                borderColor = accentColor.copy(alpha = 0.5f)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = user.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                Text(text = label, color = accentColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onDeny, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, null, tint = ErrorRed, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onApprove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Check, null, tint = Neon, modifier = Modifier.size(20.dp))
            }
        }
    }
}

private fun formatMatchDate(millis: Long): String {
    if (millis == Match.NO_DATE_MILLIS) return "Data a definir"
    val tz = TimeZone.currentSystemDefault()
    val dt = Instant.fromEpochMilliseconds(millis).toLocalDateTime(tz)
    val dayOfWeek =
        when (dt.dayOfWeek) {
            DayOfWeek.MONDAY -> "SEG"
            DayOfWeek.TUESDAY -> "TER"
            DayOfWeek.WEDNESDAY -> "QUA"
            DayOfWeek.THURSDAY -> "QUI"
            DayOfWeek.FRIDAY -> "SEX"
            DayOfWeek.SATURDAY -> "SÁB"
            DayOfWeek.SUNDAY -> "DOM"
        }
    val monthName =
        when (dt.month) {
            Month.JANUARY -> "JAN"
            Month.FEBRUARY -> "FEV"
            Month.MARCH -> "MAR"
            Month.APRIL -> "ABR"
            Month.MAY -> "MAI"
            Month.JUNE -> "JUN"
            Month.JULY -> "JUL"
            Month.AUGUST -> "AGO"
            Month.SEPTEMBER -> "SET"
            Month.OCTOBER -> "OUT"
            Month.NOVEMBER -> "NOV"
            Month.DECEMBER -> "DEZ"
        }
    val dayValue = dt.dayOfMonth.toString().padStart(2, '0')
    val hour = dt.hour.toString().padStart(2, '0')
    val minute = dt.minute.toString().padStart(2, '0')

    return "$dayOfWeek, $dayValue $monthName $hour:$minute"
}

@Preview
@Composable
fun BolaoDetailScreenPreview() {
    val myUserId = "pauloricha"
    val mockBolao =
        Bolao(
            id = "bolao-1",
            name = "Bolão da Libertadores",
            description = "Participe do maior bolão de futebol!",
            code = "LIB26",
            ownerId = myUserId,
            participants = listOf(myUserId, "user-2"),
            createdAtMillis = 1781136000000L
        )
    val mockParticipants =
        listOf(
            RankingEntry(myUserId, "Paulo George Moreira Richa", "Paulão", 10, 2, 4),
            RankingEntry("user-2", "Maria Silva", "Maria", 8, 1, 5)
        )
    val now = TimeSource.nowMillis()
    val mockMatches =
        listOf(
            Match(
                id = "GS-A-1",
                homeTeam = "River Plate",
                awayTeam = "Nacional",
                homeTeamCode = "RIV",
                awayTeamCode = "NAC",
                homeTeamFlag = "🇦🇷",
                awayTeamFlag = "🇺🇾",
                matchDateMillis = now - (2 * 60 * 60 * 1000),
                phase = Phase.GROUP_STAGE,
                group = "A",
                homeScore = 1,
                awayScore = 0
            ),
            Match(
                id = "GS-A-2",
                homeTeam = "Palmeiras",
                awayTeam = "River Plate",
                homeTeamCode = "PAL",
                awayTeamCode = "RIV",
                homeTeamFlag = "🐷",
                awayTeamFlag = "⚪️",
                matchDateMillis = now + (30 * 60 * 1000),
                phase = Phase.GROUP_STAGE,
                group = "A"
            ),
            Match(
                id = "GS-B-1",
                homeTeam = "Flamengo",
                awayTeam = "Peñarol",
                homeTeamCode = "FLA",
                awayTeamCode = "PEN",
                homeTeamFlag = "🔴",
                awayTeamFlag = "🟡",
                matchDateMillis = now + (24 * 60 * 60 * 1000),
                phase = Phase.GROUP_STAGE,
                group = "B"
            ),
            Match(
                id = "KO-1",
                homeTeam = "Atlético-MG",
                awayTeam = "Boca Juniors",
                homeTeamCode = "CAM",
                awayTeamCode = "BOC",
                homeTeamFlag = "🐔",
                awayTeamFlag = "🟦",
                matchDateMillis = now + (25 * 60 * 60 * 1000),
                phase = Phase.ROUND_OF_16
            )
        )
    val mockPredictions = mapOf("GS-A-1" to Prediction(userId = myUserId, matchId = "GS-A-1", homeScore = 1, awayScore = 0))
    val uiState =
        BolaoUiState(
            bolao = mockBolao,
            matches = mockMatches,
            userPredictions = mockPredictions,
            participants = mockParticipants,
            isLoading = false
        )
    AppTheme {
        BolaoDetailContent(
            bolaoId = "bolao-1", uiState = uiState, isOwner = true, isAppOwner = true,
            launcherProvider =
            object : LauncherProvider {
                override fun shareText(text: String) {}

                override fun sendEmail(address: String, subject: String, body: String) {}

                override fun sendWhatsApp(phone: String, text: String) {}
            },
            onLeaveBolao = {}, onApproveJoin = {
                    _,
                    _
                ->
            }, onApproveLeave = {
                    _,
                    _
                ->
            }, onNavigateToPrediction = {
            },
            onNavigateToAllPredictions = {},
            onNavigateToEdit = {},
            onNavigateToAddParticipants = {},
            onNavigateToHelp = {},
            onSaveAdminScore = { _, _, _ -> },
            onNavigateBack = {}
        )
    }
}
