package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match

/**
 * Resolve o nome de exibição de um time em um jogo de mata-mata.
 * Aplica recursividade para encontrar candidatos (bandeiras) em fases futuras.
 */
fun resolveDisplayName(
    matchId: String,
    teamName: String,
    teamFlag: String,
    allMatches: List<Match>,
    isHome: Boolean,
    depth: Int = 0
): Pair<String, String> {
    
    val id = matchId.removePrefix("KO-")
    val hasKo = matchId.startsWith("KO-")

    // 1. Determinar o ID do jogo de origem baseado na lógica sequencial
    val targetId = when {
        id.startsWith("16-") -> {
            val num = id.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            if (hasKo) "KO-32-$originNum" else "32-$originNum"
        }
        id.startsWith("QF-") -> {
            val num = id.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            if (hasKo) "KO-16-$originNum" else "16-$originNum"
        }
        id.startsWith("SF-") -> {
            val num = id.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            if (hasKo) "KO-QF-$originNum" else "QF-$originNum"
        }
        id == "FINAL" || id == "THIRD_PLACE" || id == "SF-3" -> {
            if (isHome) (if (hasKo) "KO-SF-1" else "SF-1") else (if (hasKo) "KO-SF-2" else "SF-2")
        }
        else -> null
    }

    if (targetId == null) return teamName to teamFlag

    // 2. Buscar o jogo de origem
    val m = allMatches.find { it.id == targetId }
    val seed = com.lpstudio.bolaodagalera.data.seed.allMatches.find { it.id == targetId }

    // Determinar se o que temos no banco é válido (não é TBD)
    val isDbValid = m != null && 
                   m.homeTeamCode != "TBD" && 
                   m.homeTeamCode.isNotBlank() && 
                   m.homeTeamFlag != "🏳️"

    val matchSource = if (isDbValid) m!! else seed ?: return teamName to teamFlag

    // 3. Se o jogo de origem terminou, mostramos o vencedor real
    if (matchSource.isFinished && matchSource.homeTeamCode != "TBD" && matchSource.homeTeamCode.isNotBlank()) {
        val hScore = matchSource.homeScore ?: 0
        val aScore = matchSource.awayScore ?: 0
        
        val isThirdPlace = matchId == "KO-THIRD_PLACE" || matchId == "THIRD_PLACE" || id == "SF-3"
        return if (isThirdPlace) {
            if (hScore < aScore) matchSource.homeTeam to matchSource.homeTeamFlag
            else matchSource.awayTeam to matchSource.awayTeamFlag
        } else {
            if (hScore > aScore) matchSource.homeTeam to matchSource.homeTeamFlag
            else matchSource.awayTeam to matchSource.awayTeamFlag
        }
    }

    // 4. Se o jogo não terminou, tentamos resolver os candidatos recursivamente (até profundidade 1)
    if (depth < 1) {
        val (hResName, hResFlag) = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val (aResName, aResFlag) = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        val f1 = if (hResFlag == "🏳️" || hResFlag.isBlank()) "" else hResFlag
        val f2 = if (aResFlag == "🏳️" || aResFlag.isBlank()) "" else aResFlag

        if (f1.isNotEmpty() && f2.isNotEmpty()) {
            return "" to "$f1 ou $f2"
        }
    }

    // 5. Fallback final: Nome do Vencedor Genérico
    return getFallbackName(targetId, teamName) to teamFlag.ifBlank { "🏳️" }
}

private fun getFallbackName(targetId: String, teamName: String): String {
    return when {
        targetId.contains("32-") -> "Vencedor J32-${targetId.substringAfterLast("-")}"
        targetId.contains("16-") -> "Vencedor Oitavas ${targetId.substringAfterLast("-")}"
        targetId.contains("QF-") -> "Vencedor Quartas ${targetId.substringAfterLast("-")}"
        targetId.contains("SF-") -> "Vencedor Semifinal ${targetId.substringAfterLast("-")}"
        else -> teamName
    }
}
