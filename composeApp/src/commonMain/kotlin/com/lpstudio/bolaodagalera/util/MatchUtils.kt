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
    
    // 1. Determinar o ID do jogo de origem baseado na lógica sequencial
    val targetId = when {
        matchId.startsWith("KO-16-") -> {
            val num = matchId.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            "KO-32-$originNum"
        }
        matchId.startsWith("KO-QF-") -> {
            val num = matchId.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            "KO-16-$originNum"
        }
        matchId.startsWith("KO-SF-") -> {
            val num = matchId.substringAfterLast("-").toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            "KO-QF-$originNum"
        }
        matchId == "KO-FINAL" || matchId == "KO-THIRD_PLACE" -> if (isHome) "KO-SF-1" else "KO-SF-2"
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
        
        return if (matchId == "KO-THIRD_PLACE") {
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
        } else if (f1.isNotEmpty()) {
            return "" to f1
        } else if (f2.isNotEmpty()) {
            return "" to f2
        }
    }

    // 5. Fallback final: Nome do Vencedor Genérico
    val fallbackName = when {
        targetId.contains("32-") -> "Venc. J32-${targetId.substringAfterLast("-")}"
        targetId.contains("16-") -> "Venc. Oit. ${targetId.substringAfterLast("-")}"
        targetId.contains("QF-") -> "Venc. QF ${targetId.substringAfterLast("-")}"
        targetId.contains("SF-") -> "Venc. SF ${targetId.substringAfterLast("-")}"
        else -> teamName
    }
    return fallbackName to teamFlag.ifBlank { "🏳️" }
}
