package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class TeamStanding(
    val teamName: String,
    val teamCode: String,
    val teamFlag: String,
    val teamCrest: String?,
    val played: Int = 0,
    val won: Int = 0,
    val drawn: Int = 0,
    val lost: Int = 0,
    val goalsFor: Int = 0,
    val goalsAgainst: Int = 0,
    val points: Int = 0,
) {
    val goalDifference: Int get() = goalsFor - goalsAgainst
}

object StandingsCalculator {
    fun calculate(matches: List<Match>): List<TeamStanding> {
        val table = mutableMapOf<String, TeamStanding>()

        matches.filter { it.isFinished && it.homeScore != null && it.awayScore != null }.forEach { match ->
            val h = match.homeScore!!
            val a = match.awayScore!!

            // Home Team
            val currentH =
                table.getOrPut(match.homeTeamCode) {
                    TeamStanding(match.homeTeam, match.homeTeamCode, match.homeTeamFlag, match.homeTeamCrest)
                }
            table[match.homeTeamCode] =
                currentH.copy(
                    played = currentH.played + 1,
                    won = currentH.won + (if (h > a) 1 else 0),
                    drawn = currentH.drawn + (if (h == a) 1 else 0),
                    lost = currentH.lost + (if (h < a) 1 else 0),
                    goalsFor = currentH.goalsFor + h,
                    goalsAgainst = currentH.goalsAgainst + a,
                    points =
                        currentH.points + (
                            if (h > a) {
                                3
                            } else if (h == a) {
                                1
                            } else {
                                0
                            }
                        ),
                )

            // Away Team
            val currentA =
                table.getOrPut(match.awayTeamCode) {
                    TeamStanding(match.awayTeam, match.awayTeamCode, match.awayTeamFlag, match.awayTeamCrest)
                }
            table[match.awayTeamCode] =
                currentA.copy(
                    played = currentA.played + 1,
                    won = currentA.won + (if (a > h) 1 else 0),
                    drawn = currentA.drawn + (if (a == h) 1 else 0),
                    lost = currentA.lost + (if (a < h) 1 else 0),
                    goalsFor = currentA.goalsFor + a,
                    goalsAgainst = currentA.goalsAgainst + h,
                    points =
                        currentA.points + (
                            if (a > h) {
                                3
                            } else if (a == h) {
                                1
                            } else {
                                0
                            }
                        ),
                )
        }

        return table.values.sortedWith(
            compareByDescending<TeamStanding> { it.points }
                .thenByDescending { it.won }
                .thenByDescending { it.goalDifference }
                .thenByDescending { it.goalsFor },
        )
    }
}
