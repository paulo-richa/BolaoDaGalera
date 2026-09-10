package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import bolaodagalera.feature_bolao.generated.resources.Res
import bolaodagalera.feature_bolao.generated.resources.standings_tab_empty_message
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_goal_diff
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_played
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_points
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_position
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_team
import bolaodagalera.feature_bolao.generated.resources.standings_tab_header_won
import com.lpstudio.bolaodagalera.designsystem.components.BolaoSurface
import com.lpstudio.bolaodagalera.designsystem.components.BolaoText
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoRadiusShape
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoSpacing
import com.lpstudio.bolaodagalera.designsystem.theme.BolaoTypography
import com.lpstudio.bolaodagalera.designsystem.theme.ErrorRed
import com.lpstudio.bolaodagalera.designsystem.theme.GlassBorder
import com.lpstudio.bolaodagalera.designsystem.theme.Gold
import com.lpstudio.bolaodagalera.designsystem.theme.NavyCard
import com.lpstudio.bolaodagalera.designsystem.theme.Neon
import com.lpstudio.bolaodagalera.designsystem.theme.TextMuted
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.StandingsCalculator
import com.lpstudio.bolaodagalera.domain.model.TeamStanding
import com.lpstudio.bolaodagalera.util.resolveDisplayName
import org.jetbrains.compose.resources.stringResource

@Composable
fun StandingsTab(matches: List<Match>, championship: Championship) {
    val standings = remember(matches) { StandingsCalculator.calculate(matches) }
    if (standings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            BolaoText(stringResource(Res.string.standings_tab_empty_message), color = TextMuted)
        }
    } else {
        val headerPoints = stringResource(Res.string.standings_tab_header_points)
        val headerPlayed = stringResource(Res.string.standings_tab_header_played)
        val headerWon = stringResource(Res.string.standings_tab_header_won)
        val headerGoalDiff = stringResource(Res.string.standings_tab_header_goal_diff)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(BolaoSpacing.xs)
        ) {
            item {
                StandingsHeaderRow(
                    headerPoints = headerPoints,
                    headerPlayed = headerPlayed,
                    headerWon = headerWon,
                    headerGoalDiff = headerGoalDiff
                )
            }
            items(standings.size) { index ->
                StandingsRow(index = index, standings = standings, matches = matches, championship = championship)
            }
        }
    }
}

@Composable
private fun StandingsHeaderRow(headerPoints: String, headerPlayed: String, headerWon: String, headerGoalDiff: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = BolaoSpacing.sm, vertical = BolaoSpacing.sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BolaoText(
            stringResource(Res.string.standings_tab_header_position),
            modifier = Modifier.width(24.dp),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        BolaoText(
            stringResource(Res.string.standings_tab_header_team),
            modifier = Modifier.weight(1f),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            fontWeight = FontWeight.Bold
        )
        Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf(headerPoints, headerPlayed, headerWon, headerGoalDiff).forEach {
                BolaoText(
                    it,
                    modifier = Modifier.width(35.dp),
                    fontSize = BolaoTypography.bodyMedium.fontSize,
                    color = TextMuted,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/** Which colored zone (if any) a table position falls into, per the championship's configured zone sizes. */
private data class StandingsZone(val isGreen: Boolean, val isYellow: Boolean, val isRed: Boolean) {
    val accentColor: Color?
        get() = when {
            isGreen -> Neon
            isYellow -> Gold
            isRed -> ErrorRed
            else -> null
        }
    val backgroundColor: Color
        get() = accentColor?.copy(alpha = 0.05f) ?: NavyCard
    val borderColor: Color
        get() = accentColor?.copy(alpha = 0.2f) ?: GlassBorder
}

private fun standingsZoneFor(position: Int, tableSize: Int, championship: Championship): StandingsZone {
    val isGreen = championship.standingsGreenZoneCount > 0 && position <= championship.standingsGreenZoneCount
    val isYellow =
        championship.standingsYellowZoneCount > 0 &&
            position > championship.standingsGreenZoneCount &&
            position <= championship.standingsGreenZoneCount + championship.standingsYellowZoneCount
    val isRed = championship.standingsRedZoneCount > 0 && position > tableSize - championship.standingsRedZoneCount
    return StandingsZone(isGreen, isYellow, isRed)
}

@Composable
private fun StandingsRow(index: Int, standings: List<TeamStanding>, matches: List<Match>, championship: Championship) {
    val team = standings[index]
    val (name, flag, crest) =
        remember(team.teamName, team.teamFlag, team.teamCrest, matches) {
            resolveDisplayName("", team.teamName, team.teamFlag, matches, true)
        }
    val zone = standingsZoneFor(index + 1, standings.size, championship)
    val accentColor = zone.accentColor

    BolaoSurface(
        color = zone.backgroundColor,
        shape = BolaoRadiusShape.md,
        border = androidx.compose.foundation.BorderStroke(1.dp, zone.borderColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = BolaoSpacing.md, vertical = BolaoSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BolaoText(
                "${index + 1}",
                modifier = Modifier.width(24.dp),
                fontSize = BolaoTypography.bodyMedium.fontSize,
                fontWeight = FontWeight.Bold,
                color = accentColor ?: TextMuted
            )
            TeamIcon(crestUrl = crest ?: team.teamCrest, flag = AnnotatedString(flag), isTbd = false, size = 24.dp)
            Spacer(Modifier.width(10.dp))
            BolaoText(
                name,
                modifier = Modifier.weight(1f),
                fontSize = BolaoTypography.bodyLarge.fontSize,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1
            )
            StandingsRowStats(team = team, accentColor = accentColor)
        }
    }
}

@Composable
private fun StandingsRowStats(team: TeamStanding, accentColor: Color?) {
    Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        BolaoText(
            "${team.points}",
            modifier = Modifier.width(35.dp),
            fontSize = BolaoTypography.bodyLarge.fontSize,
            fontWeight = FontWeight.Black,
            color = accentColor ?: Color.White,
            textAlign = TextAlign.Center
        )
        BolaoText(
            "${team.played}",
            modifier = Modifier.width(35.dp),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        BolaoText(
            "${team.won}",
            modifier = Modifier.width(35.dp),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color = TextMuted,
            textAlign = TextAlign.Center
        )
        BolaoText(
            "${team.goalDifference}",
            modifier = Modifier.width(35.dp),
            fontSize = BolaoTypography.bodyMedium.fontSize,
            color =
            if (team.goalDifference > 0) {
                Neon
            } else if (team.goalDifference < 0) {
                ErrorRed
            } else {
                TextMuted
            },
            textAlign = TextAlign.Center
        )
    }
}
