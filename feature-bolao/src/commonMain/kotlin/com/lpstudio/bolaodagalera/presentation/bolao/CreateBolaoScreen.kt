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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.create_bolao_button_add_participants
import bolaodagalera.feature_bolao.generated.resources.create_bolao_button_create
import bolaodagalera.feature_bolao.generated.resources.create_bolao_button_go_to_bolao
import bolaodagalera.feature_bolao.generated.resources.create_bolao_button_share_code
import bolaodagalera.feature_bolao.generated.resources.create_bolao_char_count
import bolaodagalera.feature_bolao.generated.resources.create_bolao_coming_soon
import bolaodagalera.feature_bolao.generated.resources.create_bolao_description_char_count
import bolaodagalera.feature_bolao.generated.resources.create_bolao_field_description_label
import bolaodagalera.feature_bolao.generated.resources.create_bolao_field_name_label
import bolaodagalera.feature_bolao.generated.resources.create_bolao_hero_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_hero_subtitle
import bolaodagalera.feature_bolao.generated.resources.create_bolao_info_code_message
import bolaodagalera.feature_bolao.generated.resources.create_bolao_info_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_name_error_too_short
import bolaodagalera.feature_bolao.generated.resources.create_bolao_overtime_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_overtime_info
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_full_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_groups_closed
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_groups_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_knockout_closed
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_knockout_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_scope_league_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_exact_label
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_input_decrease
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_input_increase
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_input_point_plural
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_input_point_singular
import bolaodagalera.feature_bolao.generated.resources.create_bolao_score_winner_label
import bolaodagalera.feature_bolao.generated.resources.create_bolao_section_championship
import bolaodagalera.feature_bolao.generated.resources.create_bolao_section_phases
import bolaodagalera.feature_bolao.generated.resources.create_bolao_section_scoring
import bolaodagalera.feature_bolao.generated.resources.create_bolao_share_message
import bolaodagalera.feature_bolao.generated.resources.create_bolao_success_choose_start
import bolaodagalera.feature_bolao.generated.resources.create_bolao_success_code_label
import bolaodagalera.feature_bolao.generated.resources.create_bolao_success_emoji
import bolaodagalera.feature_bolao.generated.resources.create_bolao_success_title
import bolaodagalera.feature_bolao.generated.resources.create_bolao_top_bar_title
import com.lpstudio.bolaodagalera.LauncherProvider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoHorizontalDivider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoOutlinedButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoRadioButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoScaffold
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTextField
import com.lpstudio.bolaodagalera.designsystem.components.BolaoTopBar
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.GlassWhite
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.GradientBg
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.NavyElevated
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.designsystem.theme.TextSubtle
import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

private const val DEFAULT_POINTS_EXACT = 3
private const val DEFAULT_POINTS_WINNER = 1

private class CreateBolaoFormState {
    var name by mutableStateOf("")
    var description by mutableStateOf("")
    var selectedChampionshipId by mutableStateOf("UNKNOWN")
    var selectedScope by mutableStateOf(BolaoScope.FULL)
    var selectedMatchId by mutableStateOf<String?>(null)
    var pointsExact by mutableIntStateOf(DEFAULT_POINTS_EXACT)
    var pointsWinner by mutableIntStateOf(DEFAULT_POINTS_WINNER)
    var nameTouched by mutableStateOf(false)
    var showSuccessDialog by mutableStateOf(false)
}

@Composable
private fun rememberCreateBolaoFormState() = remember { CreateBolaoFormState() }

private class PhaseAvailability(val isGroupStageAvailable: Boolean, val isKnockoutAvailable: Boolean)

/** Recomputed reactively as the loaded matches/selected championship change. */
@Composable
private fun rememberPhaseAvailability(
    viewModel: CreateBolaoViewModel,
    allMatches: List<com.lpstudio.bolaodagalera.domain.model.Match>,
    championshipId: String
): PhaseAvailability {
    val isGroupStageAvailable =
        remember(allMatches, championshipId) { viewModel.isPhaseAvailable(championshipId, Phase.GROUP_STAGE) }
    val isKnockoutAvailable = remember(allMatches, championshipId) { viewModel.isKnockoutAvailable(championshipId) }
    return PhaseAvailability(isGroupStageAvailable, isKnockoutAvailable)
}

@Composable
fun CreateBolaoScreen(onCreated: (String) -> Unit, onNavigateToAddParticipants: (String) -> Unit, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<CreateBolaoViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val form = rememberCreateBolaoFormState()
    val availability = rememberPhaseAvailability(viewModel, uiState.allMatches, form.selectedChampionshipId)
    val focusManager = LocalFocusManager.current
    val launcherProvider = rememberLauncherProvider()

    CreateBolaoScopeEffects(
        selectedChampionshipId = form.selectedChampionshipId,
        isGroupStageAvailable = availability.isGroupStageAvailable,
        isKnockoutAvailable = availability.isKnockoutAvailable,
        selectedScope = form.selectedScope,
        createdBolao = uiState.createdBolao,
        onInitialChampionshipSelected = { form.selectedChampionshipId = it },
        onScopeChange = { form.selectedScope = it },
        onMatchIdChange = { form.selectedMatchId = it },
        onShowSuccessDialogChange = { form.showSuccessDialog = it }
    )

    if (form.showSuccessDialog && uiState.createdBolao != null) {
        CreateBolaoSuccessDialog(
            bolao = uiState.createdBolao!!,
            launcherProvider = launcherProvider,
            onDismissRequest = { bolao ->
                form.showSuccessDialog = false
                onCreated(bolao.id)
            },
            onAddParticipants = { bolao ->
                form.showSuccessDialog = false
                onNavigateToAddParticipants(bolao.id)
            },
            onGoToBolao = { bolao ->
                form.showSuccessDialog = false
                onCreated(bolao.id)
            }
        )
    }

    CreateBolaoScreenScaffold(
        uiState = uiState,
        form = form,
        availability = availability,
        focusManager = focusManager,
        onNavigateBack = onNavigateBack,
        onCreateClick = {
            viewModel.create(
                form.name,
                form.description,
                form.selectedChampionshipId,
                form.selectedScope,
                form.selectedMatchId,
                form.pointsExact,
                form.pointsWinner
            )
        }
    )
}

@Composable
private fun CreateBolaoScreenScaffold(
    uiState: CreateBolaoUiState,
    form: CreateBolaoFormState,
    availability: PhaseAvailability,
    focusManager: FocusManager,
    onNavigateBack: () -> Unit,
    onCreateClick: () -> Unit
) {
    val nameErrorText = stringResource(Res.string.create_bolao_name_error_too_short)
    val nameError = if (form.nameTouched && form.name.trim().length < 10) nameErrorText else null
    val isFormValid = form.name.trim().length in 10..35

    Box(
        modifier =
        Modifier
            .fillMaxSize()
            .background(GradientBg)
    ) {
        BolaoScaffold(
            containerColor = Color.Transparent,
            topBar = {
                BolaoTopBar(title = stringResource(Res.string.create_bolao_top_bar_title), onNavigateBack = onNavigateBack)
            }
        ) { padding ->
            CreateBolaoFormContent(
                padding = padding,
                error = uiState.error,
                isLoading = uiState.isLoading,
                isFormValid = isFormValid,
                nameError = nameError,
                form = form,
                availability = availability,
                focusManager = focusManager,
                onCreateClick = onCreateClick
            )
        }
    }
}

/**
 * Side-effects driving the championship/scope selection: picks the first available
 * championship on load, resets the scope whenever the championship changes, keeps
 * the scope valid as phase availability changes, and reveals the success dialog
 * once a bolao has been created.
 */
@Composable
private fun CreateBolaoScopeEffects(
    selectedChampionshipId: String,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean,
    selectedScope: BolaoScope,
    createdBolao: Bolao?,
    onInitialChampionshipSelected: (String) -> Unit,
    onScopeChange: (BolaoScope) -> Unit,
    onMatchIdChange: (String?) -> Unit,
    onShowSuccessDialogChange: (Boolean) -> Unit
) {
    // Auto-select the first available championship on load
    LaunchedEffect(Championship.getAll()) {
        if (selectedChampionshipId == "UNKNOWN") {
            onInitialChampionshipSelected(Championship.getAll().find { it.isAvailable }?.id ?: "UNKNOWN")
        }
    }

    // Initial scope adjustment based on the selected championship
    LaunchedEffect(selectedChampionshipId) {
        val championship = Championship.fromId(selectedChampionshipId)
        onScopeChange(initialScopeForChampionship(championship, isGroupStageAvailable))
        onMatchIdChange(null)
    }

    // Auto-adjust scope if the group stage or knockout stage becomes unavailable/available
    LaunchedEffect(isGroupStageAvailable, isKnockoutAvailable, selectedChampionshipId) {
        val champ = Championship.fromId(selectedChampionshipId)
        onScopeChange(adjustScopeForAvailability(selectedScope, champ, isGroupStageAvailable, isKnockoutAvailable))
    }

    LaunchedEffect(createdBolao) {
        if (createdBolao != null) {
            onShowSuccessDialogChange(true)
        }
    }
}

/**
 * Scrollable form body of the create-bolao screen: hero copy, championship/scope
 * picker, name/description fields, scoring rules and the submit button.
 */
@Composable
private fun CreateBolaoFormContent(
    padding: PaddingValues,
    error: String?,
    isLoading: Boolean,
    isFormValid: Boolean,
    nameError: String?,
    form: CreateBolaoFormState,
    availability: PhaseAvailability,
    focusManager: FocusManager,
    onCreateClick: () -> Unit
) {
    Column(
        modifier =
        Modifier
            .fillMaxSize()
            .padding(padding)
            .imePadding()
            .padding(horizontal = BolaoSpacing.xxl)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)
    ) {
        CreateBolaoHeroSection()

        Spacer(Modifier.height(20.dp))

        CreateBolaoFormCard(error = error, nameError = nameError, form = form, availability = availability, focusManager = focusManager)

        Spacer(Modifier.height(16.dp))

        BolaoButton(
            text = stringResource(Res.string.create_bolao_button_create),
            isLoading = isLoading,
            enabled = isFormValid && !isLoading,
            onClick = onCreateClick
        )

        Spacer(Modifier.height(32.dp))
        if (WindowInsets.ime.asPaddingValues().calculateBottomPadding() > 0.dp) {
            Spacer(Modifier.height(100.dp))
        }
    }
}

@Composable
private fun CreateBolaoFormCard(
    error: String?,
    nameError: String?,
    form: CreateBolaoFormState,
    availability: PhaseAvailability,
    focusManager: FocusManager
) {
    Column(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(BolaoRadiusShape.xl)
            .background(GlassWhite)
            .border(1.dp, GlassBorder, BolaoRadiusShape.xl)
            .padding(horizontal = BolaoSpacing.xl, vertical = BolaoSpacing.xl),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
    ) {
        ChampionshipSelectionSection(
            selectedChampionshipId = form.selectedChampionshipId,
            onChampionshipSelected = { form.selectedChampionshipId = it },
            isGroupStageAvailable = availability.isGroupStageAvailable,
            isKnockoutAvailable = availability.isKnockoutAvailable,
            selectedScope = form.selectedScope,
            onScopeSelected = { scope ->
                form.selectedScope = scope
                form.selectedMatchId = null
            }
        )

        BolaoNameField(
            name = form.name,
            onNameChange = {
                form.name = it
                form.nameTouched = true
            },
            nameError = nameError,
            focusManager = focusManager
        )

        BolaoDescriptionField(
            description = form.description,
            onDescriptionChange = { form.description = it },
            focusManager = focusManager
        )

        ScoringSection(
            pointsExact = form.pointsExact,
            onPointsExactChange = { form.pointsExact = it },
            pointsWinner = form.pointsWinner,
            onPointsWinnerChange = { form.pointsWinner = it }
        )

        error?.let {
            BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize)
        }

        CreateBolaoInfoChip()
    }
}

@Composable
private fun CreateBolaoHeroSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BolaoText(stringResource(Res.string.create_bolao_hero_emoji), fontSize = BolaoTypography.displayLarge.fontSize)
        Spacer(Modifier.height(4.dp))
        BolaoText(
            stringResource(Res.string.create_bolao_hero_subtitle),
            fontSize = BolaoTypography.bodyLarge.fontSize,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Success dialog shown after a bolao is created, offering to invite participants
 * or share the join code.
 */
@Composable
private fun CreateBolaoSuccessDialog(
    bolao: Bolao,
    launcherProvider: LauncherProvider,
    onDismissRequest: (Bolao) -> Unit,
    onAddParticipants: (Bolao) -> Unit,
    onGoToBolao: (Bolao) -> Unit
) {
    val inviteUrl = "https://bolaodagalera-bb002.web.app/invite?code=${bolao.code}"
    val shareMessage = stringResource(Res.string.create_bolao_share_message, bolao.name, inviteUrl, bolao.code)
    BolaoDialog(
        onDismissRequest = { onDismissRequest(bolao) },
        containerColor = NavyCard,
        title = { CreateBolaoSuccessDialogTitle() },
        text = { CreateBolaoSuccessDialogText(bolao.code) },
        confirmButton = {
            CreateBolaoSuccessDialogActions(
                onAddParticipants = { onAddParticipants(bolao) },
                onShare = { launcherProvider.shareText(shareMessage) },
                onGoToBolao = { onGoToBolao(bolao) }
            )
        },
        dismissButton = null
    )
}

@Composable
private fun CreateBolaoSuccessDialogTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BolaoText(stringResource(Res.string.create_bolao_success_emoji), fontSize = BolaoTypography.displayLarge.fontSize)
        Spacer(Modifier.height(8.dp))
        BolaoText(
            stringResource(Res.string.create_bolao_success_title),
            color = Color.White,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreateBolaoSuccessDialogText(bolaoCode: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BolaoText(
            stringResource(Res.string.create_bolao_success_code_label),
            color = TextMuted,
            fontSize = BolaoTypography.bodyLarge.fontSize
        )
        Spacer(Modifier.height(16.dp))
        Box(
            modifier =
            Modifier
                .clip(BolaoRadiusShape.md)
                .background(Gold.copy(alpha = 0.15f))
                .border(1.dp, Gold.copy(alpha = 0.4f), BolaoRadiusShape.md)
                .padding(horizontal = BolaoSpacing.xxl, vertical = BolaoSpacing.md)
        ) {
            BolaoText(
                bolaoCode,
                fontSize = BolaoTypography.displayMedium.fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Gold,
                letterSpacing = 2.sp
            )
        }
        Spacer(Modifier.height(20.dp))
        BolaoText(
            stringResource(Res.string.create_bolao_success_choose_start),
            color = TextMuted,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CreateBolaoSuccessDialogActions(onAddParticipants: () -> Unit, onShare: () -> Unit, onGoToBolao: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BolaoButton(
            text = stringResource(Res.string.create_bolao_button_add_participants),
            onClick = onAddParticipants
        )
        BolaoOutlinedButton(
            onClick = onShare,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = BolaoRadiusShape.lg,
            border = androidx.compose.foundation.BorderStroke(1.dp, Neon.copy(alpha = 0.5f)),
            contentColor = Neon
        ) {
            BolaoText(
                stringResource(Res.string.create_bolao_button_share_code),
                fontWeight = FontWeight.SemiBold,
                fontSize = BolaoTypography.titleLarge.fontSize
            )
        }
        BolaoTextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onGoToBolao
        ) {
            BolaoText(
                stringResource(Res.string.create_bolao_button_go_to_bolao),
                color = TextMuted,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Championship picker: each championship exposes its scope/phase options once selected.
 */
@Composable
private fun ChampionshipSelectionSection(
    selectedChampionshipId: String,
    onChampionshipSelected: (String) -> Unit,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean,
    selectedScope: BolaoScope,
    onScopeSelected: (BolaoScope) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
        BolaoText(
            stringResource(Res.string.create_bolao_section_championship),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        val championships = Championship.getAll()

        championships.forEach { championship ->
            ChampionshipCard(
                championship = championship,
                isSelected = selectedChampionshipId == championship.id,
                onSelect = { onChampionshipSelected(championship.id) },
                isGroupStageAvailable = isGroupStageAvailable,
                isKnockoutAvailable = isKnockoutAvailable,
                selectedScope = selectedScope,
                onScopeSelected = onScopeSelected
            )
        }
    }
}

@Composable
private fun ChampionshipCardHeader(championship: Championship, isSelected: Boolean, isAvailable: Boolean) {
    val comingSoonText = stringResource(Res.string.create_bolao_coming_soon)
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
        ) {
            Column {
                BolaoText(
                    championship.displayName,
                    fontSize = BolaoTypography.titleLarge.fontSize,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (isSelected) Color.White else TextMuted
                )
                if (!isAvailable) {
                    BolaoText(
                        comingSoonText,
                        fontSize = BolaoTypography.bodySmall.fontSize,
                        color = Neon.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (isSelected) {
            BolaoIcon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Neon,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun ChampionshipCard(
    championship: Championship,
    isSelected: Boolean,
    onSelect: () -> Unit,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean,
    selectedScope: BolaoScope,
    onScopeSelected: (BolaoScope) -> Unit
) {
    val isAvailable = championship.isAvailable

    BolaoSurface(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(BolaoRadiusShape.md)
            .border(
                width = 1.dp,
                color = if (isSelected) Neon else GlassBorder.copy(alpha = 0.5f),
                shape = BolaoRadiusShape.md
            )
            .alpha(if (isAvailable) 1f else 0.5f)
            .clickable(enabled = isAvailable) { onSelect() },
        color = if (isSelected) NavyElevated else NavyCard.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(BolaoSpacing.lg)) {
            ChampionshipCardHeader(championship = championship, isSelected = isSelected, isAvailable = isAvailable)

            // Scope radio options are only shown for the selected championship
            val showScopeOptions = isSelected && championship.isGroupsAndKnockout

            if (showScopeOptions) {
                Spacer(Modifier.height(12.dp))
                BolaoHorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                ScopePhasesList(
                    championship = championship,
                    isGroupStageAvailable = isGroupStageAvailable,
                    isKnockoutAvailable = isKnockoutAvailable,
                    selectedScope = selectedScope,
                    onScopeSelected = onScopeSelected
                )
            }
        }
    }
}

@Composable
private fun ScopePhasesList(
    championship: Championship,
    isGroupStageAvailable: Boolean,
    isKnockoutAvailable: Boolean,
    selectedScope: BolaoScope,
    onScopeSelected: (BolaoScope) -> Unit
) {
    BolaoText(
        stringResource(Res.string.create_bolao_section_phases),
        fontSize = BolaoTypography.bodyMedium.fontSize,
        color = TextMuted,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.5.sp
    )

    Spacer(Modifier.height(12.dp))

    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
        BolaoScope.entries
            .filter { scope -> isScopeVisible(scope, championship, isGroupStageAvailable, isKnockoutAvailable) }
            .forEach { scope ->
                val isScopeEnabled = isScopeEnabled(scope, isGroupStageAvailable, isKnockoutAvailable)
                ScopeOptionRow(
                    scope = scope,
                    isSelected = selectedScope == scope && isScopeEnabled,
                    isEnabled = isScopeEnabled,
                    isGroupStageAvailable = isGroupStageAvailable,
                    onSelect = { onScopeSelected(scope) }
                )
            }
    }
}

@Composable
private fun ScopeOptionRow(
    scope: BolaoScope,
    isSelected: Boolean,
    isEnabled: Boolean,
    isGroupStageAvailable: Boolean,
    onSelect: () -> Unit
) {
    val scopeEmoji =
        when (scope) {
            BolaoScope.FULL -> stringResource(Res.string.create_bolao_scope_full_emoji)
            BolaoScope.ONLY_GROUPS -> stringResource(Res.string.create_bolao_scope_groups_emoji)
            BolaoScope.ONLY_KNOCKOUT -> stringResource(Res.string.create_bolao_scope_knockout_emoji)
            BolaoScope.PONTOS_CORRIDOS -> stringResource(Res.string.create_bolao_scope_league_emoji)
        }
    val scopeGroupsClosed = stringResource(Res.string.create_bolao_scope_groups_closed)
    val scopeKnockoutClosed = stringResource(Res.string.create_bolao_scope_knockout_closed)

    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(BolaoRadiusShape.md)
            .background(
                if (isSelected) Neon.copy(alpha = 0.1f) else Color.Transparent
            )
            .border(
                1.dp,
                if (isSelected) {
                    Neon.copy(alpha = 0.5f)
                } else {
                    GlassBorder.copy(alpha = 0.5f)
                },
                BolaoRadiusShape.md
            )
            .clickable(enabled = isEnabled) { onSelect() }
            .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.md),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BolaoText(
            scopeEmoji,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            modifier = Modifier.alpha(if (isEnabled) 1f else 0.3f)
        )
        Spacer(Modifier.width(12.dp))

        ScopeOptionLabelColumn(
            scope = scope,
            isSelected = isSelected,
            isEnabled = isEnabled,
            errorMessage =
            scopeErrorMessage(scope, isEnabled, isGroupStageAvailable, scopeGroupsClosed, scopeKnockoutClosed),
            modifier = Modifier.weight(1f)
        )

        BolaoRadioButton(
            enabled = isEnabled,
            selected = isSelected,
            onClick = { if (isEnabled) onSelect() },
            selectedColor = Neon,
            unselectedColor = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun ScopeOptionLabelColumn(
    scope: BolaoScope,
    isSelected: Boolean,
    isEnabled: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        BolaoText(
            scope.label,
            fontSize = BolaoTypography.bodyLarge.fontSize,
            color = if (isSelected) Color.White else TextMuted,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.alpha(if (isEnabled) 1f else 0.3f)
        )

        if (errorMessage != null) {
            BolaoText(
                errorMessage,
                fontSize = BolaoTypography.bodySmall.fontSize,
                color = ErrorRed.copy(alpha = 0.7f),
                lineHeight = 12.sp
            )
        }
    }
}

@Composable
private fun BolaoNameField(name: String, onNameChange: (String) -> Unit, nameError: String?, focusManager: FocusManager) {
    Column {
        BolaoTextField(
            value = name,
            onValueChange = { if (it.length <= 35) onNameChange(it) },
            label = stringResource(Res.string.create_bolao_field_name_label),
            isError = nameError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.sm, vertical = BolaoSpacing.xs),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (nameError != null) {
                BolaoText(nameError, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize)
            } else {
                Spacer(Modifier.width(1.dp))
            }
            BolaoText(
                stringResource(Res.string.create_bolao_char_count, name.length),
                color = if (name.length < 10 || name.length > 35) ErrorRed else TextSubtle,
                fontSize = BolaoTypography.bodyMedium.fontSize
            )
        }
    }
}

@Composable
private fun BolaoDescriptionField(description: String, onDescriptionChange: (String) -> Unit, focusManager: FocusManager) {
    Column {
        BolaoTextField(
            value = description,
            onValueChange = { if (it.length <= 115) onDescriptionChange(it) },
            label = stringResource(Res.string.create_bolao_field_description_label),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = false,
            minLines = 2,
            maxLines = 3
        )
        BolaoText(
            stringResource(Res.string.create_bolao_description_char_count, description.length),
            color = if (description.length >= 115) ErrorRed else TextSubtle,
            fontSize = BolaoTypography.bodyMedium.fontSize,
            modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.sm, vertical = BolaoSpacing.xs),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ScoringSection(pointsExact: Int, onPointsExactChange: (Int) -> Unit, pointsWinner: Int, onPointsWinnerChange: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
        BolaoText(
            stringResource(Res.string.create_bolao_section_scoring),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            fontWeight = FontWeight.Medium
        )

        ScoreInputsRow(
            pointsExact = pointsExact,
            onPointsExactChange = onPointsExactChange,
            pointsWinner = pointsWinner,
            onPointsWinnerChange = onPointsWinnerChange
        )

        CreateBolaoOvertimeNote()
    }
}

@Composable
private fun ScoreInputsRow(pointsExact: Int, onPointsExactChange: (Int) -> Unit, pointsWinner: Int, onPointsWinnerChange: (Int) -> Unit) {
    val scoreDecreaseCd = stringResource(Res.string.create_bolao_score_input_decrease)
    val scoreIncreaseCd = stringResource(Res.string.create_bolao_score_input_increase)
    val pointSingularLabel = stringResource(Res.string.create_bolao_score_input_point_singular)
    val pointPluralLabel = stringResource(Res.string.create_bolao_score_input_point_plural)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
    ) {
        ScoreInput(
            label = stringResource(Res.string.create_bolao_score_exact_label),
            value = pointsExact,
            onValueChange = onPointsExactChange,
            decreaseContentDescription = scoreDecreaseCd,
            increaseContentDescription = scoreIncreaseCd,
            pointSingularLabel = pointSingularLabel,
            pointPluralLabel = pointPluralLabel,
            modifier = Modifier.weight(1f)
        )
        ScoreInput(
            label = stringResource(Res.string.create_bolao_score_winner_label),
            value = pointsWinner,
            onValueChange = onPointsWinnerChange,
            decreaseContentDescription = scoreDecreaseCd,
            increaseContentDescription = scoreIncreaseCd,
            pointSingularLabel = pointSingularLabel,
            pointPluralLabel = pointPluralLabel,
            modifier = Modifier.weight(1f)
        )
    }
}

// Note on extra time and penalty shootout scoring
@Composable
private fun CreateBolaoOvertimeNote() {
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
            BolaoText(
                stringResource(Res.string.create_bolao_overtime_emoji),
                fontSize = BolaoTypography.bodyLarge.fontSize
            )
            BolaoText(
                stringResource(Res.string.create_bolao_overtime_info),
                fontSize = BolaoTypography.bodyMedium.fontSize,
                color = TextMuted,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun CreateBolaoInfoChip() {
    Row(
        modifier =
        Modifier
            .fillMaxWidth()
            .clip(BolaoRadiusShape.md)
            .background(Gold.copy(alpha = 0.08f))
            .border(1.dp, Gold.copy(alpha = 0.2f), BolaoRadiusShape.md)
            .padding(BolaoSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BolaoText(stringResource(Res.string.create_bolao_info_emoji), fontSize = BolaoTypography.bodyLarge.fontSize)
        BolaoText(
            stringResource(Res.string.create_bolao_info_code_message),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = Gold.copy(alpha = 0.8f),
            lineHeight = 16.sp
        )
    }
}
