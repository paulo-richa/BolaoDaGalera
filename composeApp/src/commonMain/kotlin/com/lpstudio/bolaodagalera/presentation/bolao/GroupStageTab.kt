package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import bolaodagalera.composeapp.generated.resources.Res
import bolaodagalera.composeapp.generated.resources.group_stage_tab_empty_today
import bolaodagalera.composeapp.generated.resources.group_stage_tab_empty_tomorrow
import com.lpstudio.bolaodagalera.designsystem.components.BolaoLinearProgressIndicator
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.model.Prediction
import com.lpstudio.bolaodagalera.presentation.theme.DeepNavy
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.util.TimeSource
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

/** Sentinela de [selectedRound] para a aba "Amanhã" (0 já é usado por "Hoje"). */
const val TOMORROW_ROUND = -1

@Composable
fun GroupStageTab(
    matches: List<Match>,
    predictions: Map<String, Prediction>,
    isLoading: Boolean,
    isAdmin: Boolean,
    bolaoCreatedAt: Long,
    selectedRound: Int,
    onRoundChange: (Int) -> Unit,
    listState: LazyListState,
    expandedGroups: SnapshotStateList<String>,
    onMatchClick: (String) -> Unit,
    onShowAllPredictions: (Match) -> Unit,
    onOpenAdminScoreDialog: (Match) -> Unit
) {
    val unlocked = remember(matches) { matches.map { it.groupRound() }.toSet() }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val tomorrowDate = remember(todayDate) { kotlinx.datetime.LocalDate.fromEpochDays(todayDate.toEpochDays() + 1) }
    val hasMatchToday =
        remember(
            matches,
            todayDate
        ) { matches.any { Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == todayDate } }
    val hasMatchTomorrow =
        remember(
            matches,
            tomorrowDate
        ) { matches.any { Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == tomorrowDate } }
    val currentRound =
        remember(matches, now) {
            val upcoming = matches.filter { !it.isFinished && it.matchDateMillis > now }
                .minByOrNull { it.matchDateMillis }?.groupRound()
            upcoming ?: matches.maxByOrNull { it.matchDateMillis }?.groupRound() ?: 0
        }
    val roundMatches =
        remember(matches, selectedRound, todayDate, tomorrowDate, now) {
            if (selectedRound == 0) {
                matches.filter {
                    val mDate = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date
                    mDate == todayDate || (now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L))
                }.sortedBy { it.matchDateMillis }
            } else if (selectedRound == TOMORROW_ROUND) {
                matches.filter {
                    Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz).date == tomorrowDate
                }.sortedBy { it.matchDateMillis }
            } else {
                matches.filter { it.groupRound() == selectedRound }.sortedBy { it.matchDateMillis }
            }
        }
    val byGroup = remember(roundMatches) { roundMatches.groupBy { it.group ?: "" } }
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }
    var hasHandledScroll by rememberSaveable(selectedRound) { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    // Roda só enquanto esta tela está em primeiro plano (RESUMED), pra não competir
    // com a navegação pra tela de palpite - o scroll aqui é só o foco inicial
    // (jogo "ao vivo"/próximo). A posição de rolagem em si (inclusive ao voltar de
    // um palpite) já é preservada sozinha porque listState é rememberSaveable.
    LaunchedEffect(selectedRound, matches.isNotEmpty(), byGroup) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            if (matches.isEmpty() || byGroup.isEmpty()) return@repeatOnLifecycle
            val sorted = byGroup.entries.sortedBy { it.key }
            if (!hasHandledScroll) {
                expandedGroups.clear()
                if (roundMatches.isNotEmpty() && roundMatches.all { it.isFinished } && selectedRound != 0) {
                    listState.scrollToItem(0)
                    hasHandledScroll = true
                    return@repeatOnLifecycle
                }
                val window = 2 * 60 * 60 * 1000L + (30 * 60 * 1000L)
                val focus =
                    matches.filter { it.phase == Phase.GROUP_STAGE }.let { all ->
                        all.find { now in it.matchDateMillis..(it.matchDateMillis + window) }
                            ?: all.filter {
                                val matchDate = Instant.fromEpochMilliseconds(it.matchDateMillis)
                                    .toLocalDateTime(tz).date
                                matchDate == todayDate && it.matchDateMillis > now
                            }.minByOrNull {
                                it.matchDateMillis
                            } ?: all.filter {
                            it.matchDateMillis > now
                        }.minByOrNull { it.matchDateMillis }
                    }
                if (focus != null) {
                    val group = focus.group ?: ""
                    val round = focus.groupRound()
                    if (selectedRound == 0 || selectedRound == TOMORROW_ROUND) {
                        expandedGroups.addAll(byGroup.keys)
                        listState.scrollToItem(0)
                    } else if (selectedRound == round) {
                        expandedGroups.add(group)
                        val actG = matches.filter {
                            val sR = it.groupRound() == selectedRound
                            val isT = Instant.fromEpochMilliseconds(it.matchDateMillis)
                                .toLocalDateTime(tz).date == todayDate
                            val isV = it.matchDateMillis + window >= now
                            sR && isT && isV && !it.isFinished
                        }.mapNotNull { it.group }
                        expandedGroups.addAll(actG)
                        var targetIdx = 0
                        for (entry in sorted) {
                            if (entry.key == group) break
                            targetIdx += 1 + entry.value.size + 1
                        }
                        listState.scrollToItem(targetIdx)
                    } else {
                        sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
                        listState.scrollToItem(0)
                    }
                } else {
                    if (selectedRound == 0 || selectedRound == TOMORROW_ROUND) {
                        expandedGroups.addAll(byGroup.keys)
                    } else {
                        sorted.firstOrNull()?.key?.let { expandedGroups.add(it) }
                    }
                    listState.scrollToItem(0)
                }
                hasHandledScroll = true
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = 8.dp)) {
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
        Box(Modifier.weight(1f)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                if (roundMatches.isEmpty() && (selectedRound == 0 || selectedRound == TOMORROW_ROUND)) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                            val msg = if (selectedRound == 0) {
                                stringResource(Res.string.group_stage_tab_empty_today)
                            } else {
                                stringResource(Res.string.group_stage_tab_empty_tomorrow)
                            }
                            BolaoText(msg, color = TextMuted, fontSize = 14.sp)
                        }
                    }
                }
                if ((selectedRound == 0 || selectedRound == TOMORROW_ROUND) && roundMatches.isNotEmpty()) {
                    items(roundMatches, key = { it.id }) { m ->
                        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                            MatchCard(
                                match = m,
                                prediction = predictions[m.id],
                                isAdmin = isAdmin,
                                bolaoCreatedAt = bolaoCreatedAt,
                                showSocialBadge = true,
                                allMatches = matches,
                                isTwoLegged = false,
                                onClick = {
                                    onMatchClick(m.id)
                                },
                                onShowAllPredictions = { onShowAllPredictions(m) },
                                onOpenAdminScoreDialog = { onOpenAdminScoreDialog(m) }
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                    }
                } else {
                    byGroup.entries.sortedBy { it.key }.forEach { (g, ms) ->
                        val isExp = expandedGroups.contains(g)
                        val isComp = ms.all { it.isFinished || predictions.containsKey(it.id) }
                        item(key = "header-$g") {
                            GroupHeader(group = g, isExpanded = isExp, isCompleted = isComp, enabled = true, onToggle = {
                                if (isExp) expandedGroups.remove(g) else expandedGroups.add(g)
                            })
                        }
                        items(ms, key = { it.id }) { m ->
                            androidx.compose.animation.AnimatedVisibility(
                                visible = isExp,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                                ) {
                                    MatchCard(
                                        match = m,
                                        prediction = predictions[m.id],
                                        isAdmin = isAdmin,
                                        bolaoCreatedAt = bolaoCreatedAt,
                                        showSocialBadge = true,
                                        allMatches = matches,
                                        isTwoLegged = false, // Group stage is never two-legged for labels here
                                        onClick = { onMatchClick(m.id) },
                                        onShowAllPredictions = {
                                            onShowAllPredictions(m)
                                        },
                                        onOpenAdminScoreDialog = {
                                            onOpenAdminScoreDialog(m)
                                        }
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                            }
                        }
                        item(key = "spacer-$g") { Spacer(Modifier.height(4.dp)) }
                    }
                }
            }
            androidx.compose.animation.AnimatedVisibility(visible = showShadow, enter = fadeIn(), exit = fadeOut()) {
                Box(
                    modifier =
                    Modifier.fillMaxWidth().height(
                        12.dp
                    ).background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)))
                )
            }
            if (isLoading) {
                BolaoLinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter),
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
