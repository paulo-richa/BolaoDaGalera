package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_today_chip
import bolaodagalera.feature_bolao.generated.resources.bolao_common_yesterday_chip
import bolaodagalera.feature_bolao.generated.resources.knockout_tab_empty_message
import bolaodagalera.feature_bolao.generated.resources.rodada_selector_chip_tomorrow
import com.lpstudio.bolaodagalera.designsystem.components.BolaoFullScreenLoading
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLinearProgressIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

// Internal sentinel only, never shown as-is - the visible chip text comes
// from the shared Res.string.bolao_common_today_chip (same "HOJE" label used
// by RodadaSelector for the group-stage/points-based tabs, kept in sync in
// one place instead of duplicating the string here).
private const val TODAY_LABEL = "__TODAY__"

// Same reasoning as TODAY_LABEL - internal sentinel only, visible text comes
// from Res.string.rodada_selector_chip_tomorrow (same "AMANHÃ" label used by
// RodadaSelector).
private const val TOMORROW_LABEL = "__TOMORROW__"

// Same reasoning as TODAY_LABEL/TOMORROW_LABEL - visible text comes from
// Res.string.bolao_common_yesterday_chip (same "ONTEM" label used by
// RodadaSelector for the group-stage/points-based tabs).
private const val YESTERDAY_LABEL = "__YESTERDAY__"
private const val LIVE_WINDOW_MILLIS = 3 * 3600_000L

private class KnockoutComputedState(
    val phaseOrder: List<Phase>,
    val tz: TimeZone,
    val now: Long,
    val todayDate: LocalDate,
    val hasMatchYesterday: Boolean,
    val hasMatchToday: Boolean,
    val hasMatchTomorrow: Boolean,
    val labels: List<String>
)

@Composable
private fun rememberKnockoutComputedState(matches: List<Match>, isTwoLegged: Boolean): KnockoutComputedState {
    val phaseOrder = remember(matches) { computeKnockoutPhaseOrder(matches) }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val hasMatchYesterday = remember(matches, todayDate) { hasKnockoutMatchYesterday(matches, todayDate, tz) }
    val hasMatchToday = remember(matches, todayDate) { hasKnockoutMatchToday(matches, todayDate, tz, now) }
    val hasMatchTomorrow = remember(matches, todayDate) { hasKnockoutMatchTomorrow(matches, todayDate, tz) }
    val labels = remember(phaseOrder, isTwoLegged) { computeKnockoutLabels(phaseOrder, isTwoLegged) }
    return KnockoutComputedState(phaseOrder, tz, now, todayDate, hasMatchYesterday, hasMatchToday, hasMatchTomorrow, labels)
}

@Composable
private fun KnockoutAutoSelectionEffect(
    matches: List<Match>,
    computed: KnockoutComputedState,
    selectedPhase: Phase?,
    selectedLabel: String?,
    onLabelChange: (String?) -> Unit,
    onPhaseChange: (Phase?) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current
    // Runs only while this screen is in the foreground (RESUMED), so it doesn't race
    // with navigation to the prediction screen. selectedPhase/selectedLabel/listState
    // are all already rememberSaveable, so the correct tab and scroll position return
    // on their own when coming back from a prediction — this effect only handles the
    // initial auto-selection (live/next match).
    LaunchedEffect(matches, selectedPhase) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            handleKnockoutAutoSelection(
                matches = matches,
                labels = computed.labels,
                selectedPhase = selectedPhase,
                selectedLabel = selectedLabel,
                hasMatchToday = computed.hasMatchToday,
                hasMatchTomorrow = computed.hasMatchTomorrow,
                onLabelChange = onLabelChange,
                onPhaseChange = onPhaseChange
            )
        }
    }
}

@Composable
fun KnockoutTab(
    data: MatchTabData,
    selectedPhase: Phase?,
    onPhaseChange: (Phase?) -> Unit,
    selectedLabel: String?,
    onLabelChange: (String?) -> Unit,
    listState: LazyListState,
    actions: MatchTabActions,
    championship: Championship = Championship.DEFAULT
) {
    val matches = data.matches
    if (data.isLoading && matches.isEmpty()) {
        BolaoFullScreenLoading()
        return
    }
    val computed = rememberKnockoutComputedState(matches, championship.isTwoLegged)
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }

    KnockoutAutoSelectionEffect(matches, computed, selectedPhase, selectedLabel, onLabelChange, onPhaseChange)
    Column(Modifier.fillMaxSize()) {
        if (computed.labels.isNotEmpty()) {
            KnockoutPhaseSelectorBar(
                matches = matches,
                labels = computed.labels,
                selectedLabel = selectedLabel,
                hasMatchYesterday = computed.hasMatchYesterday,
                hasMatchToday = computed.hasMatchToday,
                hasMatchTomorrow = computed.hasMatchTomorrow,
                onLabelChange = onLabelChange,
                onPhaseChange = onPhaseChange
            )
        }

        KnockoutMatchesBox(
            data = data,
            matches = matches,
            computed = computed,
            selectedPhase = selectedPhase,
            selectedLabel = selectedLabel,
            listState = listState,
            showShadow = showShadow,
            actions = actions,
            championship = championship
        )
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.KnockoutMatchesBox(
    data: MatchTabData,
    matches: List<Match>,
    computed: KnockoutComputedState,
    selectedPhase: Phase?,
    selectedLabel: String?,
    listState: LazyListState,
    showShadow: Boolean,
    actions: MatchTabActions,
    championship: Championship
) {
    Box(Modifier.weight(1f)) {
        val phaseMatches = remember(
            matches,
            selectedPhase,
            selectedLabel,
            championship.isTwoLegged,
            computed.todayDate,
            computed.now
        ) {
            computeKnockoutPhaseMatches(
                matches,
                selectedPhase,
                selectedLabel,
                championship.isTwoLegged,
                computed.todayDate,
                computed.tz,
                computed.now
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
        ) {
            if (phaseMatches.isEmpty() && selectedPhase == Phase.FRIENDLIES) {
                knockoutEmptyState()
            }
            knockoutMatchesList(
                phaseMatches,
                data.predictions,
                data.isAdmin,
                data.bolaoCreatedAt,
                matches,
                championship.isTwoLegged,
                actions
            )
        }

        ScrollTopShadow(visible = showShadow)

        if (data.isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                BolaoLinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

private fun computeKnockoutPhaseOrder(matches: List<Match>): List<Phase> {
    val allPhases = listOf(
        Phase.ROUND_OF_32,
        Phase.ROUND_OF_16,
        Phase.QUARTERFINALS,
        Phase.SEMIFINALS,
        Phase.THIRD_PLACE,
        Phase.FINAL
    )
    return allPhases.filter { phase -> matches.any { it.phase == phase } }
}

/** Matches after midnight before this hour still count as "today" (late-night kickoffs). */
private const val EARLY_MORNING_CUTOFF_HOUR = 4

private fun hasKnockoutMatchToday(matches: List<Match>, todayDate: LocalDate, tz: TimeZone, now: Long): Boolean =
    matches.filter { it.phase != Phase.GROUP_STAGE }.any {
        val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
        val mDate = mTime.date
        mDate == todayDate ||
            (mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < EARLY_MORNING_CUTOFF_HOUR) ||
            (now in it.matchDateMillis..(it.matchDateMillis + LIVE_WINDOW_MILLIS))
    }

/** Tomorrow's calendar date, excluding early-morning kickoffs already claimed by [hasKnockoutMatchToday]'s cutoff. */
private fun hasKnockoutMatchTomorrow(matches: List<Match>, todayDate: LocalDate, tz: TimeZone): Boolean {
    val tomorrowDate = LocalDate.fromEpochDays(todayDate.toEpochDays() + 1)
    return matches.filter { it.phase != Phase.GROUP_STAGE }.any {
        val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
        mTime.date == tomorrowDate && mTime.hour >= EARLY_MORNING_CUTOFF_HOUR
    }
}

/** Yesterday's calendar date - plain calendar-day check, no live-window/early-morning special-casing (that only matters "right now"). */
private fun hasKnockoutMatchYesterday(matches: List<Match>, todayDate: LocalDate, tz: TimeZone): Boolean {
    val yesterdayDate = LocalDate.fromEpochDays(todayDate.toEpochDays() - 1)
    return matches.filter { it.phase != Phase.GROUP_STAGE }.any {
        Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == yesterdayDate
    }
}

private fun computeKnockoutLabels(phaseOrder: List<Phase>, isTwoLegged: Boolean): List<String> = if (isTwoLegged) {
    phaseOrder.flatMap { phase ->
        if (phase == Phase.FINAL || phase == Phase.THIRD_PLACE) {
            listOf(phase.label)
        } else {
            listOf("${phase.label} - Ida", "${phase.label} - Volta")
        }
    }
} else {
    phaseOrder.map { it.label }
}

/**
 * The first label with an unfinished match ("next relevant" phase), or the
 * last label once everything's finished. Shared by the auto-selection
 * effect (which phase tab opens by default) and the chip bar (which phase
 * chip is styled as "current" vs "past") so both agree on the same phase.
 */
private fun computeCurrentKnockoutLabel(matches: List<Match>, labels: List<String>): String? = labels.find { label ->
    val base = label.substringBefore(" - ")
    val isVolta = label.contains("Volta")
    matches.any { m ->
        m.phase.label == base &&
            (if (isVolta) m.isSecondLeg else !m.isSecondLeg) &&
            !m.isFinished
    }
} ?: labels.lastOrNull()

/**
 * Same Hoje > Amanhã > next-relevant-phase priority as [handleKnockoutAutoSelection],
 * exposed so BolaoDetailState.kt's back-navigation/reset logic agrees on what
 * "default" means for this tab - it used to duplicate this decision with its own
 * (drifted) copy, comparing against the localized "HOJE" string instead of the
 * TODAY_LABEL/TOMORROW_LABEL sentinels actually stored in selectedLabel, which
 * broke system-back on the Hoje/Amanhã chips (it looked like the user was never
 * in the "default" state, so back kept resetting the tab instead of leaving it).
 */
internal fun computeKnockoutDefaultSelection(matches: List<Match>, isTwoLegged: Boolean): Pair<Phase?, String> {
    val phaseOrder = computeKnockoutPhaseOrder(matches)
    val labels = computeKnockoutLabels(phaseOrder, isTwoLegged)
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date

    if (hasKnockoutMatchToday(matches, todayDate, tz, now)) return Phase.FRIENDLIES to TODAY_LABEL
    if (hasKnockoutMatchTomorrow(matches, todayDate, tz)) return Phase.FRIENDLIES to TOMORROW_LABEL

    val nextRelevantLabel = computeCurrentKnockoutLabel(matches, labels) ?: return Phase.FRIENDLIES to TODAY_LABEL
    val base = nextRelevantLabel.substringBefore(" - ")
    return Phase.entries.find { it.label == base } to nextRelevantLabel
}

/** Auto-selects, in priority order, Hoje > Amanhã > the next relevant unfinished phase - on first load only. */
private fun handleKnockoutAutoSelection(
    matches: List<Match>,
    labels: List<String>,
    selectedPhase: Phase?,
    selectedLabel: String?,
    hasMatchToday: Boolean,
    hasMatchTomorrow: Boolean,
    onLabelChange: (String?) -> Unit,
    onPhaseChange: (Phase?) -> Unit
) {
    val isFirstLoad = selectedLabel == null
    val isOnStartMarker = selectedPhase == Phase.FRIENDLIES
    if (!isOnStartMarker && !isFirstLoad) return

    if (hasMatchToday) {
        onLabelChange(TODAY_LABEL)
        onPhaseChange(Phase.FRIENDLIES)
        return
    }
    if (hasMatchTomorrow) {
        onLabelChange(TOMORROW_LABEL)
        onPhaseChange(Phase.FRIENDLIES)
        return
    }
    val nextRelevantLabel = computeCurrentKnockoutLabel(matches, labels)

    if (nextRelevantLabel != null) {
        onLabelChange(nextRelevantLabel)
        val base = nextRelevantLabel.substringBefore(" - ")
        onPhaseChange(Phase.entries.find { it.label == base })
    }
}

private fun computeKnockoutPhaseMatches(
    matches: List<Match>,
    selectedPhase: Phase?,
    selectedLabel: String?,
    isTwoLegged: Boolean,
    todayDate: LocalDate,
    tz: TimeZone,
    now: Long
): List<Match> = when {
    selectedPhase == Phase.FRIENDLIES && selectedLabel == TOMORROW_LABEL ->
        computeTomorrowKnockoutMatches(matches, todayDate, tz)
    selectedPhase == Phase.FRIENDLIES && selectedLabel == YESTERDAY_LABEL ->
        computeYesterdayKnockoutMatches(matches, todayDate, tz)
    selectedPhase == Phase.FRIENDLIES -> computeTodayKnockoutMatches(matches, todayDate, tz, now)
    isTwoLegged && selectedLabel != null && selectedLabel != TODAY_LABEL && selectedLabel != TOMORROW_LABEL ->
        computeTwoLeggedPhaseMatches(matches, selectedLabel)
    else -> matches.filter { it.phase == selectedPhase }.sortedBy { it.matchDateMillis }
}

private fun computeTodayKnockoutMatches(matches: List<Match>, todayDate: LocalDate, tz: TimeZone, now: Long): List<Match> =
    matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz)
        val mDate = mTime.date
        val isTomorrowEarly = mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < EARLY_MORNING_CUTOFF_HOUR
        val isRecentlyFinished = now in m.matchDateMillis..(m.matchDateMillis + LIVE_WINDOW_MILLIS)
        mDate == todayDate || isTomorrowEarly || isRecentlyFinished
    }.sortedWith(
        compareByDescending<Match> { matchUrgency(it, now) }.thenBy { it.matchDateMillis }
    )

/** Same "tomorrow" definition as [hasKnockoutMatchTomorrow] - no live/urgency sorting needed, nothing in it could be live yet. */
private fun computeTomorrowKnockoutMatches(matches: List<Match>, todayDate: LocalDate, tz: TimeZone): List<Match> {
    val tomorrowDate = LocalDate.fromEpochDays(todayDate.toEpochDays() + 1)
    return matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz)
        mTime.date == tomorrowDate && mTime.hour >= EARLY_MORNING_CUTOFF_HOUR
    }.sortedBy { it.matchDateMillis }
}

/** Same "yesterday" definition as [hasKnockoutMatchYesterday] - already happened, so plain chronological order is enough. */
private fun computeYesterdayKnockoutMatches(matches: List<Match>, todayDate: LocalDate, tz: TimeZone): List<Match> {
    val yesterdayDate = LocalDate.fromEpochDays(todayDate.toEpochDays() - 1)
    return matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
        Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz).date == yesterdayDate
    }.sortedBy { it.matchDateMillis }
}

private val LIVE_STATUSES = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")

private const val PREDICTION_LOCK_LEAD_MILLIS = 60_000L
private const val URGENCY_LIVE_OR_LOCKED = 2
private const val URGENCY_UNFINISHED = 1

/** Sort priority for the "today" list: live/locked-unfinished matches first, then unfinished, then finished. */
private fun matchUrgency(match: Match, now: Long): Int {
    val isLive = match.status in LIVE_STATUSES
    val isLocked = now >= (match.matchDateMillis - PREDICTION_LOCK_LEAD_MILLIS)
    val isNotFinished = match.status != "FINISHED"
    return when {
        isLive || (isLocked && isNotFinished) -> URGENCY_LIVE_OR_LOCKED
        isNotFinished -> URGENCY_UNFINISHED
        else -> 0
    }
}

/** Whether both team codes are known (not placeholders), so ties can be grouped by team pairing instead of by id. */
private fun hasKnownTeams(homeCode: String, awayCode: String): Boolean =
    homeCode != "TBD" && awayCode != "TBD" && homeCode.isNotBlank() && awayCode.isNotBlank()

private const val TIE_PRIORITY_FINISHED = 3
private const val TIE_PRIORITY_SCORED = 2
private const val TIE_PRIORITY_CURRENT_SEASON = 1

/** Priority used to pick the representative match for a two-legged tie: finished > scored > current-season > other. */
private fun twoLeggedRepresentativePriority(match: Match): Int = when {
    match.status == "FINISHED" -> TIE_PRIORITY_FINISHED
    match.homeScore != null -> TIE_PRIORITY_SCORED
    match.id.startsWith("CLI-2026") -> TIE_PRIORITY_CURRENT_SEASON
    else -> 0
}

private const val NO_EXPLICIT_ORDER_SENTINEL = 99

private fun computeTwoLeggedPhaseMatches(matches: List<Match>, selectedLabel: String): List<Match> {
    val base = selectedLabel.substringBefore(" - ")
    val isVolta = selectedLabel.contains("Volta")
    val phaseMatchesFiltered = matches.filter { it.phase.label.equals(base, true) }
    return phaseMatchesFiltered.groupBy {
        if (it.matchOrder > 0) {
            it.matchOrder.toString()
        } else if (hasKnownTeams(it.homeTeamCode, it.awayTeamCode)) {
            listOf(it.homeTeamCode, it.awayTeamCode).sorted().joinToString("-")
        } else {
            it.id.substringBefore("-L")
        }
    }.values.mapNotNull { pair ->
        val leg = if (isVolta) pair.filter { it.isSecondLeg } else pair.filter { !it.isSecondLeg }
        leg.maxByOrNull(::twoLeggedRepresentativePriority)
    }.sortedBy { it.matchOrder.takeIf { o -> o > 0 } ?: NO_EXPLICIT_ORDER_SENTINEL }
}

@Composable
private fun KnockoutPhaseSelectorBar(
    matches: List<Match>,
    labels: List<String>,
    selectedLabel: String?,
    hasMatchYesterday: Boolean,
    hasMatchToday: Boolean,
    hasMatchTomorrow: Boolean,
    onLabelChange: (String?) -> Unit,
    onPhaseChange: (Phase?) -> Unit
) {
    val currentLabel = remember(matches, labels) { computeCurrentKnockoutLabel(matches, labels) }
    Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = BolaoSpacing.sm)) {
        KnockoutPhaseSelector(
            labels = labels,
            selectedLabel = selectedLabel,
            currentLabel = currentLabel,
            isUnlocked = true,
            showOntem = hasMatchYesterday,
            showHoje = hasMatchToday,
            showAmanha = hasMatchTomorrow,
            onSelect = { label ->
                onLabelChange(label)
                if (label == TODAY_LABEL || label == TOMORROW_LABEL || label == YESTERDAY_LABEL) {
                    onPhaseChange(Phase.FRIENDLIES)
                } else {
                    val phaseName = label?.substringBefore(" - ")
                    onPhaseChange(Phase.entries.find { p -> p.label == phaseName })
                }
            }
        )
    }
}

private fun LazyListScope.knockoutEmptyState() {
    item {
        Box(
            modifier = Modifier.fillMaxWidth().padding(top = BolaoSpacing.huge),
            contentAlignment = Alignment.Center
        ) {
            BolaoText(
                stringResource(Res.string.knockout_tab_empty_message),
                color = TextMuted,
                fontSize = BolaoTypography.bodyLarge.fontSize
            )
        }
    }
}

private fun LazyListScope.knockoutMatchesList(
    phaseMatches: List<Match>,
    predictions: Map<String, Prediction>,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    allMatches: List<Match>,
    isTwoLegged: Boolean,
    actions: MatchTabActions
) {
    items(phaseMatches, key = { it.id }) { m ->
        MatchCard(
            match = m,
            prediction = predictions[m.id],
            options =
            MatchCardOptions(
                isAdmin = isAdmin,
                bolaoCreatedAt = bolaoCreatedAt,
                allMatches = allMatches,
                isTwoLegged = isTwoLegged
            ),
            onClick = { actions.onMatchClick(m.id) },
            onShowAllPredictions = { actions.onShowAllPredictions(m) },
            onOpenAdminScoreDialog = { actions.onOpenAdminScoreDialog(m) }
        )
    }
}

/** The Ontem/Hoje/Amanhã chips (if applicable) followed by one chip per phase label, past/current-styled relative to [currentLabel]. */
@Composable
private fun KnockoutPhaseSelector(
    labels: List<String>,
    selectedLabel: String?,
    currentLabel: String?,
    isUnlocked: Boolean,
    showOntem: Boolean,
    showHoje: Boolean,
    showAmanha: Boolean,
    onSelect: (String?) -> Unit
) {
    val currentIndex = currentLabel?.let { labels.indexOf(it) } ?: -1
    val yesterdayLabelText = stringResource(Res.string.bolao_common_yesterday_chip)
    val todayLabelText = stringResource(Res.string.bolao_common_today_chip)
    val tomorrowLabelText = stringResource(Res.string.rodada_selector_chip_tomorrow)
    val chips =
        buildList {
            if (showOntem) {
                add(
                    TabChipSpec(
                        key = YESTERDAY_LABEL,
                        label = yesterdayLabelText,
                        isSelected = selectedLabel == YESTERDAY_LABEL,
                        isUnlocked = true,
                        onClick = { onSelect(YESTERDAY_LABEL) }
                    )
                )
            }
            if (showHoje) {
                add(
                    TabChipSpec(
                        key = TODAY_LABEL,
                        label = todayLabelText,
                        isSelected = selectedLabel == TODAY_LABEL,
                        isUnlocked = true,
                        onClick = { onSelect(TODAY_LABEL) }
                    )
                )
            }
            if (showAmanha) {
                add(
                    TabChipSpec(
                        key = TOMORROW_LABEL,
                        label = tomorrowLabelText,
                        isSelected = selectedLabel == TOMORROW_LABEL,
                        isUnlocked = true,
                        onClick = { onSelect(TOMORROW_LABEL) }
                    )
                )
            }
            labels.forEach { l ->
                add(
                    TabChipSpec(
                        key = l,
                        label = l,
                        isSelected = selectedLabel == l,
                        isUnlocked = isUnlocked,
                        isPast = currentIndex != -1 && labels.indexOf(l) < currentIndex,
                        isCurrent = l == currentLabel,
                        onClick = { onSelect(l) }
                    )
                )
            }
        }
    TabSelectorRow(chips)
}
