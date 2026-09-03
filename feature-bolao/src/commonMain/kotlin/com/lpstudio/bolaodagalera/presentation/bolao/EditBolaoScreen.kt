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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
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
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_button_add
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_button_save
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_char_count
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_delete_dialog_confirm
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_delete_dialog_message
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_delete_dialog_title
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_delete_icon_cd
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_description_char_count
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_error_message
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_field_description_label
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_field_name_label
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_member_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_name_error_min_length
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_overtime_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_overtime_info
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_owner_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_participant_owner_self
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_participant_self
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_remove_participant_cd
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_remove_participant_dialog_confirm
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_remove_participant_dialog_message
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_remove_participant_dialog_title
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_full_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_groups_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_knockout_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_knockout_started
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_league_emoji
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_league_label
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_league_locked
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_locked_generic
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_scope_toggle_knockout
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_exact_label
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_input_decrease
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_input_increase
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_input_point_plural
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_input_point_singular
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_score_winner_label
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_section_general
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_section_participants
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_section_scoring
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_section_type
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_success_message
import bolaodagalera.feature_bolao.generated.resources.edit_bolao_top_bar_title
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoConfirmDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoFullScreenLoading
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSnackbarHost
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSwitch
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.components.rememberBolaoSnackbarHostState
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.User
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun EditBolaoScreen(
    bolaoId: String,
    onNavigateToAddParticipants: (String) -> Unit,
    onBolaoDeleted: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val viewModel = koinViewModel<EditBolaoViewModel>(key = bolaoId) { parametersOf(bolaoId) }
    val uiState by viewModel.uiState.collectAsState()
    val isKnockoutStarted = uiState.isKnockoutStarted
    val snackbarHostState = rememberBolaoSnackbarHostState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(BolaoScope.FULL) }
    var pointsExact by remember { mutableIntStateOf(3) }
    var pointsWinner by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var participantToRemove by remember { mutableStateOf<User?>(null) }

    val nameErrorText = stringResource(Res.string.edit_bolao_name_error_min_length)
    val nameError = if (name.isNotBlank() && name.trim().length < 10) nameErrorText else null
    val isFormValid = name.trim().length in 10..35

    val successMessage = stringResource(Res.string.edit_bolao_success_message)
    LaunchedEffect(uiState.showSuccessMessage) {
        if (uiState.showSuccessMessage) {
            snackbarHostState.showSnackbar(successMessage)
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(getString(Res.string.edit_bolao_error_message, it))
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
        DeleteBolaoDialog(
            onConfirm = {
                showDeleteDialog = false
                viewModel.delete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    participantToRemove?.let { user ->
        RemoveParticipantDialog(
            user = user,
            onConfirm = {
                viewModel.removeParticipant(user.id)
                participantToRemove = null
            },
            onDismiss = { participantToRemove = null }
        )
    }

    val deleteIconCd = stringResource(Res.string.edit_bolao_delete_icon_cd)
    BolaoScaffold(
        containerColor = DeepNavy,
        snackbarHost = { BolaoSnackbarHost(snackbarHostState) },
        topBar = {
            BolaoTopBar(
                title = stringResource(Res.string.edit_bolao_top_bar_title),
                onNavigateBack = onNavigateBack,
                actions = {
                    val isOwner = viewModel.currentUserId == uiState.bolao?.ownerId
                    if (isOwner) {
                        BolaoIconButton(onClick = { showDeleteDialog = true }) {
                            BolaoIcon(Icons.Default.Delete, deleteIconCd, tint = ErrorRed)
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
                    .padding(horizontal = BolaoSpacing.xl)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xl)
            ) {
                Spacer(Modifier.height(8.dp))

                BasicInfoSection(
                    name = name,
                    onNameChange = { name = it },
                    nameError = nameError,
                    description = description,
                    onDescriptionChange = { description = it }
                )

                uiState.bolao?.let { originalBolao ->
                    ScopeSection(
                        originalBolao = originalBolao,
                        isKnockoutStarted = isKnockoutStarted,
                        selectedScope = selectedScope,
                        onSelectedScopeChange = { selectedScope = it }
                    )
                }

                EditScoringSection(
                    pointsExact = pointsExact,
                    onPointsExactChange = { pointsExact = it },
                    pointsWinner = pointsWinner,
                    onPointsWinnerChange = { pointsWinner = it },
                    isSaveEnabled = isFormValid && !uiState.isLoading,
                    isSaveLoading = uiState.isLoading,
                    onSave = { viewModel.update(name, description, selectedScope, pointsExact, pointsWinner) }
                )

                ParticipantsSection(
                    bolaoId = bolaoId,
                    onNavigateToAddParticipants = onNavigateToAddParticipants,
                    participants = uiState.participants,
                    ownerId = uiState.bolao?.ownerId,
                    currentUserId = viewModel.currentUserId,
                    onRemoveParticipant = { participantToRemove = it }
                )

                Spacer(Modifier.height(40.dp))
                if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
                    Spacer(Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun DeleteBolaoDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    BolaoConfirmDialog(
        title = stringResource(Res.string.edit_bolao_delete_dialog_title),
        message = stringResource(Res.string.edit_bolao_delete_dialog_message),
        confirmText = stringResource(Res.string.edit_bolao_delete_dialog_confirm),
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun RemoveParticipantDialog(user: User, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    BolaoConfirmDialog(
        title = stringResource(Res.string.edit_bolao_remove_participant_dialog_title),
        message = stringResource(Res.string.edit_bolao_remove_participant_dialog_message, user.name),
        confirmText = stringResource(Res.string.edit_bolao_remove_participant_dialog_confirm),
        isDestructive = true,
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun BasicInfoSection(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    description: String,
    onDescriptionChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        BolaoText(
            stringResource(Res.string.edit_bolao_section_general),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)) {
            BolaoTextField(
                value = name,
                onValueChange = { if (it.length <= 35) onNameChange(it) },
                label = stringResource(Res.string.edit_bolao_field_name_label),
                isError = nameError != null
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.xs),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (nameError != null) {
                    BolaoText(nameError, color = ErrorRed, fontSize = BolaoTypography.bodySmall.fontSize)
                } else {
                    Spacer(Modifier.width(1.dp))
                }
                BolaoText(
                    stringResource(Res.string.edit_bolao_char_count, name.length),
                    color = if (name.length < 10 || name.length > 35) ErrorRed else TextSubtle,
                    fontSize = BolaoTypography.bodySmall.fontSize
                )
            }
        }

        Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)) {
            BolaoTextField(
                value = description,
                onValueChange = { if (it.length <= 115) onDescriptionChange(it) },
                label = stringResource(Res.string.edit_bolao_field_description_label)
            )
            BolaoText(
                stringResource(Res.string.edit_bolao_description_char_count, description.length),
                color = if (description.length >= 115) ErrorRed else TextSubtle,
                fontSize = BolaoTypography.bodySmall.fontSize,
                modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.xs),
                textAlign = TextAlign.End
            )
        }
    }
}

/**
 * Editable championship scope card. Scope is only editable while the group stage
 * has not fully committed to knockout (see [canEditScope]'s upstream invariants).
 */
@Composable
private fun ScopeSection(
    originalBolao: Bolao,
    isKnockoutStarted: Boolean,
    selectedScope: BolaoScope,
    onSelectedScopeChange: (BolaoScope) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        BolaoText(
            stringResource(Res.string.edit_bolao_section_type),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        val championship = Championship.fromId(originalBolao.championshipId)
        val isOnlyGroups = originalBolao.scope == BolaoScope.ONLY_GROUPS
        val isFull = originalBolao.scope == BolaoScope.FULL
        val isLeague = originalBolao.scope == BolaoScope.PONTOS_CORRIDOS || championship.isPointsBased
        val canEditScope = (isOnlyGroups || (isFull && !isKnockoutStarted)) && !isLeague && championship.isGroupsAndKnockout

        BolaoSurface(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolaoRadiusShape.md)
                .border(
                    1.dp,
                    if (canEditScope) Neon.copy(alpha = 0.5f) else GlassBorder.copy(alpha = 0.3f),
                    BolaoRadiusShape.md
                ),
            color = NavyCard
        ) {
            Column(modifier = Modifier.padding(BolaoSpacing.lg)) {
                ScopeSummaryRow(championship = championship, selectedScope = selectedScope)

                if (canEditScope) {
                    Spacer(Modifier.height(12.dp))
                    ScopeToggleRow(selectedScope = selectedScope, onSelectedScopeChange = onSelectedScopeChange)
                } else {
                    val labelText =
                        when {
                            championship.isPointsBased -> stringResource(Res.string.edit_bolao_scope_league_locked)
                            isFull && isKnockoutStarted -> stringResource(Res.string.edit_bolao_scope_knockout_started)
                            else -> stringResource(Res.string.edit_bolao_scope_locked_generic)
                        }
                    BolaoText(
                        labelText,
                        fontSize = BolaoTypography.bodySmall.fontSize,
                        color = TextSubtle,
                        modifier = Modifier.padding(top = BolaoSpacing.sm)
                    )
                }
            }
        }
    }
}

@Composable
private fun ScopeSummaryRow(championship: Championship, selectedScope: BolaoScope) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
    ) {
        BolaoText(
            when {
                championship.isPointsBased -> stringResource(Res.string.edit_bolao_scope_league_emoji)
                selectedScope == BolaoScope.FULL -> stringResource(Res.string.edit_bolao_scope_full_emoji)
                selectedScope == BolaoScope.ONLY_GROUPS -> stringResource(Res.string.edit_bolao_scope_groups_emoji)
                selectedScope == BolaoScope.ONLY_KNOCKOUT -> stringResource(Res.string.edit_bolao_scope_knockout_emoji)
                else -> stringResource(Res.string.edit_bolao_scope_full_emoji)
            },
            fontSize = BolaoTypography.headlineSmall.fontSize
        )
        BolaoText(
            if (championship.isPointsBased) {
                stringResource(Res.string.edit_bolao_scope_league_label)
            } else {
                selectedScope.label
            },
            color = Color.White,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ScopeToggleRow(selectedScope: BolaoScope, onSelectedScopeChange: (BolaoScope) -> Unit) {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(BolaoRadiusShape.sm)
            .background(Neon.copy(alpha = 0.1f))
            .clickable {
                onSelectedScopeChange(
                    if (selectedScope == BolaoScope.ONLY_GROUPS) BolaoScope.FULL else BolaoScope.ONLY_GROUPS
                )
            }
            .padding(BolaoSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        BolaoText(
            stringResource(Res.string.edit_bolao_scope_toggle_knockout),
            color = Neon,
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold
        )
        BolaoSwitch(
            checked = selectedScope == BolaoScope.FULL,
            onCheckedChange = {
                onSelectedScopeChange(if (it) BolaoScope.FULL else BolaoScope.ONLY_GROUPS)
            },
            accentColor = Neon
        )
    }
}

@Composable
private fun EditScoringSection(
    pointsExact: Int,
    onPointsExactChange: (Int) -> Unit,
    pointsWinner: Int,
    onPointsWinnerChange: (Int) -> Unit,
    isSaveEnabled: Boolean,
    isSaveLoading: Boolean,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        BolaoText(
            stringResource(Res.string.edit_bolao_section_scoring),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            fontWeight = FontWeight.Bold,
            color = TextMuted,
            letterSpacing = 1.sp
        )

        val scoreDecreaseCd = stringResource(Res.string.edit_bolao_score_input_decrease)
        val scoreIncreaseCd = stringResource(Res.string.edit_bolao_score_input_increase)
        val pointSingularLabel = stringResource(Res.string.edit_bolao_score_input_point_singular)
        val pointPluralLabel = stringResource(Res.string.edit_bolao_score_input_point_plural)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
        ) {
            ScoreInput(
                label = stringResource(Res.string.edit_bolao_score_exact_label),
                value = pointsExact,
                onValueChange = onPointsExactChange,
                decreaseContentDescription = scoreDecreaseCd,
                increaseContentDescription = scoreIncreaseCd,
                pointSingularLabel = pointSingularLabel,
                pointPluralLabel = pointPluralLabel,
                modifier = Modifier.weight(1f)
            )
            ScoreInput(
                label = stringResource(Res.string.edit_bolao_score_winner_label),
                value = pointsWinner,
                onValueChange = onPointsWinnerChange,
                decreaseContentDescription = scoreDecreaseCd,
                increaseContentDescription = scoreIncreaseCd,
                pointSingularLabel = pointSingularLabel,
                pointPluralLabel = pointPluralLabel,
                modifier = Modifier.weight(1f)
            )
        }

        // Note on extra time and penalty shootout scoring
        BolaoSurface(
            color = Color.White.copy(alpha = 0.05f),
            shape = BolaoRadiusShape.md,
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(BolaoSpacing.md),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
            ) {
                BolaoText(stringResource(Res.string.edit_bolao_overtime_emoji), fontSize = BolaoTypography.bodyLarge.fontSize)
                BolaoText(
                    stringResource(Res.string.edit_bolao_overtime_info),
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    color = TextMuted,
                    lineHeight = 15.sp
                )
            }
        }

        BolaoButton(
            text = stringResource(Res.string.edit_bolao_button_save),
            isLoading = isSaveLoading,
            enabled = isSaveEnabled,
            modifier = Modifier.padding(top = BolaoSpacing.xs),
            onClick = onSave
        )
    }
}

@Composable
private fun ParticipantsSection(
    bolaoId: String,
    onNavigateToAddParticipants: (String) -> Unit,
    participants: List<User>,
    ownerId: String?,
    currentUserId: String?,
    onRemoveParticipant: (User) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BolaoText(
                stringResource(Res.string.edit_bolao_section_participants),
                fontSize = BolaoTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = TextMuted,
                letterSpacing = 1.sp
            )
            BolaoTextButton(
                onClick = { onNavigateToAddParticipants(bolaoId) },
                contentPadding = PaddingValues(0.dp)
            ) {
                BolaoIcon(Icons.Default.Add, null, modifier = Modifier.size(16.dp), tint = Neon)
                Spacer(Modifier.width(4.dp))
                BolaoText(
                    stringResource(Res.string.edit_bolao_button_add),
                    color = Neon,
                    fontSize = BolaoTypography.bodyLarge.fontSize
                )
            }
        }

        val sortedParticipants = participants.sortedBy { it.name.lowercase() }
        val removeParticipantCd = stringResource(Res.string.edit_bolao_remove_participant_cd)
        val ownerEmoji = stringResource(Res.string.edit_bolao_owner_emoji)
        val memberEmoji = stringResource(Res.string.edit_bolao_member_emoji)

        sortedParticipants.forEach { participant ->
            val isOwner = participant.id == ownerId
            val isSelf = participant.id == currentUserId

            ParticipantRow(
                participant = participant,
                isOwner = isOwner,
                isSelf = isSelf,
                ownerEmoji = ownerEmoji,
                memberEmoji = memberEmoji,
                removeParticipantCd = removeParticipantCd,
                canRemove = !isOwner && currentUserId == ownerId,
                onRemove = { onRemoveParticipant(participant) }
            )
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: User,
    isOwner: Boolean,
    isSelf: Boolean,
    ownerEmoji: String,
    memberEmoji: String,
    removeParticipantCd: String,
    canRemove: Boolean,
    onRemove: () -> Unit
) {
    BolaoSurface(
        color = NavyCard,
        shape = BolaoRadiusShape.md,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier.padding(BolaoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(if (isOwner) Neon else NavyElevated),
                contentAlignment = Alignment.Center
            ) {
                BolaoText(if (isOwner) ownerEmoji else memberEmoji, fontSize = BolaoTypography.bodyLarge.fontSize)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                BolaoText(
                    text =
                    when {
                        isOwner && isSelf -> stringResource(Res.string.edit_bolao_participant_owner_self, participant.name)
                        isSelf -> stringResource(Res.string.edit_bolao_participant_self, participant.name)
                        else -> participant.name
                    },
                    color = Color.White,
                    fontSize = BolaoTypography.bodyLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                if (participant.nickname.isNotBlank()) {
                    BolaoText(
                        text = "@${participant.nickname.lowercase()}",
                        color = TextMuted,
                        fontSize = BolaoTypography.bodyMedium.fontSize
                    )
                }
            }

            if (canRemove) {
                BolaoIconButton(onClick = onRemove) {
                    BolaoIcon(Icons.Default.Delete, removeParticipantCd, tint = ErrorRed.copy(alpha = 0.7f))
                }
            }
        }
    }
}
