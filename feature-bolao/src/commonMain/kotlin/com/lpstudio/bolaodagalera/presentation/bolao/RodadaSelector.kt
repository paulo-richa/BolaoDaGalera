package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.runtime.Composable
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.bolao_common_today_chip
import bolaodagalera.feature_bolao.generated.resources.bolao_common_yesterday_chip
import bolaodagalera.feature_bolao.generated.resources.rodada_selector_chip_round
import bolaodagalera.feature_bolao.generated.resources.rodada_selector_chip_tomorrow
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import org.jetbrains.compose.resources.stringResource

@Composable
fun RodadaSelector(
    selected: Int,
    unlocked: Set<Int>,
    showOntem: Boolean,
    showHoje: Boolean,
    showAmanha: Boolean,
    currentRound: Int,
    onSelect: (Int) -> Unit
) {
    val sorted = unlocked.sorted()
    val roundLabels = sorted.map { stringResource(Res.string.rodada_selector_chip_round, it) }
    val yesterdayLabel = stringResource(Res.string.bolao_common_yesterday_chip)
    val todayLabel = stringResource(Res.string.bolao_common_today_chip)
    val tomorrowLabel = stringResource(Res.string.rodada_selector_chip_tomorrow)

    val chips =
        buildList {
            if (showOntem) {
                add(
                    TabChipSpec(
                        key = "ontem",
                        label = yesterdayLabel,
                        isSelected = selected == YESTERDAY_ROUND,
                        isUnlocked = true,
                        selectedAccent = ErrorRed,
                        onClick = { onSelect(YESTERDAY_ROUND) }
                    )
                )
            }
            if (showHoje) {
                add(TabChipSpec(key = "hoje", label = todayLabel, isSelected = selected == 0, isUnlocked = true, onClick = { onSelect(0) }))
            }
            if (showAmanha) {
                add(
                    TabChipSpec(
                        key = "amanha",
                        label = tomorrowLabel,
                        isSelected = selected == TOMORROW_ROUND,
                        isUnlocked = true,
                        onClick = { onSelect(TOMORROW_ROUND) }
                    )
                )
            }
            sorted.forEachIndexed { i, r ->
                add(
                    TabChipSpec(
                        key = "round-$r",
                        label = roundLabels[i],
                        isSelected = selected == r,
                        isUnlocked = true,
                        isPast = r < currentRound,
                        isCurrent = r == currentRound,
                        onClick = { onSelect(r) }
                    )
                )
            }
        }

    TabSelectorRow(chips)
}
