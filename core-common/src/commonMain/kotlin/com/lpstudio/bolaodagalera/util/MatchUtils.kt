package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

/** Caps how many bracket phases [resolveDisplayName] recurses through, to bound worst-case chains. */
private const val MAX_UNRESOLVED_BRACKET_DEPTH = 3

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

    return resolvePlaceholderDisplayName(matchId, teamName, teamFlag, allMatches, isHome, depth)
}

/** Extracts the numeric suffix following [prefix] in a bracket match id, e.g. "QF2" -> 2. */
private fun bracketMatchNumber(matchId: String, prefix: String): Int =
    matchId.substringAfter(prefix).takeWhile { it.isDigit() }.toIntOrNull() ?: 0

/** The Round-of-16 match feeding a quarterfinal's home/away slot. */
private fun qfSourceMatchId(matchId: String, allMatches: List<Match>, isHome: Boolean): String? {
    val num = bracketMatchNumber(matchId, "QF")
    val originNum = if (isHome) (num * 2 - 1) else (num * 2)
    return allMatches.find { it.phase == Phase.ROUND_OF_16 && it.matchOrder == originNum }?.id
}

/** The quarterfinal match feeding a semifinal's home/away slot. */
private fun sfSourceMatchId(matchId: String, allMatches: List<Match>, isHome: Boolean): String? {
    val num = bracketMatchNumber(matchId, "SF")
    val originQfOrder = if (isHome) (num * 2 - 1) else (num * 2)
    return allMatches.find { it.phase == Phase.QUARTERFINALS && it.matchOrder == originQfOrder && !it.id.contains("-L2") }?.id
}

/** The semifinal match feeding the final's home/away slot. */
private fun finalSourceMatchId(allMatches: List<Match>, isHome: Boolean): String? {
    val originSfOrder = if (isHome) 1 else 2
    return allMatches.find { it.phase == Phase.SEMIFINALS && it.matchOrder == originSfOrder && !it.id.contains("-L2") }?.id
}

/** Determines the source match ID feeding into [matchId]'s home/away slot, based on the sequential bracket logic. */
private fun bracketSourceMatchId(matchId: String, allMatches: List<Match>, isHome: Boolean): String? = when {
    matchId.contains("QF") -> qfSourceMatchId(matchId, allMatches, isHome)
    matchId.contains("SF") -> sfSourceMatchId(matchId, allMatches, isHome)
    matchId.contains("FINAL") -> finalSourceMatchId(allMatches, isHome)
    else -> null
}

/** Resolves who advanced out of a finished source match by comparing scores. */
private fun resolveFinishedMatchWinner(matchSource: Match, allMatches: List<Match>, depth: Int): Triple<String, String, String?> {
    val homeRes = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
    val awayRes = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

    val hScore = matchSource.homeScore ?: 0
    val aScore = matchSource.awayScore ?: 0
    return if (hScore > aScore) homeRes else awayRes
}

/** Builds the "Team A ou Team B" placeholder once both candidate names for an unfinished match are known. */
private fun resolveCandidatesOrLabel(matchSource: Match, allMatches: List<Match>, depth: Int): Triple<String, String, String?>? {
    val (hResName, _, _) =
        resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
    val (aResName, _, _) =
        resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

    if (hResName.isBlank() || aResName.isBlank()) return null

    // The app only formats the "OR" display; the names come from the backend
    val n1 = hResName.split(" ").first()
    val n2 = aResName.split(" ").first()
    return Triple("", "$n1 ou $n2", null)
}

private fun resolvePlaceholderDisplayName(
    matchId: String,
    teamName: String,
    teamFlag: String,
    allMatches: List<Match>,
    isHome: Boolean,
    depth: Int
): Triple<String, String, String?> {
    // 1. Determine the source match feeding this slot, then look it up
    val targetId = bracketSourceMatchId(matchId, allMatches, isHome)
    val matchSource = targetId?.let { id -> allMatches.find { it.id == id } }
    if (matchSource == null) return Triple(teamName, teamFlag, null)

    // 2. If the source match has finished, resolve who advanced
    if (matchSource.isFinished) return resolveFinishedMatchWinner(matchSource, allMatches, depth)

    // 3. If the match hasn't finished, try to resolve the candidates recursively
    if (depth < MAX_UNRESOLVED_BRACKET_DEPTH) {
        val orLabel = resolveCandidatesOrLabel(matchSource, allMatches, depth)
        if (orLabel != null) return orLabel
    }

    return Triple(teamName, teamFlag.ifBlank { "" }, null)
}
