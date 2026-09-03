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
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.snapshots.SnapshotStateList
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
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTheme
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
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
    // Same uid used as the source of truth in firestore.rules (isAdmin()) —
    // username/email are user-editable fields under users/{uid}, so they
    // cannot be used as an admin check on the client.
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

    val labels =
        TabLabels(
            grupos = stringResource(Res.string.bolao_detail_tab_grupos),
            ranking = stringResource(Res.string.bolao_detail_tab_ranking),
            mataMata = stringResource(Res.string.bolao_detail_tab_mata_mata),
            pontosCorridos = stringResource(Res.string.bolao_detail_tab_pontos_corridos),
            tabela = stringResource(Res.string.bolao_detail_tab_tabela),
            jogos = stringResource(Res.string.bolao_detail_tab_jogos),
            rodadas = stringResource(Res.string.bolao_detail_tab_rodadas)
        )
    val todayLabel = stringResource(Res.string.bolao_common_today_chip)
    val firstLegFormat = stringResource(Res.string.bolao_common_phase_first_leg, "%1\$s")
    val secondLegFormat = stringResource(Res.string.bolao_common_phase_second_leg, "%1\$s")

    val tabs =
        remember(uiState.bolao?.scope, uiState.bolao?.championshipId, championship, labels) {
            computeTabs(uiState.bolao?.scope, championship, labels)
        }

    var selectedRound by rememberSaveable { mutableIntStateOf(0) }
    var selectedPhase by rememberSaveable { mutableStateOf<Phase?>(Phase.FRIENDLIES) }
    var selectedLabel by rememberSaveable(bolaoId) { mutableStateOf<String?>(null) }
    // rememberSaveable (not just remember) so the scroll position survives
    // navigating to the prediction screen and back without recomputing or
    // forcing a manual scroll — the screen returns exactly as the user left it.
    val groupsListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val knockoutListState = rememberSaveable(saver = LazyListState.Saver) { LazyListState() }
    val expandedGroups =
        rememberSaveable(
            bolaoId,
            saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
        ) { mutableStateListOf<String>() }
    var matchToUpdate by remember { mutableStateOf<Match?>(null) }

    AdminScoreDialogHost(
        match = matchToUpdate,
        onDismiss = { matchToUpdate = null },
        onConfirm = { match, h, a ->
            onSaveAdminScore(match.id, h, a)
            matchToUpdate = null
        }
    )

    val knockoutDefaults =
        remember(uiState.matches, championship.isTwoLegged, firstLegFormat, secondLegFormat, todayLabel) {
            computeKnockoutDefaults(uiState.matches, championship.isTwoLegged, firstLegFormat, secondLegFormat, todayLabel)
        }
    val defaultPhase = knockoutDefaults.first
    val defaultLabel = knockoutDefaults.second

    val isFirstTab = selectedTab == 0
    val defaultRound = remember(uiState.matches) { computeDefaultRound(uiState.matches) }

    val isInDefaultState =
        remember(selectedTab, selectedRound, defaultRound, selectedPhase, defaultPhase, selectedLabel, defaultLabel, tabs) {
            isTabInDefaultState(
                tabs.getOrNull(selectedTab),
                labels,
                selectedRound,
                defaultRound,
                selectedPhase,
                defaultPhase,
                selectedLabel,
                defaultLabel
            )
        }

    CommonBackHandler(enabled = !isFirstTab || !isInDefaultState) {
        if (!isFirstTab) {
            selectedTab = 0
        } else {
            resetTabToDefault(
                currentTabLabel = tabs.getOrNull(selectedTab),
                labels = labels,
                onResetRound = { selectedRound = defaultRound },
                onResetPhase = {
                    selectedPhase = defaultPhase
                    selectedLabel = defaultLabel
                }
            )
        }
    }

    var hasAutoSelectedTab by rememberSaveable(bolaoId) { mutableStateOf(false) }
    LaunchedEffect(uiState.matches, defaultPhase, defaultLabel) {
        applyAutoTabSelection(
            inputs =
            AutoSelectionInputs(
                matches = uiState.matches,
                hasAutoSelected = hasAutoSelectedTab,
                tabs = tabs,
                tabRanking = labels.ranking,
                selectedPhase = selectedPhase,
                defaultPhase = defaultPhase,
                defaultLabel = defaultLabel,
                selectedRound = selectedRound,
                defaultRound = defaultRound
            ),
            actions =
            AutoSelectionActions(
                onSelectTab = { selectedTab = it },
                onMarkAutoSelected = { hasAutoSelectedTab = true },
                onSelectPhase = { selectedPhase = it },
                onSelectLabel = { selectedLabel = it },
                onSelectRound = { selectedRound = it }
            )
        )
    }

    LeaveBolaoDialog(
        show = showLeaveDialog,
        isOwner = isOwner,
        onConfirm = {
            showLeaveDialog = false
            onLeaveBolao()
        },
        onDismiss = { showLeaveDialog = false }
    )

    ParticipantsSheetDialog(
        show = showParticipantsSheet,
        uiState = uiState,
        isOwner = isOwner,
        onDismiss = { showParticipantsSheet = false },
        onApproveJoin = onApproveJoin,
        onApproveLeave = onApproveLeave
    )

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
                        BolaoDetailHeaderSection(
                            bolao = uiState.bolao,
                            isOwner = isOwner,
                            championship = championship,
                            showMenu = showMenu,
                            onShowMenuChange = { showMenu = it },
                            tabBarState =
                            HeaderTabBarState(tabs = tabs, selectedTab = selectedTab, onTabSelected = { selectedTab = it }),
                            topBarActions =
                            TopBarActions(
                                onNavigateBack = onNavigateBack,
                                onNavigateToHelp = onNavigateToHelp,
                                onShare = { shareMessage?.let { launcherProvider.shareText(it) } },
                                onAddParticipants = { onNavigateToAddParticipants(bolaoId) },
                                onEdit = { onNavigateToEdit(bolaoId) },
                                onLeaveClick = { showLeaveDialog = true }
                            ),
                            onShowParticipants = { showParticipantsSheet = true }
                        )
                        val filtered = remember(uiState.matches) {
                            uiState.matches.filter { it.phase != Phase.FRIENDLIES }
                        }
                        val groups = remember(filtered) {
                            filtered.filter { it.phase == Phase.GROUP_STAGE }
                        }
                        BolaoDetailTabContent(
                            modifier = Modifier.weight(1f),
                            selection = TabSelection(tabs = tabs, selectedTab = selectedTab, labels = labels),
                            groups = groups,
                            filtered = filtered,
                            uiState = uiState,
                            isAppOwner = isAppOwner,
                            bolaoId = bolaoId,
                            state =
                            GroupKnockoutState(
                                selectedRound = selectedRound,
                                onRoundChange = { selectedRound = it },
                                selectedPhase = selectedPhase,
                                onPhaseChange = { selectedPhase = it },
                                selectedLabel = selectedLabel,
                                onLabelChange = { selectedLabel = it },
                                groupsListState = groupsListState,
                                knockoutListState = knockoutListState,
                                expandedGroups = expandedGroups
                            ),
                            callbacks =
                            TabNavigationCallbacks(
                                onNavigateToPrediction = onNavigateToPrediction,
                                onNavigateToAllPredictions = onNavigateToAllPredictions,
                                onOpenAdminScoreDialog = { matchToUpdate = it }
                            )
                        )
                    }
                }
            }
            val adBannerProvider = koinInject<AdBannerProvider>()
            adBannerProvider.Banner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy))
        }
    }
}

private data class TabLabels(
    val grupos: String,
    val ranking: String,
    val mataMata: String,
    val pontosCorridos: String,
    val tabela: String,
    val jogos: String,
    val rodadas: String
)

private data class HeaderTabBarState(val tabs: List<String>, val selectedTab: Int, val onTabSelected: (Int) -> Unit)

private data class TopBarActions(
    val onNavigateBack: () -> Unit,
    val onNavigateToHelp: () -> Unit,
    val onShare: () -> Unit,
    val onAddParticipants: () -> Unit,
    val onEdit: () -> Unit,
    val onLeaveClick: () -> Unit
)

private data class TabSelection(val tabs: List<String>, val selectedTab: Int, val labels: TabLabels)

private data class GroupKnockoutState(
    val selectedRound: Int,
    val onRoundChange: (Int) -> Unit,
    val selectedPhase: Phase?,
    val onPhaseChange: (Phase?) -> Unit,
    val selectedLabel: String?,
    val onLabelChange: (String?) -> Unit,
    val groupsListState: LazyListState,
    val knockoutListState: LazyListState,
    val expandedGroups: SnapshotStateList<String>
)

private data class TabNavigationCallbacks(
    val onNavigateToPrediction: (String) -> Unit,
    val onNavigateToAllPredictions: (String) -> Unit,
    val onOpenAdminScoreDialog: (Match) -> Unit
)

private data class AutoSelectionInputs(
    val matches: List<Match>,
    val hasAutoSelected: Boolean,
    val tabs: List<String>,
    val tabRanking: String,
    val selectedPhase: Phase?,
    val defaultPhase: Phase?,
    val defaultLabel: String?,
    val selectedRound: Int,
    val defaultRound: Int
)

private data class AutoSelectionActions(
    val onSelectTab: (Int) -> Unit,
    val onMarkAutoSelected: () -> Unit,
    val onSelectPhase: (Phase?) -> Unit,
    val onSelectLabel: (String?) -> Unit,
    val onSelectRound: (Int) -> Unit
)

private fun computeTabs(scope: BolaoScope?, championship: Championship, labels: TabLabels): List<String> = when (scope) {
    BolaoScope.ONLY_GROUPS -> listOf(labels.grupos, labels.ranking)
    BolaoScope.ONLY_KNOCKOUT -> listOf(labels.mataMata, labels.ranking)
    BolaoScope.PONTOS_CORRIDOS -> {
        val list = mutableListOf(labels.pontosCorridos, labels.ranking)
        if (championship.hasStandings) list.add(labels.tabela)
        list
    }
    else -> {
        if (championship.isPointsBased) {
            val list = mutableListOf(labels.pontosCorridos, labels.ranking)
            if (championship.hasStandings) list.add(labels.tabela)
            list
        } else if (championship.isGroupsAndKnockout) {
            listOf(labels.grupos, labels.mataMata, labels.ranking)
        } else {
            listOf(labels.mataMata, labels.ranking)
        }
    }
}

private fun computeKnockoutDefaults(
    matches: List<Match>,
    isTwoLegged: Boolean,
    firstLegFormat: String,
    secondLegFormat: String,
    todayLabel: String
): Pair<Phase?, String> {
    val phases = listOf(
        Phase.ROUND_OF_32,
        Phase.ROUND_OF_16,
        Phase.QUARTERFINALS,
        Phase.SEMIFINALS,
        Phase.THIRD_PLACE,
        Phase.FINAL
    )
    val phaseOrder = phases.filter { p -> matches.any { it.phase == p } }
    val phaseLabels =
        if (isTwoLegged) {
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

    val hasTodayKo = matches.filter { it.phase != Phase.GROUP_STAGE }.any {
        val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
        val mDate = mTime.date
        val isRecentlyFinished = now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L)
        mDate == today || (mDate.toEpochDays() == today.toEpochDays() + 1 && mTime.hour < 4) || isRecentlyFinished
    }

    return if (hasTodayKo) {
        Phase.FRIENDLIES to todayLabel
    } else {
        val next = phaseLabels.find { l ->
            val base = l.substringBefore(" - ")
            val isV = l.contains("Volta")
            matches.any { m ->
                m.phase.label == base && (if (isV) m.id.contains("-L2") else !m.id.contains("-L2")) && !m.isFinished
            }
        } ?: phaseLabels.lastOrNull()

        if (next != null) {
            val p = Phase.entries.find { it.label == next.substringBefore(" - ") }
            p to next
        } else {
            Phase.FRIENDLIES to todayLabel
        }
    }
}

private fun computeDefaultRound(matches: List<Match>): Int {
    val matchesGroupStage = matches.filter { it.phase == Phase.GROUP_STAGE }
    if (matchesGroupStage.isEmpty()) return 0
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val hasMatchToday = matchesGroupStage.any {
        Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate
    }
    return if (hasMatchToday) {
        0
    } else {
        val upcoming = matchesGroupStage
            .filter { !it.isFinished && it.matchDateMillis > now }
            .minByOrNull { it.matchDateMillis }
            ?.groupRound()
        val lastR = matchesGroupStage.maxByOrNull { it.matchDateMillis }?.groupRound() ?: 1
        upcoming ?: lastR
    }
}

private fun isTabInDefaultState(
    currentTabLabel: String?,
    labels: TabLabels,
    selectedRound: Int,
    defaultRound: Int,
    selectedPhase: Phase?,
    defaultPhase: Phase?,
    selectedLabel: String?,
    defaultLabel: String?
): Boolean = when (currentTabLabel) {
    labels.grupos, labels.jogos, labels.rodadas, labels.pontosCorridos -> selectedRound == defaultRound
    labels.mataMata -> selectedPhase == defaultPhase && (selectedLabel == defaultLabel || selectedLabel == null)
    else -> false
}

private fun resetTabToDefault(currentTabLabel: String?, labels: TabLabels, onResetRound: () -> Unit, onResetPhase: () -> Unit) {
    when (currentTabLabel) {
        labels.grupos, labels.jogos, labels.rodadas, labels.pontosCorridos -> onResetRound()
        labels.mataMata -> onResetPhase()
    }
}

private fun applyAutoTabSelection(inputs: AutoSelectionInputs, actions: AutoSelectionActions) {
    if (inputs.matches.isEmpty()) return
    if (!inputs.hasAutoSelected) {
        if (inputs.matches.all { it.isFinished }) {
            inputs.tabs.indexOf(inputs.tabRanking).takeIf { it != -1 }?.let {
                actions.onSelectTab(it)
                actions.onMarkAutoSelected()
                return
            }
        }

        // Knockout auto-selection on first load
        if (inputs.selectedPhase == Phase.FRIENDLIES && inputs.defaultPhase != Phase.FRIENDLIES && inputs.defaultPhase != null) {
            actions.onSelectPhase(inputs.defaultPhase)
            actions.onSelectLabel(inputs.defaultLabel)
        }

        actions.onMarkAutoSelected()
    }
    if (inputs.selectedRound == 0 && inputs.defaultRound != 0) actions.onSelectRound(inputs.defaultRound)
}

@Composable
private fun AdminScoreDialogHost(match: Match?, onDismiss: () -> Unit, onConfirm: (Match, Int?, Int?) -> Unit) {
    match?.let { m ->
        AdminScoreDialog(
            match = m,
            onDismiss = onDismiss,
            onConfirm = { h, a -> onConfirm(m, h, a) }
        )
    }
}

@Composable
private fun LeaveBolaoDialog(show: Boolean, isOwner: Boolean, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    if (!show) return
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
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun ParticipantsSheetDialog(
    show: Boolean,
    uiState: BolaoUiState,
    isOwner: Boolean,
    onDismiss: () -> Unit,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit
) {
    if (!show) return
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        BolaoSurface(
            modifier = Modifier.fillMaxWidth(0.95f).fillMaxHeight(0.7f),
            color = NavyCard,
            shape = BolaoRadiusShape.xxl,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(BolaoSpacing.xxl)) {
                BolaoText(
                    stringResource(Res.string.bolao_detail_participants_title),
                    fontSize = BolaoTypography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                BolaoText(
                    stringResource(Res.string.bolao_detail_participants_count, uiState.participants.size),
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = BolaoSpacing.xl)
                )
                ParticipantsSheetList(
                    modifier = Modifier.weight(1f),
                    uiState = uiState,
                    isOwner = isOwner,
                    onApproveJoin = onApproveJoin,
                    onApproveLeave = onApproveLeave
                )
                Spacer(Modifier.height(16.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    BolaoTextButton(
                        onClick = onDismiss,
                        contentPadding =
                        PaddingValues(
                            horizontal = 16.dp
                        )
                    ) {
                        BolaoText(
                            stringResource(Res.string.bolao_detail_close_button),
                            color = Neon,
                            fontWeight = FontWeight.Bold,
                            fontSize = BolaoTypography.titleLarge.fontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ParticipantsSheetList(
    modifier: Modifier = Modifier,
    uiState: BolaoUiState,
    isOwner: Boolean,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit
) {
    Box(modifier = modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (isOwner && (uiState.pendingJoinUsers.isNotEmpty() || uiState.pendingExitUsers.isNotEmpty())) {
                pendingRequestsSection(uiState, onApproveJoin, onApproveLeave)
            }
            participantsSection(uiState)
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
}

private fun LazyListScope.pendingRequestsSection(
    uiState: BolaoUiState,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit
) {
    item {
        BolaoText(
            stringResource(Res.string.bolao_detail_pending_requests_title),
            color = Gold,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = BolaoSpacing.sm)
        )
    }
    items(uiState.pendingJoinUsers) { user ->
        PendingRequestItem(
            user = user,
            label = stringResource(Res.string.bolao_detail_pending_join_label),
            onApprove = { onApproveJoin(user.id, true) },
            onDeny = { onApproveJoin(user.id, false) }
        )
    }
    items(uiState.pendingExitUsers) { user ->
        PendingRequestItem(
            user = user,
            label = stringResource(Res.string.bolao_detail_pending_leave_label),
            accentColor = ErrorRed,
            onApprove = { onApproveLeave(user.id, true) },
            onDeny = { onApproveLeave(user.id, false) }
        )
    }
    item {
        Spacer(Modifier.height(16.dp))
        BolaoText(
            stringResource(Res.string.bolao_detail_participants_title),
            color = TextMuted,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Black,
            modifier = Modifier.padding(vertical = BolaoSpacing.sm)
        )
    }
}

private fun LazyListScope.participantsSection(uiState: BolaoUiState) {
    items(uiState.participants.sortedBy { it.userName.lowercase() }) { p ->
        val isOwnerP = p.userId == uiState.bolao?.ownerId
        BolaoSurface(
            color = NavyElevated,
            shape = BolaoRadiusShape.lg,
            border = BorderStroke(1.dp, GlassBorder)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    initials = p.userName.getInitials(),
                    size = 40.dp,
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    isOwner = isOwnerP,
                    borderColor = if (isOwnerP) Gold else Neon.copy(alpha = 0.5f)
                )
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    BolaoText(
                        text = p.userName,
                        color = Color.White,
                        fontSize = BolaoTypography.bodyLarge.fontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    if (p.userNickname.isNotBlank()) {
                        BolaoText(
                            text = "@${p.userNickname.lowercase()}",
                            color = TextMuted,
                            fontSize = BolaoTypography.bodyMedium.fontSize
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BolaoDetailHeaderSection(
    bolao: Bolao?,
    isOwner: Boolean,
    championship: Championship,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    tabBarState: HeaderTabBarState,
    topBarActions: TopBarActions,
    onShowParticipants: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxWidth().background(
            GradientHero
        ).padding(top = BolaoSpacing.lg, bottom = BolaoSpacing.lg)
    ) {
        Column(Modifier.padding(horizontal = BolaoSpacing.xl)) {
            BolaoDetailTopBar(
                name = bolao?.name ?: stringResource(Res.string.bolao_detail_default_name),
                isOwner = isOwner,
                showMenu = showMenu,
                onShowMenuChange = onShowMenuChange,
                actions = topBarActions
            )
            bolao?.let {
                BolaoDetailDescriptionAndPending(bolao = it, isOwner = isOwner, onShowParticipants = onShowParticipants)
                BolaoDetailInfoChips(bolao = it, championship = championship, onShowParticipants = onShowParticipants)
                Spacer(Modifier.height(12.dp))
                BolaoDetailTabRow(tabBarState = tabBarState)
            }
        }
    }
}

@Composable
private fun BolaoDetailTopBar(
    name: String,
    isOwner: Boolean,
    showMenu: Boolean,
    onShowMenuChange: (Boolean) -> Unit,
    actions: TopBarActions
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)) {
        BolaoIconButton(onClick = actions.onNavigateBack, modifier = Modifier.size(36.dp).offset(x = (-10).dp)) {
            BolaoIcon(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(Res.string.bolao_detail_nav_back_cd),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        BolaoText(
            name,
            fontSize = BolaoTypography.headlineMedium.fontSize,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            modifier = Modifier.weight(1f).padding(top = BolaoSpacing.xs).offset(x = (-8).dp)
        )

        BolaoIconButton(onClick = actions.onNavigateToHelp, modifier = Modifier.size(36.dp)) {
            BolaoIcon(
                Icons.AutoMirrored.Outlined.HelpOutline,
                stringResource(Res.string.bolao_detail_help_cd),
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        if (isOwner) {
            BolaoDetailOwnerActions(onShare = actions.onShare, onAddParticipants = actions.onAddParticipants, onEdit = actions.onEdit)
        } else {
            BolaoDetailMemberMenu(
                showMenu = showMenu,
                onShowMenuChange = onShowMenuChange,
                onShare = actions.onShare,
                onLeaveClick = actions.onLeaveClick
            )
        }
    }
}

@Composable
private fun BolaoDetailOwnerActions(onShare: () -> Unit, onAddParticipants: () -> Unit, onEdit: () -> Unit) {
    BolaoIconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
        BolaoIcon(
            Icons.Default.Share,
            stringResource(Res.string.bolao_detail_share_cd),
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
    BolaoIconButton(onClick = onAddParticipants, modifier = Modifier.size(36.dp)) {
        BolaoIcon(
            Icons.Default.PersonAdd,
            stringResource(Res.string.bolao_detail_add_participant_cd),
            tint = Neon,
            modifier = Modifier.size(20.dp)
        )
    }
    BolaoIconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
        BolaoIcon(
            Icons.Default.Edit,
            stringResource(Res.string.bolao_detail_edit_cd),
            tint = Neon,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun BolaoDetailMemberMenu(showMenu: Boolean, onShowMenuChange: (Boolean) -> Unit, onShare: () -> Unit, onLeaveClick: () -> Unit) {
    Box {
        BolaoIconButton(onClick = { onShowMenuChange(true) }, modifier = Modifier.size(36.dp)) {
            BolaoIcon(
                Icons.Default.MoreVert,
                stringResource(Res.string.bolao_detail_menu_cd),
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        BolaoDropdownMenu(
            expanded = showMenu,
            onDismissRequest = { onShowMenuChange(false) },
            modifier =
            Modifier.background(
                NavyCard
            ).border(1.dp, GlassBorder, BolaoRadiusShape.sm)
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
                onShowMenuChange(false)
                onShare()
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
                onShowMenuChange(false)
                onLeaveClick()
            })
        }
    }
}

@Composable
private fun BolaoDetailDescriptionAndPending(bolao: Bolao, isOwner: Boolean, onShowParticipants: () -> Unit) {
    if (bolao.description.isNotBlank()) {
        Spacer(Modifier.height(16.dp))
        BolaoText(
            bolao.description,
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            maxLines = 2,
            lineHeight = 16.sp,
            modifier = Modifier.padding(horizontal = BolaoSpacing.xs)
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
            BolaoRadiusShape.md,
            border =
            BorderStroke(
                1.dp,
                Gold.copy(alpha = 0.3f)
            ),
            modifier =
            Modifier.fillMaxWidth().clickable {
                onShowParticipants()
            }
        ) {
            Row(
                modifier = Modifier.padding(BolaoSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
            ) {
                BolaoText(
                    stringResource(Res.string.bolao_detail_warning_emoji),
                    fontSize = BolaoTypography.titleLarge.fontSize
                )
                BolaoText(
                    stringResource(Res.string.bolao_detail_pending_count_message, pCount),
                    color = Gold,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                BolaoText(
                    stringResource(Res.string.bolao_detail_pending_view_button),
                    color = Gold,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun BolaoDetailInfoChips(bolao: Bolao, championship: Championship, onShowParticipants: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md, Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        InfoChip(backgroundColor = Gold.copy(alpha = 0.15f), borderColor = Gold.copy(alpha = 0.4f), spacing = BolaoSpacing.sm) {
            BolaoText(stringResource(Res.string.bolao_detail_key_emoji), fontSize = BolaoTypography.bodyMedium.fontSize)
            BolaoText(
                bolao.code,
                fontSize = BolaoTypography.bodyLarge.fontSize,
                fontWeight = FontWeight.Bold,
                color = Gold,
                letterSpacing = 1.sp
            )
        }
        InfoChip(
            backgroundColor = Neon.copy(alpha = 0.10f),
            borderColor = Neon.copy(alpha = 0.3f),
            onClick = onShowParticipants
        ) {
            BolaoText(stringResource(Res.string.bolao_detail_participants_emoji), fontSize = BolaoTypography.bodyMedium.fontSize)
            BolaoText(
                stringResource(Res.string.bolao_detail_participants_chip, bolao.participants.size),
                fontSize = BolaoTypography.bodyMedium.fontSize,
                color = Neon,
                fontWeight = FontWeight.SemiBold
            )
        }
        InfoChip(backgroundColor = Color.White.copy(alpha = 0.05f), borderColor = GlassBorder) {
            BolaoText(stringResource(Res.string.bolao_detail_trophy_emoji), fontSize = BolaoTypography.bodyMedium.fontSize)
            BolaoText(
                championship.displayName,
                fontSize = BolaoTypography.bodyMedium.fontSize,
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun InfoChip(
    backgroundColor: Color,
    borderColor: Color,
    spacing: androidx.compose.ui.unit.Dp = BolaoSpacing.xs,
    onClick: (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        modifier =
        Modifier.clip(BolaoRadiusShape.sm)
            .background(backgroundColor)
            .border(1.dp, borderColor, BolaoRadiusShape.sm)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(spacing),
        content = content
    )
}

@Composable
private fun BolaoDetailTabRow(tabBarState: HeaderTabBarState) {
    Row(
        modifier =
        Modifier.fillMaxWidth().height(
            IntrinsicSize.Min
        ).clip(
            BolaoRadiusShape.md
        ).background(NavyCard).border(1.dp, GlassBorder, BolaoRadiusShape.md).padding(BolaoSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)
    ) {
        tabBarState.tabs.forEachIndexed { index, label ->
            val selected = tabBarState.selectedTab == index
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
                ).fillMaxHeight().clip(BolaoRadiusShape.sm).background(bg).clickable {
                    tabBarState.onTabSelected(index)
                }.padding(vertical = BolaoSpacing.md, horizontal = BolaoSpacing.xs),
                contentAlignment = Alignment.Center
            ) {
                BolaoText(
                    label,
                    color = txtColor,
                    fontSize = if (tabBarState.tabs.size > 2 && label.length > 10) 13.sp else 14.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun BolaoDetailTabContent(
    modifier: Modifier = Modifier,
    selection: TabSelection,
    groups: List<Match>,
    filtered: List<Match>,
    uiState: BolaoUiState,
    isAppOwner: Boolean,
    bolaoId: String,
    state: GroupKnockoutState,
    callbacks: TabNavigationCallbacks
) {
    val championship = Championship.fromId(uiState.bolao?.championshipId)
    Box(modifier) {
        when (selection.tabs.getOrNull(selection.selectedTab) ?: selection.labels.grupos) {
            selection.labels.grupos, selection.labels.jogos, selection.labels.rodadas, selection.labels.pontosCorridos ->
                GroupStageTab(
                    matches = groups.ifEmpty { filtered },
                    predictions = uiState.userPredictions,
                    isLoading = uiState.isLoading,
                    isAdmin = isAppOwner,
                    bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                    selectedRound = state.selectedRound,
                    onRoundChange = state.onRoundChange,
                    listState = state.groupsListState,
                    expandedGroups = state.expandedGroups,
                    onMatchClick = callbacks.onNavigateToPrediction,
                    onShowAllPredictions = { callbacks.onNavigateToAllPredictions(it.id) },
                    onOpenAdminScoreDialog = callbacks.onOpenAdminScoreDialog
                )
            selection.labels.mataMata ->
                KnockoutTab(
                    matches = filtered,
                    predictions = uiState.userPredictions,
                    isLoading = uiState.isLoading,
                    isAdmin = isAppOwner,
                    bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L,
                    selectedPhase = state.selectedPhase,
                    onPhaseChange = state.onPhaseChange,
                    selectedLabel = state.selectedLabel,
                    onLabelChange = state.onLabelChange,
                    listState = state.knockoutListState,
                    onMatchClick = callbacks.onNavigateToPrediction,
                    onShowAllPredictions = { callbacks.onNavigateToAllPredictions(it.id) },
                    onOpenAdminScoreDialog = callbacks.onOpenAdminScoreDialog,
                    championship = championship
                )
            selection.labels.ranking -> RankingScreen(bolaoId = bolaoId)
            selection.labels.tabela -> {
                val champId = uiState.bolao?.championshipId ?: "UNKNOWN"
                StandingsTab(matches = uiState.allMatches.filter { it.championshipId == champId })
            }
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
            RankingEntry(myUserId, "Paulo Teste Silva", "Paulão", 10, 2, 4),
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
