package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_today_chip
import bolaodagalera.feature_bolao.generated.resources.rodada_selector_chip_round
import bolaodagalera.feature_bolao.generated.resources.rodada_selector_chip_tomorrow
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import org.jetbrains.compose.resources.stringResource

/** Index of the tab representing [selected] in the scrollable row, or -1 if it isn't visible yet. */
private fun resolveSelectedTabIndex(selected: Int, sorted: List<Int>, showHoje: Boolean, showAmanha: Boolean): Int {
    val leadingTabs = (if (showHoje) 1 else 0) + (if (showAmanha) 1 else 0)
    return when {
        selected == 0 && showHoje -> 0
        selected == TOMORROW_ROUND && showAmanha -> if (showHoje) 1 else 0
        selected > 0 -> sorted.indexOf(selected).takeIf { it != -1 }?.plus(leadingTabs) ?: -1
        else -> -1
    }
}

@Composable
fun RodadaSelector(selected: Int, unlocked: Set<Int>, showHoje: Boolean, showAmanha: Boolean, currentRound: Int, onSelect: (Int) -> Unit) {
    val sorted = remember(unlocked) { unlocked.sorted() }
    val listState = rememberLazyListState()
    LaunchedEffect(selected, sorted) {
        if (sorted.isEmpty()) return@LaunchedEffect
        val target = resolveSelectedTabIndex(selected, sorted, showHoje, showAmanha)
        if (target != -1) listState.animateScrollToItem(target)
    }
    val todayLabel = stringResource(Res.string.bolao_common_today_chip)
    val tomorrowLabel = stringResource(Res.string.rodada_selector_chip_tomorrow)
    androidx.compose.foundation.lazy.LazyRow(
        state = listState,
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(BolaoSpacing.sm),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        if (showHoje) item { FilterChip(label = todayLabel, isSelected = selected == 0, isUnlocked = true, onClick = { onSelect(0) }) }
        if (showAmanha) {
            item {
                FilterChip(
                    label = tomorrowLabel,
                    isSelected = selected == TOMORROW_ROUND,
                    isUnlocked = true,
                    onClick = { onSelect(TOMORROW_ROUND) }
                )
            }
        }
        items(sorted) { r ->
            FilterChip(
                label = stringResource(Res.string.rodada_selector_chip_round, r),
                isSelected = selected == r,
                isUnlocked = true,
                isPast = r < currentRound,
                isCurrent = r == currentRound,
                onClick = { onSelect(r) }
            )
        }
    }
}
