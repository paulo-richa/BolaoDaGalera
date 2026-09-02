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
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
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
    val snackbarHostState = rememberBolaoSnackbarHostState()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedScope by remember { mutableStateOf(BolaoScope.FULL) }
    var pointsExact by remember { mutableIntStateOf(3) }
    var pointsWinner by remember { mutableIntStateOf(1) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var participantToRemove by remember { mutableStateOf<com.lpstudio.bolaodagalera.domain.model.User?>(null) }

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
        BolaoConfirmDialog(
            title = stringResource(Res.string.edit_bolao_delete_dialog_title),
            message = stringResource(Res.string.edit_bolao_delete_dialog_message),
            confirmText = stringResource(Res.string.edit_bolao_delete_dialog_confirm),
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
            title = stringResource(Res.string.edit_bolao_remove_participant_dialog_title),
            message = stringResource(Res.string.edit_bolao_remove_participant_dialog_message, user.name),
            confirmText = stringResource(Res.string.edit_bolao_remove_participant_dialog_confirm),
            isDestructive = true,
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

                // Basic Info Section
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
                            onValueChange = { if (it.length <= 35) name = it },
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
                            onValueChange = { if (it.length <= 115) description = it },
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

                // Scope Section
                uiState.bolao?.let { originalBolao ->
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
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
                                ) {
                                    BolaoText(
                                        when {
                                            championship.isPointsBased -> stringResource(Res.string.edit_bolao_scope_league_emoji)
                                            selectedScope == BolaoScope.FULL -> stringResource(Res.string.edit_bolao_scope_full_emoji)
                                            selectedScope == BolaoScope.ONLY_GROUPS -> stringResource(
                                                Res.string.edit_bolao_scope_groups_emoji
                                            )
                                            selectedScope == BolaoScope.ONLY_KNOCKOUT -> stringResource(
                                                Res.string.edit_bolao_scope_knockout_emoji
                                            )
                                            else -> stringResource(Res.string.edit_bolao_scope_full_emoji)
                                        },
                                        fontSize = BolaoTypography.headlineSmall.fontSize
                                    )
                                    BolaoText(
                                        if (championship.isPointsBased) {
                                            stringResource(
                                                Res.string.edit_bolao_scope_league_label
                                            )
                                        } else {
                                            selectedScope.label
                                        },
                                        color = Color.White,
                                        fontSize = BolaoTypography.bodyLarge.fontSize,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                if (canEditScope) {
                                    Spacer(Modifier.height(12.dp))
                                    Row(
                                        modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .clip(BolaoRadiusShape.sm)
                                            .background(Neon.copy(alpha = 0.1f))
                                            .clickable {
                                                selectedScope = if (selectedScope == BolaoScope.ONLY_GROUPS) {
                                                    BolaoScope.FULL
                                                } else {
                                                    BolaoScope.ONLY_GROUPS
                                                }
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
                                                selectedScope = if (it) BolaoScope.FULL else BolaoScope.ONLY_GROUPS
                                            },
                                            accentColor = Neon
                                        )
                                    }
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

                // Scoring System Section
                Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
                    BolaoText(
                        stringResource(Res.string.edit_bolao_section_scoring),
                        fontSize = BolaoTypography.bodyMedium.fontSize,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
                    ) {
                        ScoreInput(
                            label = stringResource(Res.string.edit_bolao_score_exact_label),
                            value = pointsExact,
                            onValueChange = { pointsExact = it },
                            modifier = Modifier.weight(1f)
                        )
                        ScoreInput(
                            label = stringResource(Res.string.edit_bolao_score_winner_label),
                            value = pointsWinner,
                            onValueChange = { pointsWinner = it },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Info sobre prorrogação e pênaltis
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
                        isLoading = uiState.isLoading,
                        enabled = isFormValid && !uiState.isLoading,
                        modifier = Modifier.padding(top = BolaoSpacing.xs),
                        onClick = { viewModel.update(name, description, selectedScope, pointsExact, pointsWinner) }
                    )
                }

                // Participants Section
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

                    val sortedParticipants = uiState.participants.sortedBy { it.name.lowercase() }
                    val removeParticipantCd = stringResource(Res.string.edit_bolao_remove_participant_cd)
                    val ownerEmoji = stringResource(Res.string.edit_bolao_owner_emoji)
                    val memberEmoji = stringResource(Res.string.edit_bolao_member_emoji)

                    sortedParticipants.forEach { participant ->
                        val isOwner = participant.id == uiState.bolao?.ownerId
                        val isSelf = participant.id == viewModel.currentUserId

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
                                            isOwner && isSelf -> stringResource(
                                                Res.string.edit_bolao_participant_owner_self,
                                                participant.name
                                            )
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

                                if (!isOwner && viewModel.currentUserId == uiState.bolao?.ownerId) {
                                    BolaoIconButton(onClick = { participantToRemove = participant }) {
                                        BolaoIcon(Icons.Default.Delete, removeParticipantCd, tint = ErrorRed.copy(alpha = 0.7f))
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
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
        BolaoText(label, fontSize = BolaoTypography.bodyMedium.fontSize, color = TextMuted)
        Row(
            modifier =
            Modifier
                .fillMaxWidth()
                .clip(BolaoRadiusShape.md)
                .background(NavyCard)
                .border(1.dp, GlassBorder, BolaoRadiusShape.md)
                .padding(BolaoSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BolaoIconButton(
                onClick = { if (value > 1) onValueChange(value - 1) },
                modifier = Modifier.size(36.dp)
            ) {
                BolaoText(
                    stringResource(Res.string.edit_bolao_score_input_decrease),
                    color = Neon,
                    fontSize = BolaoTypography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                BolaoText(
                    text = value.toString(),
                    color = Color.White,
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(Modifier.width(4.dp))
                BolaoText(
                    text =
                    if (value == 1) {
                        stringResource(Res.string.edit_bolao_score_input_point_singular)
                    } else {
                        stringResource(Res.string.edit_bolao_score_input_point_plural)
                    },
                    color = TextMuted,
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    fontWeight = FontWeight.Medium
                )
            }

            BolaoIconButton(
                onClick = { onValueChange(value + 1) },
                modifier = Modifier.size(36.dp)
            ) {
                BolaoText(
                    stringResource(Res.string.edit_bolao_score_input_increase),
                    color = Neon,
                    fontSize = BolaoTypography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
