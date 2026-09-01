package com.lpstudio.bolaodagalera.presentation.bolao

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.sp
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.StandingsCalculator
import com.lpstudio.bolaodagalera.presentation.theme.ErrorRed
import com.lpstudio.bolaodagalera.presentation.theme.GlassBorder
import com.lpstudio.bolaodagalera.presentation.theme.Gold
import com.lpstudio.bolaodagalera.presentation.theme.NavyCard
import com.lpstudio.bolaodagalera.presentation.theme.Neon
import com.lpstudio.bolaodagalera.presentation.theme.TextMuted
import com.lpstudio.bolaodagalera.util.resolveDisplayName

@Composable
fun StandingsTab(matches: List<Match>) {
    val standings = remember(matches) { StandingsCalculator.calculate(matches) }
    if (standings.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Aguardando início dos jogos...", color = TextMuted) }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#", modifier = Modifier.width(24.dp), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Text("TIME", modifier = Modifier.weight(1f), fontSize = 11.sp, color = TextMuted, fontWeight = FontWeight.Bold)
                    Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf("P", "J", "V", "SG").forEach {
                            Text(
                                it,
                                modifier = Modifier.width(35.dp),
                                fontSize = 11.sp,
                                color = TextMuted,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
            items(standings.size) { index ->
                val team = standings[index]
                val (name, flag, crest) =
                    remember(team.teamName, team.teamFlag, team.teamCrest, matches) {
                        resolveDisplayName("", team.teamName, team.teamFlag, matches, true)
                    }
                val isG4 = index < 4
                val isG5 = index == 4
                val isZ4 = index >= standings.size - 4 && standings.size > 5
                val accentColor =
                    when {
                        isG4 -> Neon
                        isG5 -> Gold
                        isZ4 -> ErrorRed
                        else -> null
                    }
                Surface(
                    color =
                    when {
                        isG4 -> Neon.copy(alpha = 0.05f)
                        isG5 -> Gold.copy(alpha = 0.05f)
                        isZ4 -> ErrorRed.copy(alpha = 0.05f)
                        else -> NavyCard
                    },
                    shape = RoundedCornerShape(12.dp),
                    border =
                    androidx.compose.foundation.BorderStroke(
                        1.dp,
                        when {
                            isG4 -> Neon.copy(alpha = 0.2f)
                            isG5 -> Gold.copy(alpha = 0.2f)
                            isZ4 -> ErrorRed.copy(alpha = 0.2f)
                            else -> GlassBorder
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "${index + 1}",
                            modifier = Modifier.width(24.dp),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = accentColor ?: TextMuted
                        )
                        TeamIcon(crestUrl = crest ?: team.teamCrest, flag = AnnotatedString(flag), isTbd = false, size = 24.dp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            name,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            maxLines = 1
                        )
                        Row(modifier = Modifier.width(140.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                "${team.points}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor ?: Color.White,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.played}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.won}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
                                color = TextMuted,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "${team.goalDifference}",
                                modifier = Modifier.width(35.dp),
                                fontSize = 12.sp,
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
                }
            }
        }
    }
}
