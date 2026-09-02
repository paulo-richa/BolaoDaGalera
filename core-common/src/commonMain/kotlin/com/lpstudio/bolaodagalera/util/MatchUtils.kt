package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/**
 * Resolves the display name of a team in a knockout-stage match.
 * Recurses to find candidates (flags/names) in upcoming phases.
 */
fun resolveDisplayName(
    matchId: String,
    teamName: String,
    teamFlag: String,
    allMatches: List<Match>,
    isHome: Boolean,
    depth: Int = 0
): Triple<String, String, String?> {
    // If the name already belongs to a real team (not TBD or "Winner..."), use it as-is.
    // The backend is now responsible for sending the name already clean/short.
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

    // 1. Determine the source match ID based on the sequential bracket logic
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

    // 2. Look up the source match
    val matchSource = allMatches.find { it.id == targetId } ?: return Triple(teamName, teamFlag, null)

    // 3. If the source match has finished, resolve who advanced
    if (matchSource.isFinished) {
        val homeRes = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val awayRes = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        val hScore = (matchSource.homeScore ?: 0)
        val aScore = (matchSource.awayScore ?: 0)
        return if (hScore > aScore) homeRes else awayRes
    }

    // 4. If the match hasn't finished, try to resolve the candidates recursively
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
            // The app only formats the "OR" display; the names come from the backend
            val n1 = hResName.split(" ").first()
            val n2 = aResName.split(" ").first()
            return Triple("", "$n1 ou $n2", null)
        }
    }

    return Triple(teamName, teamFlag.ifBlank { "" }, null)
}
