package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_phase_first_leg
import bolaodagalera.feature_bolao.generated.resources.bolao_common_phase_second_leg
import bolaodagalera.feature_bolao.generated.resources.bolao_common_today_chip
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_add_participant_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_close_button
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_default_name
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_edit_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_help_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_key_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_leave_dialog_confirm_member
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_leave_dialog_confirm_owner
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_leave_dialog_message_member
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_leave_dialog_message_owner
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_leave_dialog_title
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_menu_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_menu_leave
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_menu_share
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_nav_back_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_participants_chip
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_participants_count
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_participants_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_participants_title
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_pending_count_message
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_pending_join_label
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_pending_leave_label
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_pending_requests_title
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_pending_view_button
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_share_cd
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_share_message
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_grupos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_jogos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_mata_mata
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_pontos_corridos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_ranking
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_rodadas
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_tabela
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_trophy_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_warning_emoji
import com.lpstudio.bolaodagalera.CommonBackHandler
import com.lpstudio.bolaodagalera.LauncherProvider
import com.lpstudio.bolaodagalera.ads.AdBannerProvider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoConfirmDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDropdownMenu
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDropdownMenuItem
import com.lpstudio.bolaodagalera.designsystem.components.BolaoFullScreenLoading
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.UserAvatar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientHero
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.presentation.ranking.RankingScreen
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.util.TimeSource
import com.lpstudio.bolaodagalera.util.getInitials
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
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
    val viewModel: BolaoViewModel = koinViewModel(key = bolaoId) { parametersOf(bolaoId) }
    val uiState by viewModel.uiState.collectAsState()
    val authRepository = koinInject<com.lpstudio.bolaodagalera.domain.repository.AuthRepository>()
    val userId = authRepository.currentUser?.id ?: ""
    // Mesmo uid usado como fonte da verdade em firestore.rules (isAdmin()) -
    // username/e-mail são campos que o próprio usuário pode editar em
    // users/{uid}, então não servem como checagem de admin no client.
    val isAppOwner = userId == "Uf3tNfKKE3hnQ7xhLxaZpW1QyIC2"
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
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    val championship = Championship.fromId(uiState.bolao?.championshipId)

    val todayLabel = stringResource(Res.string.bolao_common_today_chip)
    val tabGrupos = stringResource(Res.string.bolao_detail_tab_grupos)
    val tabRanking = stringResource(Res.string.bolao_detail_tab_ranking)
    val tabMataMata = stringResource(Res.string.bolao_detail_tab_mata_mata)
    val tabPontosCorridos = stringResource(Res.string.bolao_detail_tab_pontos_corridos)
    val tabTabela = stringResource(Res.string.bolao_detail_tab_tabela)
    val tabJogos = stringResource(Res.string.bolao_detail_tab_jogos)
    val tabRodadas = stringResource(Res.string.bolao_detail_tab_rodadas)
    val firstLegFormat = stringResource(Res.string.bolao_common_phase_first_leg, "%1\$s")
    val secondLegFormat = stringResource(Res.string.bolao_common_phase_second_leg, "%1\$s")

    val tabs =
        remember(
            uiState.bolao?.scope,
            uiState.bolao?.championshipId,
            championship,
            tabGrupos,
            tabRanking,
            tabMataMata,
            tabPontosCorridos,
            tabTabela
        ) {
            when (uiState.bolao?.scope) {
                BolaoScope.ONLY_GROUPS -> listOf(tabGrupos, tabRanking)
                BolaoScope.ONLY_KNOCKOUT -> listOf(tabMataMata, tabRanking)
                BolaoScope.PONTOS_CORRIDOS -> {
                    val list = mutableListOf(tabPontosCorridos, tabRanking)
                    if (championship.hasStandings) list.add(tabTabela)
                    list
                }
                else -> {
                    if (championship.isPointsBased) {
                        val list = mutableListOf(tabPontosCorridos, tabRanking)
                        if (championship.hasStandings) list.add(tabTabela)
                        list
                    } else if (championship.isGroupsAndKnockout) {
                        listOf(tabGrupos, tabMataMata, tabRanking)
                    } else {
                        listOf(tabMataMata, tabRanking)
                    }
                }
            }
        }

    var selectedRound by rememberSaveable { mutableIntStateOf(0) }
    var selectedPhase by rememberSaveable { mutableStateOf<Phase?>(Phase.FRIENDLIES) }
    var selectedLabel by rememberSaveable(bolaoId) { mutableStateOf<String?>(null) }
    // Saveable (não só remember) para que a posição de rolagem sobreviva à
    // navegação pra tela de palpite e volta sem precisar recalcular/forçar
    // um scroll manual - a tela volta exatamente como o usuário deixou.
    val groupsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val knockoutListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
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

    val knockoutDefaults = remember(uiState.matches, championship.isTwoLegged, firstLegFormat, secondLegFormat, todayLabel) {
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
                    listOf(
                        firstLegFormat.replace("%1\$s", p.label),
                        secondLegFormat.replace("%1\$s", p.label)
                    )
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
            Phase.FRIENDLIES to todayLabel
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
                Phase.FRIENDLIES to todayLabel
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
            tabGrupos, tabJogos, tabRodadas, tabPontosCorridos -> selectedRound == defaultRound
            tabMataMata -> {
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
                tabGrupos, tabJogos, tabRodadas, tabPontosCorridos -> {
                    selectedRound = defaultRound
                }
                tabMataMata -> {
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
                tabs.indexOf(tabRanking).takeIf { it != -1 }?.let {
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
        val leaveMessage =
            if (isOwner) {
                stringResource(Res.string.bolao_detail_leave_dialog_message_owner)
            } else {
                stringResource(Res.string.bolao_detail_leave_dialog_message_member)
            }
        BolaoConfirmDialog(
            title = stringResource(Res.string.bolao_detail_leave_dialog_title),
            message = leaveMessage,
            confirmText =
            if (isOwner) {
                stringResource(Res.string.bolao_detail_leave_dialog_confirm_owner)
            } else {
                stringResource(Res.string.bolao_detail_leave_dialog_confirm_member)
            },
            isDestructive = true,
            onConfirm = {
                showLeaveDialog = false
                onLeaveBolao()
            },
            onDismiss = { showLeaveDialog = false }
        )
    }

    if (showParticipantsSheet) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showParticipantsSheet = false }) {
            BolaoSurface(
                modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.7f),
                color = NavyCard,
                shape = RoundedCornerShape(28.dp),
                border = BorderStroke(1.dp, GlassBorder)
            ) {
                Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
                    BolaoText(
                        stringResource(Res.string.bolao_detail_participants_title),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                    BolaoText(
                        stringResource(Res.string.bolao_detail_participants_count, uiState.participants.size),
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
                                    BolaoText(
                                        stringResource(Res.string.bolao_detail_pending_requests_title),
                                        color = Gold,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                                items(
                                    uiState.pendingJoinUsers
                                ) { user ->
                                    PendingRequestItem(
                                        user = user,
                                        label = stringResource(Res.string.bolao_detail_pending_join_label),
                                        onApprove = {
                                            onApproveJoin(user.id, true)
                                        },
                                        onDeny = { onApproveJoin(user.id, false) }
                                    )
                                }
                                items(
                                    uiState.pendingExitUsers
                                ) { user ->
                                    PendingRequestItem(
                                        user = user,
                                        label = stringResource(Res.string.bolao_detail_pending_leave_label),
                                        accentColor = ErrorRed,
                                        onApprove = {
                                            onApproveLeave(user.id, true)
                                        },
                                        onDeny = { onApproveLeave(user.id, false) }
                                    )
                                }
                                item {
                                    Spacer(Modifier.height(16.dp))
                                    BolaoText(
                                        stringResource(Res.string.bolao_detail_participants_title),
                                        color = TextMuted,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    )
                                }
                            }
                            items(uiState.participants.sortedBy { it.userName.lowercase() }) { p ->
                                val isOwnerP = p.userId == uiState.bolao?.ownerId
                                BolaoSurface(
                                    color = NavyElevated,
                                    shape = RoundedCornerShape(16.dp),
                                    border = BorderStroke(1.dp, GlassBorder)
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
                                            BolaoText(
                                                text = p.userName,
                                                color = Color.White,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1
                                            )
                                            if (p.userNickname.isNotBlank()) {
                                                BolaoText(
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
                        BolaoTextButton(
                            onClick = {
                                showParticipantsSheet = false
                            },
                            contentPadding =
                            PaddingValues(
                                horizontal = 16.dp
                            )
                        ) {
                            BolaoText(
                                stringResource(Res.string.bolao_detail_close_button),
                                color = Neon,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        }
    }

    val shareMessage = uiState.bolao?.let { b ->
        val web = "https://bolaodagalera-bb002.web.app/invite?code=${b.code}"
        val app = "bolaodagalera://invite?code=${b.code}"
        stringResource(Res.string.bolao_detail_share_message, b.name, web, app, b.code)
    }

    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.matches.isEmpty()) {
                    BolaoFullScreenLoading()
                } else {
                    Column(Modifier.fillMaxSize()) {
                        Box(modifier = Modifier.fillMaxWidth().background(GradientHero).padding(top = 16.dp, bottom = 16.dp)) {
                            Column(Modifier.padding(horizontal = 20.dp)) {
                                Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    BolaoIconButton(onClick = onNavigateBack, modifier = Modifier.size(36.dp).offset(x = (-10).dp)) {
                                        BolaoIcon(
                                            Icons.AutoMirrored.Filled.ArrowBack,
                                            stringResource(Res.string.bolao_detail_nav_back_cd),
                                            tint = Color.White,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                    BolaoText(
                                        uiState.bolao?.name ?: stringResource(Res.string.bolao_detail_default_name),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.weight(1f).padding(top = 4.dp).offset(x = (-8).dp)
                                    )

                                    BolaoIconButton(onClick = onNavigateToHelp, modifier = Modifier.size(36.dp)) {
                                        BolaoIcon(
                                            Icons.AutoMirrored.Outlined.HelpOutline,
                                            stringResource(Res.string.bolao_detail_help_cd),
                                            tint = TextMuted,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    if (isOwner) {
                                        BolaoIconButton(
                                            onClick = {
                                                shareMessage?.let { launcherProvider.shareText(it) }
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) {
                                            BolaoIcon(
                                                Icons.Default.Share,
                                                stringResource(Res.string.bolao_detail_share_cd),
                                                tint = TextMuted,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        BolaoIconButton(
                                            onClick = {
                                                onNavigateToAddParticipants(bolaoId)
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) {
                                            BolaoIcon(
                                                Icons.Default.PersonAdd,
                                                stringResource(Res.string.bolao_detail_add_participant_cd),
                                                tint = Neon,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                        BolaoIconButton(
                                            onClick = {
                                                onNavigateToEdit(bolaoId)
                                            },
                                            modifier =
                                            Modifier.size(
                                                36.dp
                                            )
                                        ) {
                                            BolaoIcon(
                                                Icons.Default.Edit,
                                                stringResource(Res.string.bolao_detail_edit_cd),
                                                tint = Neon,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    } else {
                                        Box {
                                            BolaoIconButton(
                                                onClick = {
                                                    showMenu = true
                                                },
                                                modifier =
                                                Modifier.size(
                                                    36.dp
                                                )
                                            ) {
                                                BolaoIcon(
                                                    Icons.Default.MoreVert,
                                                    stringResource(Res.string.bolao_detail_menu_cd),
                                                    tint = Color.White,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            BolaoDropdownMenu(
                                                expanded = showMenu,
                                                onDismissRequest = {
                                                    showMenu = false
                                                },
                                                modifier =
                                                Modifier.background(
                                                    NavyCard
                                                ).border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                                            ) {
                                                BolaoDropdownMenuItem(text = {
                                                    BolaoText(stringResource(Res.string.bolao_detail_menu_share), color = Color.White)
                                                }, leadingIcon = {
                                                    BolaoIcon(
                                                        Icons.Default.Share,
                                                        null,
                                                        tint = TextMuted,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }, onClick = {
                                                    showMenu = false
                                                    shareMessage?.let { launcherProvider.shareText(it) }
                                                })
                                                BolaoDropdownMenuItem(text = {
                                                    BolaoText(stringResource(Res.string.bolao_detail_menu_leave), color = ErrorRed)
                                                }, leadingIcon = {
                                                    BolaoIcon(
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
                                        BolaoText(
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
                                        BolaoSurface(
                                            color =
                                            Gold.copy(
                                                alpha = 0.1f
                                            ),
                                            shape =
                                            RoundedCornerShape(
                                                12.dp
                                            ),
                                            border =
                                            BorderStroke(
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
                                                BolaoText(stringResource(Res.string.bolao_detail_warning_emoji), fontSize = 16.sp)
                                                BolaoText(
                                                    stringResource(Res.string.bolao_detail_pending_count_message, pCount),
                                                    color = Gold,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                BolaoText(
                                                    stringResource(Res.string.bolao_detail_pending_view_button),
                                                    color = Gold,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Black
                                                )
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
                                            BolaoText(stringResource(Res.string.bolao_detail_key_emoji), fontSize = 12.sp)
                                            BolaoText(
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
                                            BolaoText(stringResource(Res.string.bolao_detail_participants_emoji), fontSize = 12.sp)
                                            BolaoText(
                                                stringResource(Res.string.bolao_detail_participants_chip, bolao.participants.size),
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
                                            BolaoText(stringResource(Res.string.bolao_detail_trophy_emoji), fontSize = 12.sp)
                                            BolaoText(
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
                                            BolaoText(
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
                            when (tabs.getOrNull(selectedTab) ?: tabGrupos) {
                                tabGrupos, tabJogos, tabRodadas, tabPontosCorridos ->
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
                                        onMatchClick = onNavigateToPrediction,
                                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                                        onOpenAdminScoreDialog = { matchToUpdate = it }
                                    )
                                tabMataMata ->
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
                                        onMatchClick = onNavigateToPrediction,
                                        onShowAllPredictions = { onNavigateToAllPredictions(it.id) },
                                        onOpenAdminScoreDialog = { matchToUpdate = it },
                                        championship = championship
                                    )
                                tabRanking -> RankingScreen(bolaoId = bolaoId)
                                tabTabela -> {
                                    val champId = uiState.bolao?.championshipId ?: "UNKNOWN"
                                    StandingsTab(matches = uiState.allMatches.filter { it.championshipId == champId })
                                }
                            }
                        }
                    }
                }
            }
            val adBannerProvider = koinInject<AdBannerProvider>()
            adBannerProvider.Banner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy))
        }
    }
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
    BolaoTheme {
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
