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
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
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
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_trophy_emoji
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_warning_emoji
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
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.presentation.ranking.RankingScreen
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import com.lpstudio.bolaodagalera.util.getInitials
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
        bolaoId = bolaoId,
        uiState = uiState,
        isOwner = uiState.bolao?.ownerId == userId,
        isAppOwner = isAppOwner,
        launcherProvider = launcherProvider,
        callbacks =
        BolaoDetailCallbacks(
            onLeaveBolao = { viewModel.leaveBolao() },
            onApproveJoin = { u, a -> viewModel.approveParticipant(u, a) },
            onApproveLeave = { u, a -> viewModel.approveLeaveRequest(u, a) },
            onNavigateToPrediction = onNavigateToPrediction,
            onNavigateToAllPredictions = onNavigateToAllPredictions,
            onNavigateToEdit = onNavigateToEdit,
            onNavigateToAddParticipants = onNavigateToAddParticipants,
            onNavigateToHelp = onNavigateToHelp,
            onSaveAdminScore = { m, h, a -> viewModel.updateMatchScore(m, h, a) },
            onNavigateBack = onNavigateBack
        )
    )
}

/** Callbacks bundled together to keep [BolaoDetailContent]'s parameter list manageable. */
internal data class BolaoDetailCallbacks(
    val onLeaveBolao: () -> Unit,
    val onApproveJoin: (String, Boolean) -> Unit,
    val onApproveLeave: (String, Boolean) -> Unit,
    val onNavigateToPrediction: (String) -> Unit,
    val onNavigateToAllPredictions: (String) -> Unit,
    val onNavigateToEdit: (String) -> Unit,
    val onNavigateToAddParticipants: (String) -> Unit,
    val onNavigateToHelp: () -> Unit,
    val onSaveAdminScore: (String, Int?, Int?) -> Unit,
    val onNavigateBack: () -> Unit
)

@Composable
private fun BolaoDetailDialogs(
    uiState: BolaoUiState,
    isOwner: Boolean,
    runtime: BolaoDetailRuntimeState,
    onLeaveBolao: () -> Unit,
    onApproveJoin: (String, Boolean) -> Unit,
    onApproveLeave: (String, Boolean) -> Unit
) {
    LeaveBolaoDialog(
        show = runtime.dialogs.showLeaveDialog.value,
        isOwner = isOwner,
        onConfirm = {
            runtime.dialogs.showLeaveDialog.value = false
            onLeaveBolao()
        },
        onDismiss = { runtime.dialogs.showLeaveDialog.value = false }
    )

    ParticipantsSheetDialog(
        show = runtime.dialogs.showParticipantsSheet.value,
        uiState = uiState,
        isOwner = isOwner,
        onDismiss = { runtime.dialogs.showParticipantsSheet.value = false },
        onApproveJoin = onApproveJoin,
        onApproveLeave = onApproveLeave
    )
}

@Composable
internal fun BolaoDetailContent(
    bolaoId: String,
    uiState: BolaoUiState,
    isOwner: Boolean,
    isAppOwner: Boolean,
    launcherProvider: LauncherProvider,
    callbacks: BolaoDetailCallbacks
) {
    val derived = rememberBolaoDetailDerivedState(uiState)
    val runtime = rememberBolaoDetailRuntimeState(bolaoId)

    AdminScoreDialogHost(
        match = runtime.dialogs.matchToUpdate.value,
        onDismiss = { runtime.dialogs.matchToUpdate.value = null },
        onConfirm = { match, h, a ->
            callbacks.onSaveAdminScore(match.id, h, a)
            runtime.dialogs.matchToUpdate.value = null
        }
    )

    BolaoDetailTabEffects(uiState, derived, runtime)

    BolaoDetailDialogs(uiState, isOwner, runtime, callbacks.onLeaveBolao, callbacks.onApproveJoin, callbacks.onApproveLeave)

    val shareMessage = uiState.bolao?.let { b ->
        val web = "https://bolaodagalera-bb002.web.app/invite?code=${b.code}"
        val app = "bolaodagalera://invite?code=${b.code}"
        stringResource(Res.string.bolao_detail_share_message, b.name, web, app, b.code)
    }

    BolaoDetailScreenBody(bolaoId, uiState, isOwner, isAppOwner, derived, runtime, shareMessage, launcherProvider, callbacks)
}

@Composable
private fun BolaoDetailScreenBody(
    bolaoId: String,
    uiState: BolaoUiState,
    isOwner: Boolean,
    isAppOwner: Boolean,
    derived: BolaoDetailDerivedState,
    runtime: BolaoDetailRuntimeState,
    shareMessage: String?,
    launcherProvider: LauncherProvider,
    callbacks: BolaoDetailCallbacks
) {
    Box(modifier = Modifier.fillMaxSize().background(DeepNavy)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                if (uiState.isLoading && uiState.matches.isEmpty()) {
                    BolaoFullScreenLoading()
                } else {
                    BolaoDetailMainColumn(
                        bolaoId,
                        uiState,
                        isOwner,
                        isAppOwner,
                        derived,
                        runtime,
                        shareMessage,
                        launcherProvider,
                        callbacks
                    )
                }
            }
            val adBannerProvider = koinInject<AdBannerProvider>()
            adBannerProvider.Banner(modifier = Modifier.fillMaxWidth().height(50.dp).background(DeepNavy))
        }
    }
}

@Composable
private fun BolaoDetailMainHeader(
    bolaoId: String,
    uiState: BolaoUiState,
    isOwner: Boolean,
    derived: BolaoDetailDerivedState,
    runtime: BolaoDetailRuntimeState,
    shareMessage: String?,
    launcherProvider: LauncherProvider,
    callbacks: BolaoDetailCallbacks
) {
    BolaoDetailHeaderSection(
        bolao = uiState.bolao,
        isOwner = isOwner,
        championship = derived.championship,
        showMenu = runtime.dialogs.showMenu.value,
        onShowMenuChange = { runtime.dialogs.showMenu.value = it },
        tabBarState =
        HeaderTabBarState(
            tabs = derived.tabs,
            selectedTab = runtime.tab.selectedTab.value,
            onTabSelected = { runtime.tab.selectedTab.value = it }
        ),
        topBarActions =
        TopBarActions(
            onNavigateBack = callbacks.onNavigateBack,
            onNavigateToHelp = callbacks.onNavigateToHelp,
            onShare = { shareMessage?.let { launcherProvider.shareText(it) } },
            onAddParticipants = { callbacks.onNavigateToAddParticipants(bolaoId) },
            onEdit = { callbacks.onNavigateToEdit(bolaoId) },
            onLeaveClick = { runtime.dialogs.showLeaveDialog.value = true }
        ),
        onShowParticipants = { runtime.dialogs.showParticipantsSheet.value = true }
    )
}

@Composable
private fun BolaoDetailMainColumn(
    bolaoId: String,
    uiState: BolaoUiState,
    isOwner: Boolean,
    isAppOwner: Boolean,
    derived: BolaoDetailDerivedState,
    runtime: BolaoDetailRuntimeState,
    shareMessage: String?,
    launcherProvider: LauncherProvider,
    callbacks: BolaoDetailCallbacks
) {
    Column(Modifier.fillMaxSize()) {
        BolaoDetailMainHeader(bolaoId, uiState, isOwner, derived, runtime, shareMessage, launcherProvider, callbacks)
        val filtered = remember(uiState.matches) {
            uiState.matches.filter { it.phase != Phase.FRIENDLIES }
        }
        val groups = remember(filtered) {
            filtered.filter { it.phase == Phase.GROUP_STAGE }
        }
        BolaoDetailTabContent(
            modifier = Modifier.weight(1f),
            selection = TabSelection(tabs = derived.tabs, selectedTab = runtime.tab.selectedTab.value, labels = derived.labels),
            groups = groups,
            filtered = filtered,
            uiState = uiState,
            isAppOwner = isAppOwner,
            bolaoId = bolaoId,
            state =
            GroupKnockoutState(
                selectedRound = runtime.tab.selectedRound.value,
                onRoundChange = { runtime.tab.selectedRound.value = it },
                selectedPhase = runtime.tab.selectedPhase.value,
                onPhaseChange = { runtime.tab.selectedPhase.value = it },
                selectedLabel = runtime.tab.selectedLabel.value,
                onLabelChange = { runtime.tab.selectedLabel.value = it },
                groupsListState = runtime.tab.groupsListState,
                knockoutListState = runtime.tab.knockoutListState,
                expandedGroups = runtime.tab.expandedGroups
            ),
            callbacks =
            TabNavigationCallbacks(
                onNavigateToPrediction = callbacks.onNavigateToPrediction,
                onNavigateToAllPredictions = callbacks.onNavigateToAllPredictions,
                onOpenAdminScoreDialog = { runtime.dialogs.matchToUpdate.value = it }
            )
        )
    }
}

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
        BolaoDetailPendingBanner(
            pCount = bolao.pendingParticipants.size + bolao.pendingExits.size,
            onShowParticipants = onShowParticipants
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun BolaoDetailPendingBanner(pCount: Int, onShowParticipants: () -> Unit) {
    BolaoSurface(
        color = Gold.copy(alpha = 0.1f),
        shape = BolaoRadiusShape.md,
        border = BorderStroke(1.dp, Gold.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth().clickable { onShowParticipants() }
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
                    data =
                    MatchTabData(
                        matches = groups.ifEmpty { filtered },
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isAppOwner,
                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L
                    ),
                    selectedRound = state.selectedRound,
                    onRoundChange = state.onRoundChange,
                    listState = state.groupsListState,
                    expandedGroups = state.expandedGroups,
                    actions =
                    MatchTabActions(
                        onMatchClick = callbacks.onNavigateToPrediction,
                        onShowAllPredictions = { callbacks.onNavigateToAllPredictions(it.id) },
                        onOpenAdminScoreDialog = callbacks.onOpenAdminScoreDialog
                    )
                )
            selection.labels.mataMata ->
                KnockoutTab(
                    data =
                    MatchTabData(
                        matches = filtered,
                        predictions = uiState.userPredictions,
                        isLoading = uiState.isLoading,
                        isAdmin = isAppOwner,
                        bolaoCreatedAt = uiState.bolao?.createdAtMillis ?: 0L
                    ),
                    selectedPhase = state.selectedPhase,
                    onPhaseChange = state.onPhaseChange,
                    selectedLabel = state.selectedLabel,
                    onLabelChange = state.onLabelChange,
                    listState = state.knockoutListState,
                    actions =
                    MatchTabActions(
                        onMatchClick = callbacks.onNavigateToPrediction,
                        onShowAllPredictions = { callbacks.onNavigateToAllPredictions(it.id) },
                        onOpenAdminScoreDialog = callbacks.onOpenAdminScoreDialog
                    ),
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
