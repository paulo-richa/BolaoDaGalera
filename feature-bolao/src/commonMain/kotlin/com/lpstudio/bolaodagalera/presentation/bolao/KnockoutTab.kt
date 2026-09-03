package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.knockout_tab_empty_message
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

private const val TODAY_LABEL = "⚽️ HOJE"
private const val LIVE_WINDOW_MILLIS = 3 * 3600_000L

/** Groups the [MatchCard] click callbacks so extracted sub-composables don't need one parameter per callback. */
private data class KnockoutMatchActions(
    val onMatchClick: (String) -> Unit,
    val onShowAllPredictions: (Match) -> Unit,
    val onOpenAdminScoreDialog: (Match) -> Unit
)

@Composable
fun KnockoutTab(
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
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onOpenAdminScoreDialog: (Match) -> Unit,
    championship: Championship = Championship.DEFAULT
) {
    if (isLoading && matches.isEmpty()) {
        BolaoFullScreenLoading()
        return
    }
    val actions = remember(onMatchClick, onShowAllPredictions, onOpenAdminScoreDialog) {
        KnockoutMatchActions(onMatchClick, onShowAllPredictions, onOpenAdminScoreDialog)
    }
    val phaseOrder = remember(matches) { computeKnockoutPhaseOrder(matches) }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val hasMatchToday =
        remember(matches, todayDate) { hasKnockoutMatchToday(matches, todayDate, tz, now) }
    val labels = remember(phaseOrder, championship.isTwoLegged) { computeKnockoutLabels(phaseOrder, championship.isTwoLegged) }
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }

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
                labels = labels,
                selectedPhase = selectedPhase,
                selectedLabel = selectedLabel,
                hasMatchToday = hasMatchToday,
                onLabelChange = onLabelChange,
                onPhaseChange = onPhaseChange
            )
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (labels.isNotEmpty()) {
            KnockoutPhaseSelectorBar(
                labels = labels,
                selectedLabel = selectedLabel,
                hasMatchToday = hasMatchToday,
                onLabelChange = onLabelChange,
                onPhaseChange = onPhaseChange
            )
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
                computeKnockoutPhaseMatches(matches, selectedPhase, selectedLabel, championship.isTwoLegged, todayDate, tz, now)
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
                knockoutMatchesList(phaseMatches, predictions, isAdmin, bolaoCreatedAt, matches, championship.isTwoLegged, actions)
            }

            ScrollTopShadow(visible = showShadow)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
                    BolaoLinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = Color.Transparent
                    )
                }
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

private fun hasKnockoutMatchToday(matches: List<Match>, todayDate: LocalDate, tz: TimeZone, now: Long): Boolean =
    matches.filter { it.phase != Phase.GROUP_STAGE }.any {
        val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
        val mDate = mTime.date
        mDate == todayDate ||
            (mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < 4) ||
            (now in it.matchDateMillis..(it.matchDateMillis + LIVE_WINDOW_MILLIS))
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

/** Auto-selects the "today"/live phase tab on first load, or the next relevant unfinished phase otherwise. */
private fun handleKnockoutAutoSelection(
    matches: List<Match>,
    labels: List<String>,
    selectedPhase: Phase?,
    selectedLabel: String?,
    hasMatchToday: Boolean,
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
    selectedPhase == Phase.FRIENDLIES -> computeTodayKnockoutMatches(matches, todayDate, tz, now)
    isTwoLegged && selectedLabel != null && selectedLabel != TODAY_LABEL ->
        computeTwoLeggedPhaseMatches(matches, selectedLabel)
    else -> matches.filter { it.phase == selectedPhase }.sortedBy { it.matchDateMillis }
}

private fun computeTodayKnockoutMatches(matches: List<Match>, todayDate: LocalDate, tz: TimeZone, now: Long): List<Match> =
    matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis).toLocalDateTime(tz)
        val mDate = mTime.date
        val isTomorrowEarly = mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < 4
        val isRecentlyFinished = now in m.matchDateMillis..(m.matchDateMillis + LIVE_WINDOW_MILLIS)
        mDate == todayDate || isTomorrowEarly || isRecentlyFinished
    }.sortedWith(
        compareByDescending<Match> { matchUrgency(it, now) }.thenBy { it.matchDateMillis }
    )

private val LIVE_STATUSES = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")

/** Sort priority for the "today" list: live/locked-unfinished matches first, then unfinished, then finished. */
private fun matchUrgency(match: Match, now: Long): Int {
    val isLive = match.status in LIVE_STATUSES
    val isLocked = now >= (match.matchDateMillis - 60_000)
    val isNotFinished = match.status != "FINISHED"
    return when {
        isLive || (isLocked && isNotFinished) -> 2
        isNotFinished -> 1
        else -> 0
    }
}

/** Whether both team codes are known (not placeholders), so ties can be grouped by team pairing instead of by id. */
private fun hasKnownTeams(homeCode: String, awayCode: String): Boolean =
    homeCode != "TBD" && awayCode != "TBD" && homeCode.isNotBlank() && awayCode.isNotBlank()

/** Priority used to pick the representative match for a two-legged tie: finished > scored > current-season > other. */
private fun twoLeggedRepresentativePriority(match: Match): Int = when {
    match.status == "FINISHED" -> 3
    match.homeScore != null -> 2
    match.id.startsWith("CLI-2026") -> 1
    else -> 0
}

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
        val leg = if (isVolta) pair.filter { it.id.contains("-L2") } else pair.filter { !it.id.contains("-L2") }
        leg.maxByOrNull(::twoLeggedRepresentativePriority)
    }.sortedBy { it.matchOrder.takeIf { o -> o > 0 } ?: 99 }
}

@Composable
private fun KnockoutPhaseSelectorBar(
    labels: List<String>,
    selectedLabel: String?,
    hasMatchToday: Boolean,
    onLabelChange: (String?) -> Unit,
    onPhaseChange: (Phase?) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = BolaoSpacing.sm)) {
        KnockoutPhaseSelector(
            labels = labels,
            selectedLabel = selectedLabel,
            isUnlocked = true,
            showHoje = hasMatchToday,
            onSelect = { label ->
                onLabelChange(label)
                if (label == TODAY_LABEL) {
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
    actions: KnockoutMatchActions
) {
    items(phaseMatches, key = { it.id }) { m ->
        MatchCard(
            match = m,
            prediction = predictions[m.id],
            isAdmin = isAdmin,
            bolaoCreatedAt = bolaoCreatedAt,
            forceLocked = false,
            showSocialBadge = true,
            allMatches = allMatches,
            isTwoLegged = isTwoLegged,
            onClick = { actions.onMatchClick(m.id) },
            onShowAllPredictions = { actions.onShowAllPredictions(m) },
            onOpenAdminScoreDialog = { actions.onOpenAdminScoreDialog(m) }
        )
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
            modifier = Modifier.fillMaxWidth().padding(vertical = BolaoSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (showHoje) {
                item {
                    FilterChip(
                        label = TODAY_LABEL,
                        isSelected = selectedLabel == TODAY_LABEL,
                        isUnlocked = true,
                        onClick = { onSelect(TODAY_LABEL) }
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
