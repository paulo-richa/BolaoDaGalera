package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match

fun resolveDisplayName(name: String, flag: String, allMatches: List<Match>): Pair<String, String> {
    if (allMatches.isEmpty()) return name to flag
    if (!name.startsWith("Venc.") && !name.startsWith("Perd.")) return name to flag

    val targetId = when {
        name.contains("J32-") -> "KO-32-" + name.substringAfter("J32-").trim()
        name.contains("Oit. ") -> "KO-16-" + name.substringAfter("Oit. ").trim()
        name.contains("QF ") -> "KO-QF-" + name.substringAfter("QF ").trim()
        name.contains("SF ") -> "KO-SF-" + name.substringAfter("SF ").trim()
        else -> return name to flag
    }

    val m = allMatches.find { it.id == targetId } ?: return name to flag
    
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
