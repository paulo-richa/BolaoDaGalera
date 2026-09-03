package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.group_stage_tab_empty_today
import bolaodagalera.feature_bolao.generated.resources.group_stage_tab_empty_tomorrow
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLinearProgressIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.DeepNavy
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Sentinel value of [selectedRound] for the "Tomorrow" tab (0 is already used by "Today"). */
const val TOMORROW_ROUND = -1

/** Common data shared by [GroupStageTab] and [KnockoutTab] - the same underlying match/prediction state, viewed differently. */
data class MatchTabData(
    val matches: List<Match>,
    val predictions: Map<String, Prediction>,
    val isLoading: Boolean,
    val isAdmin: Boolean,
    val bolaoCreatedAt: Long
)

/** Groups the [MatchCard] click callbacks so extracted sub-composables don't need one parameter per callback. */
data class MatchTabActions(
    val onMatchClick: (String) -> Unit,
    val onShowAllPredictions: (Match) -> Unit,
    val onOpenAdminScoreDialog: (Match) -> Unit
)

private class GroupStageComputedState(
    val unlocked: Set<Int>,
    val tz: TimeZone,
    val now: Long,
    val todayDate: LocalDate,
    val currentRound: Int,
    val roundMatches: List<Match>,
    val byGroup: Map<String, List<Match>>,
    val hasMatchToday: Boolean,
    val hasMatchTomorrow: Boolean
)

@Composable
private fun rememberGroupStageComputedState(matches: List<Match>, selectedRound: Int): GroupStageComputedState {
    val unlocked = remember(matches) { matches.map { it.groupRound() }.toSet() }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val tomorrowDate = remember(todayDate) { LocalDate.fromEpochDays(todayDate.toEpochDays() + 1) }
    val hasMatchToday = remember(matches, todayDate) { matches.any { it.isOnDate(todayDate, tz) } }
    val hasMatchTomorrow = remember(matches, tomorrowDate) { matches.any { it.isOnDate(tomorrowDate, tz) } }
    val currentRound = remember(matches, now) { computeCurrentGroupRound(matches, now) }
    val roundMatches =
        remember(matches, selectedRound, todayDate, tomorrowDate, now) {
            computeGroupRoundMatches(matches, selectedRound, todayDate, tomorrowDate, tz, now)
        }
    val byGroup = remember(roundMatches) { roundMatches.groupBy { it.group ?: "" } }
    return GroupStageComputedState(
        unlocked = unlocked,
        tz = tz,
        now = now,
        todayDate = todayDate,
        currentRound = currentRound,
        roundMatches = roundMatches,
        byGroup = byGroup,
        hasMatchToday = hasMatchToday,
        hasMatchTomorrow = hasMatchTomorrow
    )
}

@Composable
private fun GroupStageInitialScrollEffect(
    matches: List<Match>,
    computed: GroupStageComputedState,
    selectedRound: Int,
    expandedGroups: SnapshotStateList<String>,
    listState: LazyListState
) {
    var hasHandledScroll by rememberSaveable(selectedRound) { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    // Runs only while this screen is in the foreground (RESUMED), so it doesn't race
    // with navigation to the prediction screen — this scroll is only the initial focus
    // (live/next match). The scroll position itself (including on returning from a
    // prediction) is already preserved on its own since listState is rememberSaveable.
    LaunchedEffect(selectedRound, matches.isNotEmpty(), computed.byGroup) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (matches.isEmpty() || computed.byGroup.isEmpty()) return@repeatOnLifecycle
            if (!hasHandledScroll) {
                handleGroupStageInitialScroll(
                    matches = matches,
                    byGroup = computed.byGroup,
                    roundMatches = computed.roundMatches,
                    selectedRound = selectedRound,
                    todayDate = computed.todayDate,
                    tz = computed.tz,
                    now = computed.now,
                    expandedGroups = expandedGroups,
                    listState = listState
                )
                hasHandledScroll = true
            }
        }
    }
}

@Composable
fun GroupStageTab(
    data: MatchTabData,
    selectedRound: Int,
    onRoundChange: (Int) -> Unit,
    listState: LazyListState,
    expandedGroups: SnapshotStateList<String>,
    actions: MatchTabActions
) {
    val matches = data.matches
    val computed = rememberGroupStageComputedState(matches, selectedRound)
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    GroupStageInitialScrollEffect(matches, computed, selectedRound, expandedGroups, listState)
    Column(Modifier.fillMaxSize()) {
        GroupStageRoundSelectorBar(
            selectedRound = selectedRound,
            unlocked = computed.unlocked,
            hasMatchToday = computed.hasMatchToday,
            hasMatchTomorrow = computed.hasMatchTomorrow,
            currentRound = computed.currentRound,
            onRoundChange = onRoundChange
        )
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)
            ) {
                val isDayTab = selectedRound == 0 || selectedRound == TOMORROW_ROUND
                if (computed.roundMatches.isEmpty() && isDayTab) {
                    groupStageEmptyState(selectedRound)
                }
                if (isDayTab && computed.roundMatches.isNotEmpty()) {
                    dayMatchesList(computed.roundMatches, data.predictions, data.isAdmin, data.bolaoCreatedAt, matches, actions)
                } else {
                    groupedMatchesList(
                        computed.byGroup,
                        expandedGroups,
                        data.predictions,
                        data.isAdmin,
                        data.bolaoCreatedAt,
                        matches,
                        actions
                    )
                }
            }
            ScrollTopShadow(visible = showShadow)
            if (data.isLoading) {
                BolaoLinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}

/** Whether [this] match falls on [date] in the given [tz]. */
private fun Match.isOnDate(date: LocalDate, tz: TimeZone): Boolean =
    Instant.fromEpochMilliseconds(matchDateMillis).toLocalDateTime(tz).date == date

private fun computeCurrentGroupRound(matches: List<Match>, now: Long): Int {
    val upcoming = matches.filter { !it.isFinished && it.matchDateMillis > now }
        .minByOrNull { it.matchDateMillis }?.groupRound()
    return upcoming ?: matches.maxByOrNull { it.matchDateMillis }?.groupRound() ?: 0
}

/** A match still shows in the "today" list this long after kickoff, in case it's running long. */
private const val TODAY_LIST_GRACE_MILLIS = 3 * 3_600_000L

private fun computeGroupRoundMatches(
    matches: List<Match>,
    selectedRound: Int,
    todayDate: LocalDate,
    tomorrowDate: LocalDate,
    tz: TimeZone,
    now: Long
): List<Match> = when (selectedRound) {
    0 ->
        matches.filter {
            it.isOnDate(todayDate, tz) || (now in it.matchDateMillis..(it.matchDateMillis + TODAY_LIST_GRACE_MILLIS))
        }.sortedBy { it.matchDateMillis }
    TOMORROW_ROUND ->
        matches.filter { it.isOnDate(tomorrowDate, tz) }.sortedBy { it.matchDateMillis }
    else ->
        matches.filter { it.groupRound() == selectedRound }.sortedBy { it.matchDateMillis }
}

/**
 * Auto-focuses the list on first display: scrolls to and expands the group with the live/next
 * match, or falls back to the first group when there's nothing to focus on.
 */
private suspend fun handleGroupStageInitialScroll(
    matches: List<Match>,
    byGroup: Map<String, List<Match>>,
    roundMatches: List<Match>,
    selectedRound: Int,
    todayDate: LocalDate,
    tz: TimeZone,
    now: Long,
    expandedGroups: SnapshotStateList<String>,
    listState: LazyListState
) {
    val sorted = byGroup.entries.sortedBy { it.key }
    expandedGroups.clear()
    if (roundMatches.isNotEmpty() && roundMatches.all { it.isFinished } && selectedRound != 0) {
        listState.scrollToItem(0)
        return
    }
    val isDayTab = selectedRound == 0 || selectedRound == TOMORROW_ROUND
    val window = GROUP_STAGE_FOCUS_WINDOW_MILLIS
    val focus = findGroupStageFocusMatch(matches, todayDate, tz, now, window)
    if (focus == null) {
        if (isDayTab) {
            expandedGroups.addAll(byGroup.keys)
        } else {
            sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
        }
        listState.scrollToItem(0)
        return
    }
    val group = focus.group ?: ""
    val round = focus.groupRound()
    when {
        isDayTab -> {
            expandedGroups.addAll(byGroup.keys)
            listState.scrollToItem(0)
        }
        selectedRound == round -> {
            expandedGroups.add(group)
            expandedGroups.addAll(activeGroupStageGroups(matches, selectedRound, todayDate, tz, now, window))
            listState.scrollToItem(groupStageScrollIndex(sorted, group))
        }
        else -> {
            sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
            listState.scrollToItem(0)
        }
    }
}

/** Time window (2h30) after kickoff during which a match is still considered "live" for focus purposes. */
private const val GROUP_STAGE_FOCUS_WINDOW_MILLIS = 2 * 60 * 60 * 1000L + (30 * 60 * 1000L)

/** Live match right now, else the next unstarted match today, else the next unstarted match overall. */
private fun findGroupStageFocusMatch(matches: List<Match>, todayDate: LocalDate, tz: TimeZone, now: Long, window: Long): Match? =
    matches.filter { it.phase == Phase.GROUP_STAGE }.let { all ->
        all.find { now in it.matchDateMillis..(it.matchDateMillis + window) }
            ?: all.filter { it.isOnDate(todayDate, tz) && it.matchDateMillis > now }.minByOrNull { it.matchDateMillis }
            ?: all.filter { it.matchDateMillis > now }.minByOrNull { it.matchDateMillis }
    }

/** Groups (besides the focus match's own group) that also have a live/imminent match today, so they auto-expand too. */
private fun activeGroupStageGroups(
    matches: List<Match>,
    selectedRound: Int,
    todayDate: LocalDate,
    tz: TimeZone,
    now: Long,
    window: Long
): List<String> = matches.filter {
    it.groupRound() == selectedRound &&
        it.isOnDate(todayDate, tz) &&
        it.matchDateMillis + window >= now &&
        !it.isFinished
}.mapNotNull { it.group }

/** Item index of [targetGroup]'s header within the flattened (header + matches + spacer) list. */
private fun groupStageScrollIndex(sorted: List<Map.Entry<String, List<Match>>>, targetGroup: String): Int {
    var index = 0
    for (entry in sorted) {
        if (entry.key == targetGroup) break
        index += 1 + entry.value.size + 1
    }
    return index
}

@Composable
private fun GroupStageRoundSelectorBar(
    selectedRound: Int,
    unlocked: Set<Int>,
    hasMatchToday: Boolean,
    hasMatchTomorrow: Boolean,
    currentRound: Int,
    onRoundChange: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = BolaoSpacing.sm)) {
        RodadaSelector(
            selected = selectedRound,
            unlocked = unlocked,
            showHoje = hasMatchToday,
            showAmanha = hasMatchTomorrow,
            currentRound = currentRound,
            onSelect = {
                if (it == 0 || it == TOMORROW_ROUND || it in unlocked) onRoundChange(it)
            }
        )
    }
}

private fun LazyListScope.groupStageEmptyState(selectedRound: Int) {
    item {
        Box(Modifier.fillMaxWidth().padding(top = BolaoSpacing.huge), contentAlignment = Alignment.Center) {
            val message =
                if (selectedRound == 0) {
                    Res.string.group_stage_tab_empty_today
                } else {
                    Res.string.group_stage_tab_empty_tomorrow
                }
            BolaoText(stringResource(message), color = TextMuted, fontSize = BolaoTypography.bodyLarge.fontSize)
        }
    }
}

/** Flat match list used by the "Today"/"Tomorrow" tabs, which don't group matches by [Match.group]. */
private fun LazyListScope.dayMatchesList(
    roundMatches: List<Match>,
    predictions: Map<String, Prediction>,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    allMatches: List<Match>,
    actions: MatchTabActions
) {
    items(roundMatches, key = { it.id }) { m ->
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = BolaoSpacing.sm)) {
            MatchCard(
                match = m,
                prediction = predictions[m.id],
                options =
                MatchCardOptions(
                    isAdmin = isAdmin,
                    bolaoCreatedAt = bolaoCreatedAt,
                    allMatches = allMatches
                ),
                onClick = { actions.onMatchClick(m.id) },
                onShowAllPredictions = { actions.onShowAllPredictions(m) },
                onOpenAdminScoreDialog = { actions.onOpenAdminScoreDialog(m) }
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

/** Match list grouped by [Match.group] with collapsible group headers, used by the round tabs. */
private fun LazyListScope.groupedMatchesList(
    byGroup: Map<String, List<Match>>,
    expandedGroups: SnapshotStateList<String>,
    predictions: Map<String, Prediction>,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    allMatches: List<Match>,
    actions: MatchTabActions
) {
    byGroup.entries.sortedBy { it.key }.forEach { (g, ms) ->
        val isExpanded = expandedGroups.contains(g)
        val isCompleted = ms.all { it.isFinished || predictions.containsKey(it.id) }
        item(key = "header-$g") {
            GroupHeader(group = g, isExpanded = isExpanded, isCompleted = isCompleted, enabled = true, onToggle = {
                if (isExpanded) expandedGroups.remove(g) else expandedGroups.add(g)
            })
        }
        items(ms, key = { it.id }) { m ->
            androidx.compose.animation.AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = BolaoSpacing.sm)) {
                    MatchCard(
                        match = m,
                        prediction = predictions[m.id],
                        // Group stage is never two-legged for labels here
                        options =
                        MatchCardOptions(
                            isAdmin = isAdmin,
                            bolaoCreatedAt = bolaoCreatedAt,
                            allMatches = allMatches
                        ),
                        onClick = { actions.onMatchClick(m.id) },
                        onShowAllPredictions = { actions.onShowAllPredictions(m) },
                        onOpenAdminScoreDialog = { actions.onOpenAdminScoreDialog(m) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        item(key = "spacer-$g") { Spacer(Modifier.height(4.dp)) }
    }
}

/** Fading shadow shown at the top of a scrollable list once it has been scrolled past its start. */
@Composable
internal fun ScrollTopShadow(visible: Boolean) {
    androidx.compose.animation.AnimatedVisibility(visible = visible, enter = fadeIn(), exit = fadeOut()) {
        Box(
            modifier =
            Modifier.fillMaxWidth().height(12.dp)
                .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)))
        )
    }
}
