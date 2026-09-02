package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/**
 * Reduces the full list of matches for a championship to the list a pool
 * should display: filters by the pool's scope, removes "ghost" duplicates
 * that the API creates while migrating a match (same fixture with different
 * IDs), and trims rounds that were already mostly finished before the pool
 * was created.
 *
 * Extracted from BolaoViewModel's combine(): it was the app's biggest
 * historical source of bugs, and living inside the lambda made it impossible
 * to test this logic in isolation.
 */
class FilterBolaoMatchesUseCase {
    operator fun invoke(bolao: Bolao, matches: List<Match>): List<Match> {
        var filtered = filterByScope(bolao, matches)
        filtered = dedupeGhosts(filtered)
        filtered = applyRoundCutoff(bolao, filtered)
        return filtered
    }

    private fun filterByScope(bolao: Bolao, matches: List<Match>): List<Match> = matches.filter { m ->
        when {
            bolao.specificMatchId != null -> m.id == bolao.specificMatchId
            bolao.scope == BolaoScope.ONLY_GROUPS -> m.phase == Phase.GROUP_STAGE
            bolao.scope == BolaoScope.ONLY_KNOCKOUT -> m.phase != Phase.GROUP_STAGE
            else -> true
        }
    }

    private fun dedupeGhosts(matches: List<Match>): List<Match> = matches
        .groupBy {
            if (it.phase == Phase.GROUP_STAGE) {
                "${it.homeTeamCode}-${it.awayTeamCode}-${it.groupRound()}"
            } else {
                // Knockout: groups strictly by team names and phase.
                // This prevents different IDs for the same match from producing two cards.
                // While the API hasn't confirmed the teams (both TBD), several
                // different fixtures (QF1, QF2, QF3...) end up with the same
                // generic "TBD" name - in that case matchOrder is used to
                // avoid collapsing distinct fixtures into the same group.
                val teams = if (it.homeTeamCode != "TBD" && it.awayTeamCode != "TBD") {
                    listOf(it.homeTeam, it.awayTeam).sorted().joinToString(" vs ")
                } else {
                    "order-${it.matchOrder}"
                }
                val leg = if (it.id.contains("-L2")) "L2" else "L1"
                "${it.phase}-$teams-$leg"
            }
        }
        .map { (_, matchGroup) ->
            matchGroup.maxByOrNull {
                when {
                    it.status == "FINISHED" -> 3
                    it.homeScore != null -> 2
                    it.id.contains("-") -> 1
                    else -> 0
                }
            }!!
        }

    private fun applyRoundCutoff(bolao: Bolao, matches: List<Match>): List<Match> {
        val championship = Championship.fromId(bolao.championshipId)
        if (!championship.isPointsBased) return matches

        val matchesByRound = matches.groupBy { it.groupRound() }
        val lastMostlyFinishedRound =
            matchesByRound.keys
                .filter { round ->
                    val roundMatches = matchesByRound[round] ?: emptyList()
                    val finishedCount = roundMatches.count { it.matchDateMillis < bolao.createdAtMillis }
                    finishedCount > (roundMatches.size / 2)
                }
                .maxOrNull() ?: 0

        val startFromRound = lastMostlyFinishedRound + 1
        return matches.filter { it.groupRound() >= startFromRound }
    }
}
