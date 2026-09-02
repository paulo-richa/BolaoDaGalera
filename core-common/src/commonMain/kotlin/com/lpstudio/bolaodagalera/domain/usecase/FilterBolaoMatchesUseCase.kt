package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/**
 * Reduz a lista completa de jogos de um campeonato à lista que o bolão deve
 * exibir: filtra pelo escopo do bolão, remove duplicatas "fantasma" que a API
 * cria durante a migração de um jogo (mesmo confronto com IDs diferentes) e
 * corta as rodadas já quase todas encerradas antes do bolão ser criado.
 *
 * Extraído do combine() de BolaoViewModel: é a maior fonte histórica de bug
 * do app, e viver dentro do lambda impedia testar essa lógica isoladamente.
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
                // Mata-mata: agrupa estritamente pelos nomes dos times e fase.
                // Isso impede que IDs diferentes do mesmo jogo gerem dois cards.
                // Enquanto a API não confirma os times (ambos TBD), vários
                // confrontos diferentes (QF1, QF2, QF3...) ficam com o mesmo
                // nome genérico "A definir" - nesse caso usa matchOrder para
                // não colapsar confrontos distintos no mesmo grupo.
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
