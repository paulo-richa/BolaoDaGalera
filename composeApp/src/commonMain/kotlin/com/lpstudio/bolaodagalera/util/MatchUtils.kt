package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/**
 * Resolve o nome de exibição de um time em um jogo de mata-mata.
 * Aplica recursividade para encontrar candidatos (bandeiras/nomes) em fases futuras.
 */
fun resolveDisplayName(
    matchId: String,
    teamName: String,
    teamFlag: String,
    allMatches: List<Match>,
    isHome: Boolean,
    depth: Int = 0
): Triple<String, String, String?> {
    // Se o nome já for de um time real (não for TBD ou "Vencedor..."), usamos ele direto.
    // O Backend agora é responsável por enviar o nome já limpo/curto.
    val isPlaceholder =
        teamName == "TBD" ||
            teamName.startsWith("Vencedor") ||
            teamName.startsWith("Perdedor") ||
            teamName.contains("/") ||
            teamName.contains(" ou ")

    if (teamName.isNotBlank() && !isPlaceholder) {
        val currentMatch = allMatches.find { it.id == matchId }
        val crest = if (isHome) currentMatch?.homeTeamCrest else currentMatch?.awayTeamCrest
        return Triple(teamName, teamFlag, crest)
    }

    val id = matchId

    // 1. Determinar o ID do jogo de origem baseado na lógica sequencial
    val targetId =
        when {
            id.contains("QF") -> {
                val num = id.substringAfter("QF").takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                val originNum = if (isHome) (num * 2 - 1) else (num * 2)
                allMatches.find { it.phase == Phase.ROUND_OF_16 && it.matchOrder == originNum }?.id
            }
            id.contains("SF") -> {
                val num = id.substringAfter("SF").takeWhile { it.isDigit() }.toIntOrNull() ?: 0
                val originQfOrder = if (isHome) (num * 2 - 1) else (num * 2)
                allMatches.find { it.phase == Phase.QUARTERFINALS && it.matchOrder == originQfOrder && !it.id.contains("-L2") }?.id
            }
            id.contains("FINAL") -> {
                val originSfOrder = if (isHome) 1 else 2
                allMatches.find { it.phase == Phase.SEMIFINALS && it.matchOrder == originSfOrder && !it.id.contains("-L2") }?.id
            }
            else -> null
        }

    if (targetId == null) return Triple(teamName, teamFlag, null)

    // 2. Buscar o jogo de origem
    val matchSource = allMatches.find { it.id == targetId } ?: return Triple(teamName, teamFlag, null)

    // 3. Se o jogo de origem terminou, resolvemos quem passou
    if (matchSource.isFinished) {
        val homeRes = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val awayRes = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        val hScore = (matchSource.homeScore ?: 0)
        val aScore = (matchSource.awayScore ?: 0)
        return if (hScore > aScore) homeRes else awayRes
    }

    // 4. Se o jogo não terminou, tentamos resolver os candidatos recursivamente
    if (depth < 3) {
        val (hResName, _, _) =
            resolveDisplayName(
                matchSource.id,
                matchSource.homeTeam,
                matchSource.homeTeamFlag,
                allMatches,
                true,
                depth + 1
            )
        val (aResName, _, _) =
            resolveDisplayName(
                matchSource.id,
                matchSource.awayTeam,
                matchSource.awayTeamFlag,
                allMatches,
                false,
                depth + 1
            )

        if (hResName.isNotBlank() && aResName.isNotBlank()) {
            // O App apenas formata a exibição do "OU", os nomes vêm do backend
            val n1 = hResName.split(" ").first()
            val n2 = aResName.split(" ").first()
            return Triple("", "$n1 ou $n2", null)
        }
    }

    return Triple(teamName, teamFlag.ifBlank { "" }, null)
}
