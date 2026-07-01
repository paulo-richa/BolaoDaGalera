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

    val m = allMatches.find { it.id == targetId } ?: return name to flag
    
    // Se o jogo de origem já terminou, mostramos o vencedor
    if (m.isFinished) {
        val hScore = m.homeScore ?: 0
        val aScore = m.awayScore ?: 0
        return if (hScore > aScore) {
            m.homeTeam to m.homeTeamFlag
        } else if (aScore > hScore) {
            m.awayTeam to m.awayTeamFlag
        } else {
            // Empate no tempo normal (Mata-mata precisa de um vencedor)
            // Como o DTO não tem campo de vencedor de pênaltis, mantemos o "ou" ou o nome original
            name to flag
        }
    }

    // Se o time de origem NÃO for TBD, podemos mostrar as bandeiras sem o nome
    if (m.homeTeamCode != "TBD" && m.awayTeamCode != "TBD") {
        val newFlag = if (m.homeTeamFlag != "🏳️" && m.awayTeamFlag != "🏳️") {
            "${m.homeTeamFlag} ou ${m.awayTeamFlag}"
        } else {
            flag
        }
        // Retorna nome vazio para indicar que apenas as bandeiras devem ser exibidas centralizadas
        return "" to newFlag
    }
    
    return name to flag
}
