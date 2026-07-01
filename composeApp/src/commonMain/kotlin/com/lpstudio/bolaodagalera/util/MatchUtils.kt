package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match

fun resolveDisplayName(name: String, flag: String, allMatches: List<Match>): Pair<String, String> {
    if (allMatches.isEmpty()) return name to flag
    if (!name.startsWith("Venc.") && !name.startsWith("Perd.")) return name to flag

    val targetId = when {
        name.contains("J32-") -> "KO-32-" + name.substringAfter("J32-").trim()
        name.contains("Oit. ") -> "KO-16-" + name.substringAfter("Oit. ").trim()
        name.contains("QF ") -> "KO-QF-" + name.substringAfter("QF ").trim()
        name.contains("Semi ") || name.contains("SF ") -> "KO-SF-" +
                (if (name.contains("SF ")) name.substringAfter("SF ") else name.substringAfter("Semi ")).trim()
        else -> return name to flag
    }

    val m = allMatches.find { it.id == targetId }
    
    // Se não encontrou no banco, tenta buscar no MatchSeedData como fallback
    val matchSource = m ?: com.lpstudio.bolaodagalera.data.seed.allMatches.find { it.id == targetId }
    
    if (matchSource == null) return name to flag

    // Se o jogo de origem já terminou, mostramos o vencedor
    if (matchSource.isFinished) {
        val hScore = matchSource.homeScore ?: 0
        val aScore = matchSource.awayScore ?: 0
        return if (hScore > aScore) {
            matchSource.homeTeam to matchSource.homeTeamFlag
        } else if (aScore > hScore) {
            matchSource.awayTeam to matchSource.awayTeamFlag
        } else {
            name to flag
        }
    }

    // Se o time de origem NÃO for TBD, podemos mostrar as bandeiras sem o nome
    if (matchSource.homeTeamCode != "TBD" && matchSource.awayTeamCode != "TBD") {
        val newFlag = if (matchSource.homeTeamFlag != "🏳️" && matchSource.awayTeamFlag != "🏳️") {
            "${matchSource.homeTeamFlag} ou ${matchSource.awayTeamFlag}"
        } else {
            flag
        }
        return "" to newFlag
    }
    
    return name to flag
}
