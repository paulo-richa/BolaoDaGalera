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
import com.lpstudio.bolaodagalera.designsystem.components.BolaoButton
import com.lpstudio.bolaodagalera.designsystem.components.BolaoDialog
import com.lpstudio.bolaodagalera.designsystem.components.BolaoHorizontalDivider
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIcon
import com.lpstudio.bolaodagalera.designsystem.components.BolaoIconButton
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
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.rememberLauncherProvider
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CreateBolaoScreen(onCreated: (String) -> Unit, onNavigateToAddParticipants: (String) -> Unit, onNavigateBack: () -> Unit) {
    val viewModel = koinViewModel<CreateBolaoViewModel>()
    val uiState by viewModel.uiState.collectAsState()
    val allMatches = uiState.allMatches

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedChampionshipId by remember { mutableStateOf("UNKNOWN") }

    // Auto-select the first available championship on load
    LaunchedEffect(Championship.getAll()) {
        if (selectedChampionshipId == "UNKNOWN") {
            selectedChampionshipId = Championship.getAll().find { it.isAvailable }?.id ?: "UNKNOWN"
        }
    }

    // Recompute availability reactively as matches/championship change
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

    // Initial scope adjustment based on the selected championship
    LaunchedEffect(selectedChampionshipId) {
        val championship = Championship.fromId(selectedChampionshipId)
        when {
            championship.isPointsBased -> {
                selectedScope = BolaoScope.PONTOS_CORRIDOS
                selectedMatchId = null
            }
            !championship.isGroupsAndKnockout -> {
                // No groups+knockout mix (e.g. knockout-only, like Copa do Brasil)
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

    // Auto-adjust scope if the group stage or knockout stage becomes unavailable/available
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

    // Validation helpers
    val nameErrorText = stringResource(Res.string.create_bolao_name_error_too_short)
    val nameError = if (nameTouched && name.trim().length < 10) nameErrorText else null
    val isFormValid = name.trim().length in 10..35

    LaunchedEffect(uiState.createdBolao) {
        if (uiState.createdBolao != null) {
            showSuccessDialog = true
        }
    }

    if (showSuccessDialog && uiState.createdBolao != null) {
        val bolao = uiState.createdBolao!!
        val inviteUrl = "https://bolaodagalera-bb002.web.app/invite?code=${bolao.code}"
        val shareMessage = stringResource(Res.string.create_bolao_share_message, bolao.name, inviteUrl, bolao.code)
        BolaoDialog(
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
                    BolaoText(stringResource(Res.string.create_bolao_success_emoji), fontSize = BolaoTypography.displayLarge.fontSize)
                    Spacer(Modifier.height(8.dp))
                    BolaoText(
                        stringResource(Res.string.create_bolao_success_title),
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
                            bolao.code,
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
            },
            confirmButton = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    BolaoButton(
                        text = stringResource(Res.string.create_bolao_button_add_participants),
                        onClick = {
                            showSuccessDialog = false
                            onNavigateToAddParticipants(bolao.id)
                        }
                    )
                    BolaoOutlinedButton(
                        onClick = {
                            launcherProvider.shareText(shareMessage)
                        },
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
                        onClick = {
                            showSuccessDialog = false
                            onCreated(bolao.id)
                        }
                    ) {
                        BolaoText(
                            stringResource(Res.string.create_bolao_button_go_to_bolao),
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
        BolaoScaffold(
            containerColor = Color.Transparent,
            topBar = {
                BolaoTopBar(title = stringResource(Res.string.create_bolao_top_bar_title), onNavigateBack = onNavigateBack)
            }
        ) { padding ->
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
                // Hero section
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

                Spacer(Modifier.height(20.dp))

                // Form card
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
                    // Championship
                    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)) {
                        BolaoText(
                            stringResource(Res.string.create_bolao_section_championship),
                            fontSize = BolaoTypography.bodyMedium.fontSize,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        val championships = Championship.getAll()
                        val comingSoonText = stringResource(Res.string.create_bolao_coming_soon)
                        val phasesLabel = stringResource(Res.string.create_bolao_section_phases)
                        val scopeFullEmoji = stringResource(Res.string.create_bolao_scope_full_emoji)
                        val scopeGroupsEmoji = stringResource(Res.string.create_bolao_scope_groups_emoji)
                        val scopeKnockoutEmoji = stringResource(Res.string.create_bolao_scope_knockout_emoji)
                        val scopeLeagueEmoji = stringResource(Res.string.create_bolao_scope_league_emoji)
                        val scopeGroupsClosed = stringResource(Res.string.create_bolao_scope_groups_closed)
                        val scopeKnockoutClosed = stringResource(Res.string.create_bolao_scope_knockout_closed)

                        championships.forEach { championship ->
                            val id = championship.id
                            val label = championship.displayName
                            val isAvailable = championship.isAvailable
                            val isSelected = selectedChampionshipId == id

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
                                    .clickable(enabled = isAvailable) { selectedChampionshipId = id },
                                color = if (isSelected) NavyElevated else NavyCard.copy(alpha = 0.7f)
                            ) {
                                Column(modifier = Modifier.padding(BolaoSpacing.lg)) {
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
                                                    label,
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

                                    // Scope radio options are only shown for the selected championship
                                    val showScopeOptions = isSelected && championship.isGroupsAndKnockout

                                    if (showScopeOptions) {
                                        Spacer(Modifier.height(12.dp))
                                        BolaoHorizontalDivider(color = GlassBorder, thickness = 0.5.dp)
                                        Spacer(Modifier.height(12.dp))

                                        BolaoText(
                                            phasesLabel,
                                            fontSize = BolaoTypography.bodyMedium.fontSize,
                                            color = TextMuted,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )

                                        Spacer(Modifier.height(12.dp))

                                        Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
                                            BolaoScope.entries
                                                .filter { scope ->
                                                    // Scope visibility filters based on championship and match dates
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
                                                            BolaoScope.FULL -> scopeFullEmoji
                                                            BolaoScope.ONLY_GROUPS -> scopeGroupsEmoji
                                                            BolaoScope.ONLY_KNOCKOUT -> scopeKnockoutEmoji
                                                            BolaoScope.PONTOS_CORRIDOS -> scopeLeagueEmoji
                                                        }

                                                    Row(
                                                        modifier =
                                                        Modifier
                                                            .fillMaxWidth()
                                                            .clip(BolaoRadiusShape.md)
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
                                                                BolaoRadiusShape.md
                                                            )
                                                            .clickable(enabled = isScopeEnabled) {
                                                                selectedScope = scope
                                                                selectedMatchId = null
                                                            }
                                                            .padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.md),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        BolaoText(
                                                            scopeEmoji,
                                                            fontSize = BolaoTypography.bodyLarge.fontSize,
                                                            modifier = Modifier.alpha(if (isScopeEnabled) 1f else 0.3f)
                                                        )
                                                        Spacer(Modifier.width(12.dp))

                                                        Column(modifier = Modifier.weight(1f)) {
                                                            BolaoText(
                                                                scope.label,
                                                                fontSize = BolaoTypography.bodyLarge.fontSize,
                                                                color = if (isScopeSelected) Color.White else TextMuted,
                                                                fontWeight = if (isScopeSelected) FontWeight.Bold else FontWeight.Normal,
                                                                modifier = Modifier.alpha(if (isScopeEnabled) 1f else 0.3f)
                                                            )

                                                            val errorMsg =
                                                                when {
                                                                    (scope == BolaoScope.FULL || scope == BolaoScope.ONLY_GROUPS) &&
                                                                        !isGroupStageAvailable -> scopeGroupsClosed
                                                                    scope == BolaoScope.ONLY_KNOCKOUT && !isScopeEnabled ->
                                                                        scopeKnockoutClosed
                                                                    else -> null
                                                                }

                                                            if (errorMsg != null) {
                                                                BolaoText(
                                                                    errorMsg,
                                                                    fontSize = BolaoTypography.bodySmall.fontSize,
                                                                    color = ErrorRed.copy(alpha = 0.7f),
                                                                    lineHeight = 12.sp
                                                                )
                                                            }
                                                        }

                                                        BolaoRadioButton(
                                                            enabled = isScopeEnabled,
                                                            selected = isScopeSelected,
                                                            onClick = {
                                                                if (isScopeEnabled) {
                                                                    selectedScope = scope
                                                                    selectedMatchId = null
                                                                }
                                                            },
                                                            selectedColor = Neon,
                                                            unselectedColor = TextMuted,
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

                    Column {
                        BolaoTextField(
                            value = description,
                            onValueChange = { if (it.length <= 115) description = it },
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

                    // Scoring System Section
                    Column(verticalArrangement = Arrangement.spacedBy(BolaoSpacing.sm)) {
                        BolaoText(
                            stringResource(Res.string.create_bolao_section_scoring),
                            fontSize = BolaoTypography.bodyMedium.fontSize,
                            color = TextMuted,
                            fontWeight = FontWeight.Medium
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
                        ) {
                            ScoreInput(
                                label = stringResource(Res.string.create_bolao_score_exact_label),
                                value = pointsExact,
                                onValueChange = { pointsExact = it },
                                modifier = Modifier.weight(1f)
                            )
                            ScoreInput(
                                label = stringResource(Res.string.create_bolao_score_winner_label),
                                value = pointsWinner,
                                onValueChange = { pointsWinner = it },
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

                    uiState.error?.let {
                        BolaoText(it, color = ErrorRed, fontSize = BolaoTypography.bodyMedium.fontSize)
                    }

                    // Info chip
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

                Spacer(Modifier.height(16.dp))

                BolaoButton(
                    text = stringResource(Res.string.create_bolao_button_create),
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
                    stringResource(Res.string.create_bolao_score_input_decrease),
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
                        stringResource(Res.string.create_bolao_score_input_point_singular)
                    } else {
                        stringResource(Res.string.create_bolao_score_input_point_plural)
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
                    stringResource(Res.string.create_bolao_score_input_increase),
                    color = Neon,
                    fontSize = BolaoTypography.headlineMedium.fontSize,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
