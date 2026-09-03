package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_phase_first_leg
import bolaodagalera.feature_bolao.generated.resources.bolao_common_phase_second_leg
import bolaodagalera.feature_bolao.generated.resources.bolao_common_today_chip
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_grupos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_jogos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_mata_mata
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_pontos_corridos
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_ranking
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_rodadas
import bolaodagalera.feature_bolao.generated.resources.bolao_detail_tab_tabela
import com.lpstudio.bolaodagalera.CommonBackHandler
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

internal data class TabLabels(
    val grupos: String,
    val ranking: String,
    val mataMata: String,
    val pontosCorridos: String,
    val tabela: String,
    val jogos: String,
    val rodadas: String
)

/** Derived (non-persisted) state shared across the screen's tab logic. */
internal class BolaoDetailDerivedState(
    val labels: TabLabels,
    val championship: Championship,
    val tabs: List<String>,
    val defaultRound: Int,
    val defaultPhase: Phase?,
    val defaultLabel: String?
)

@Composable
internal fun rememberBolaoDetailDerivedState(uiState: BolaoUiState): BolaoDetailDerivedState {
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
    val knockoutDefaults =
        remember(uiState.matches, championship.isTwoLegged, firstLegFormat, secondLegFormat, todayLabel) {
            computeKnockoutDefaults(uiState.matches, championship.isTwoLegged, firstLegFormat, secondLegFormat, todayLabel)
        }
    val defaultRound = remember(uiState.matches) { computeDefaultRound(uiState.matches) }
    return BolaoDetailDerivedState(
        labels = labels,
        championship = championship,
        tabs = tabs,
        defaultRound = defaultRound,
        defaultPhase = knockoutDefaults.first,
        defaultLabel = knockoutDefaults.second
    )
}

/** Persisted (rememberSaveable-backed) state driving tab/round/phase selection. */
internal class BolaoDetailTabRuntimeState(
    val selectedTab: MutableState<Int>,
    val selectedRound: MutableState<Int>,
    val selectedPhase: MutableState<Phase?>,
    val selectedLabel: MutableState<String?>,
    val hasAutoSelectedTab: MutableState<Boolean>,
    val groupsListState: LazyListState,
    val knockoutListState: LazyListState,
    val expandedGroups: SnapshotStateList<String>
)

/** Persisted state driving the leave/participants/menu/admin-score dialogs. */
internal class BolaoDetailDialogRuntimeState(
    val matchToUpdate: MutableState<Match?>,
    val showLeaveDialog: MutableState<Boolean>,
    val showParticipantsSheet: MutableState<Boolean>,
    val showMenu: MutableState<Boolean>
)

internal class BolaoDetailRuntimeState(val tab: BolaoDetailTabRuntimeState, val dialogs: BolaoDetailDialogRuntimeState)

@Composable
internal fun rememberBolaoDetailRuntimeState(bolaoId: String): BolaoDetailRuntimeState {
    val selectedTab = rememberSaveable { mutableStateOf(0) }
    val selectedRound = rememberSaveable { mutableStateOf(0) }
    val selectedPhase = rememberSaveable { mutableStateOf<Phase?>(Phase.FRIENDLIES) }
    val selectedLabel = rememberSaveable(bolaoId) { mutableStateOf<String?>(null) }
    val hasAutoSelectedTab = rememberSaveable(bolaoId) { mutableStateOf(false) }
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
    val matchToUpdate = remember { mutableStateOf<Match?>(null) }
    val showLeaveDialog = remember { mutableStateOf(false) }
    val showParticipantsSheet = remember { mutableStateOf(false) }
    val showMenu = remember { mutableStateOf(false) }
    return BolaoDetailRuntimeState(
        tab =
        BolaoDetailTabRuntimeState(
            selectedTab = selectedTab,
            selectedRound = selectedRound,
            selectedPhase = selectedPhase,
            selectedLabel = selectedLabel,
            hasAutoSelectedTab = hasAutoSelectedTab,
            groupsListState = groupsListState,
            knockoutListState = knockoutListState,
            expandedGroups = expandedGroups
        ),
        dialogs =
        BolaoDetailDialogRuntimeState(
            matchToUpdate = matchToUpdate,
            showLeaveDialog = showLeaveDialog,
            showParticipantsSheet = showParticipantsSheet,
            showMenu = showMenu
        )
    )
}

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

/** Resets the current tab to its default round/phase before leaving it (back navigation). */
@Composable
private fun BolaoDetailBackHandler(derived: BolaoDetailDerivedState, tab: BolaoDetailTabRuntimeState) {
    val isFirstTab = tab.selectedTab.value == 0
    val isInDefaultState =
        remember(
            tab.selectedTab.value,
            tab.selectedRound.value,
            derived.defaultRound,
            tab.selectedPhase.value,
            derived.defaultPhase,
            tab.selectedLabel.value,
            derived.defaultLabel,
            derived.tabs
        ) {
            isTabInDefaultState(
                derived.tabs.getOrNull(tab.selectedTab.value),
                derived.labels,
                tab.selectedRound.value,
                derived.defaultRound,
                tab.selectedPhase.value,
                derived.defaultPhase,
                tab.selectedLabel.value,
                derived.defaultLabel
            )
        }

    CommonBackHandler(enabled = !isFirstTab || !isInDefaultState) {
        if (!isFirstTab) {
            tab.selectedTab.value = 0
        } else {
            resetTabToDefault(
                currentTabLabel = derived.tabs.getOrNull(tab.selectedTab.value),
                labels = derived.labels,
                onResetRound = { tab.selectedRound.value = derived.defaultRound },
                onResetPhase = {
                    tab.selectedPhase.value = derived.defaultPhase
                    tab.selectedLabel.value = derived.defaultLabel
                }
            )
        }
    }
}

/** Auto-selects the ranking tab (if all matches are finished) or the live/next knockout phase on first load. */
@Composable
private fun BolaoDetailAutoSelectionEffect(uiState: BolaoUiState, derived: BolaoDetailDerivedState, tab: BolaoDetailTabRuntimeState) {
    LaunchedEffect(uiState.matches, derived.defaultPhase, derived.defaultLabel) {
        applyAutoTabSelection(
            inputs =
            AutoSelectionInputs(
                matches = uiState.matches,
                hasAutoSelected = tab.hasAutoSelectedTab.value,
                tabs = derived.tabs,
                tabRanking = derived.labels.ranking,
                selectedPhase = tab.selectedPhase.value,
                defaultPhase = derived.defaultPhase,
                defaultLabel = derived.defaultLabel,
                selectedRound = tab.selectedRound.value,
                defaultRound = derived.defaultRound
            ),
            actions =
            AutoSelectionActions(
                onSelectTab = { tab.selectedTab.value = it },
                onMarkAutoSelected = { tab.hasAutoSelectedTab.value = true },
                onSelectPhase = { tab.selectedPhase.value = it },
                onSelectLabel = { tab.selectedLabel.value = it },
                onSelectRound = { tab.selectedRound.value = it }
            )
        )
    }
}

/** Back-navigation (reset tab to default before leaving) and initial auto-tab-selection effects. */
@Composable
internal fun BolaoDetailTabEffects(uiState: BolaoUiState, derived: BolaoDetailDerivedState, runtime: BolaoDetailRuntimeState) {
    BolaoDetailBackHandler(derived, runtime.tab)
    BolaoDetailAutoSelectionEffect(uiState, derived, runtime.tab)
}

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

/** A knockout match still counts as "today" this long after kickoff, in case it's running long. */
private const val TODAY_KNOCKOUT_GRACE_MILLIS = 3 * 3_600_000L

/** Matches after midnight before this hour still count as "today" (late-night kickoffs). */
private const val EARLY_MORNING_CUTOFF_HOUR = 4

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
        val isRecentlyFinished = now in it.matchDateMillis..(it.matchDateMillis + TODAY_KNOCKOUT_GRACE_MILLIS)
        val isTomorrowEarly = mDate.toEpochDays() == today.toEpochDays() + 1 && mTime.hour < EARLY_MORNING_CUTOFF_HOUR
        mDate == today || isTomorrowEarly || isRecentlyFinished
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
