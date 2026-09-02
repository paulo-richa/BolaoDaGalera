package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource

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
    val phaseOrder = remember(matches) {
        val allPhases = listOf(
            Phase.ROUND_OF_32,
            Phase.ROUND_OF_16,
            Phase.QUARTERFINALS,
            Phase.SEMIFINALS,
            Phase.THIRD_PLACE,
            Phase.FINAL
        )
        allPhases.filter { phase -> matches.any { it.phase == phase } }
    }
    val tz = TimeZone.currentSystemDefault()
    val now = TimeSource.nowMillis()
    val todayDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
    val hasMatchToday =
        remember(matches, todayDate) {
            matches.filter { it.phase != Phase.GROUP_STAGE }.any {
                val mTime = Instant.fromEpochMilliseconds(it.matchDateMillis).toLocalDateTime(tz)
                val mDate = mTime.date
                mDate == todayDate ||
                    (mDate.toEpochDays() == todayDate.toEpochDays() + 1 && mTime.hour < 4) ||
                    (now in it.matchDateMillis..(it.matchDateMillis + 3 * 3600_000L))
            }
        }
    val labels =
        remember(phaseOrder, championship.isTwoLegged) {
            if (championship.isTwoLegged) {
                phaseOrder.flatMap { phase ->
                    if (phase == Phase.FINAL || phase == Phase.THIRD_PLACE) {
                        listOf(
                            phase.label
                        )
                    } else {
                        listOf("${phase.label} - Ida", "${phase.label} - Volta")
                    }
                }
            } else {
                phaseOrder.map { it.label }
            }
        }
    val selLabel = selectedLabel
    val showShadow by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 || listState.firstVisibleItemScrollOffset > 0 } }

    val lifecycleOwner = LocalLifecycleOwner.current
    // Roda só enquanto esta tela está em primeiro plano (RESUMED), pra não competir
    // com a navegação pra tela de palpite. selectedPhase/selectedLabel/listState já
    // são todos rememberSaveable, então a aba e a posição de rolagem certas voltam
    // sozinhas ao retornar de um palpite - esse efeito só cuida da auto-seleção
    // inicial (jogo "ao vivo"/próximo).
    LaunchedEffect(matches, selectedPhase) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            // Lógica de Auto-Seleção Inteligente
            val isFirstLoad = selLabel == null
            val isOnStartMarker = selectedPhase == Phase.FRIENDLIES

            if (isOnStartMarker || isFirstLoad) {
                if (hasMatchToday) {
                    onLabelChange("⚽️ HOJE")
                    onPhaseChange(Phase.FRIENDLIES)
                } else {
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
                        val phase = Phase.entries.find { it.label == base }
                        onPhaseChange(phase)
                    }
                }
            }
        }
    }
    Column(Modifier.fillMaxSize()) {
        if (labels.isNotEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().background(DeepNavy).padding(vertical = BolaoSpacing.sm)) {
                KnockoutPhaseSelector(
                    labels = labels,
                    selectedLabel = selectedLabel,
                    isUnlocked = true,
                    showHoje = hasMatchToday,
                    onSelect = { label ->
                        onLabelChange(label)
                        if (label == "⚽️ HOJE") {
                            onPhaseChange(Phase.FRIENDLIES)
                        } else {
                            val phaseName = label?.substringBefore(" - ")
                            val phase = Phase.entries.find { p -> p.label == phaseName }
                            onPhaseChange(phase)
                        }
                    }
                )
            }
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
                if (selectedPhase == Phase.FRIENDLIES) {
                    matches.filter { it.phase != Phase.GROUP_STAGE }.filter { m ->
                        val mTime = Instant.fromEpochMilliseconds(m.matchDateMillis)
                            .toLocalDateTime(tz)
                        val mDate = mTime.date
                        val window = 3 * 3600_000L
                        val isTomorrowEarly = mDate.toEpochDays() == todayDate.toEpochDays() + 1 &&
                            mTime.hour < 4
                        val isRecentlyFinished = now in m.matchDateMillis..(m.matchDateMillis + window)
                        mDate == todayDate || isTomorrowEarly || isRecentlyFinished
                    }.sortedWith(
                        compareByDescending<Match> {
                            val statusLive = listOf("IN_PLAY", "PAUSED", "EXTRA_TIME", "PENALTIES", "LIVE")
                            val isLive = it.status in statusLive
                            val isLocked = now >= (it.matchDateMillis - 60_000)
                            val isNotFin = it.status != "FINISHED"
                            if (isLive || (isLocked && isNotFin)) {
                                2
                            } else if (isNotFin) {
                                1
                            } else {
                                0
                            }
                        }.thenBy { it.matchDateMillis }
                    )
                } else if (championship.isTwoLegged &&
                    selectedLabel != null &&
                    selectedLabel != "⚽️ HOJE"
                ) {
                    val currentLabel = selectedLabel
                    val base = currentLabel.substringBefore(" - ")
                    val isVolta = currentLabel.contains("Volta")
                    val phaseMatchesFiltered = matches.filter { it.phase.label.equals(base, true) }
                    phaseMatchesFiltered.groupBy {
                        if (it.matchOrder > 0) {
                            it.matchOrder.toString()
                        } else {
                            val t1 = it.homeTeamCode
                            val t2 = it.awayTeamCode
                            val codes = listOf(t1, t2)
                            if (t1 != "TBD" &&
                                t2 != "TBD" &&
                                t1.isNotBlank() &&
                                t2.isNotBlank()
                            ) {
                                codes.sorted().joinToString("-")
                            } else {
                                it.id.substringBefore("-L")
                            }
                        }
                    }.values.mapNotNull { pair ->
                        if (isVolta) {
                            pair.filter { it.id.contains("-L2") }.maxByOrNull {
                                if (it.status == "FINISHED") {
                                    3
                                } else if (it.homeScore != null) {
                                    2
                                } else if (it.id.startsWith("CLI-2026")) {
                                    1
                                } else {
                                    0
                                }
                            }
                        } else {
                            pair.filter { !it.id.contains("-L2") }.maxByOrNull {
                                if (it.status == "FINISHED") {
                                    3
                                } else if (it.homeScore != null) {
                                    2
                                } else if (it.id.startsWith("CLI-2026")) {
                                    1
                                } else {
                                    0
                                }
                            }
                        }
                    }.sortedBy { it.matchOrder.takeIf { o -> o > 0 } ?: 99 }
                } else {
                    matches.filter { it.phase == selectedPhase }.sortedBy { it.matchDateMillis }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(BolaoSpacing.md)
            ) {
                if (phaseMatches.isEmpty() && selectedPhase == Phase.FRIENDLIES) {
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
                items(phaseMatches, key = { it.id }) { m ->
                    MatchCard(
                        match = m,
                        prediction = predictions[m.id],
                        isAdmin = isAdmin,
                        bolaoCreatedAt = bolaoCreatedAt,
                        forceLocked = false,
                        showSocialBadge = true,
                        allMatches = matches,
                        isTwoLegged = championship.isTwoLegged,
                        onClick = { onMatchClick(m.id) },
                        onShowAllPredictions = {
                            onShowAllPredictions(m)
                        },
                        onOpenAdminScoreDialog = {
                            onOpenAdminScoreDialog(m)
                        }
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(
                visible = showShadow,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent)
                            )
                        )
                )
            }

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

@Composable
fun KnockoutPhaseSelector(
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
                        label = "⚽️ HOJE",
                        isSelected = selectedLabel == "⚽️ HOJE",
                        isUnlocked = true,
                        onClick = { onSelect("⚽️ HOJE") }
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
