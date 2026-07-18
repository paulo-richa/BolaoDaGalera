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
): Triple<String, String, String?> {

    // Se o nome já for de uma seleção real (não for TBD ou "Vencedor..."), usamos ele direto.
    // Para jogos de fase de grupos, aceitamos a bandeira branca como válida (pode ser erro de mapping).
    val isPlaceholder = teamName == "TBD" || teamName.startsWith("Vencedor") || teamName.startsWith("Perdedor") || teamName.contains("/")
    if (teamName.isNotBlank() && !isPlaceholder && (teamFlag != "🏳️" || !matchId.startsWith("KO-"))) {
        val currentMatch = allMatches.find { it.id == matchId }
        val crest = if (isHome) currentMatch?.homeTeamCrest else currentMatch?.awayTeamCrest
        
        // Limpeza Local de Nomes do Brasileirão
        val cleanedName = teamName
            .replace("CR Vasco da Gama", "Vasco")
            .replace("Vasco da Gama", "Vasco")
            .replace("Santos FC", "Santos")
            .replace("Botafogo FR", "Botafogo")
            .replace("SE Palmeiras", "Palmeiras")
            .replace("CR Flamengo", "Flamengo")
            .replace("SC Corinthians Paulista", "Corinthians")
            .replace("São Paulo FC", "São Paulo")
            .replace("Fluminense FC", "Fluminense")
            .replace("CA Mineiro", "Atlético-MG")
            .replace("Grêmio FBPA", "Grêmio")
            .replace("SC Internacional", "Internacional")
            .replace("Cruzeiro EC", "Cruzeiro")
            .replace("EC Vitória", "Vitória")
            .replace("Fortaleza EC", "Fortaleza")
            .replace("EC Bahia", "Bahia")
            .replace("CA Paranaense", "Athletico-PR")
            .replace("RB Bragantino", "Bragantino")
            .replace("CA Boca Juniors", "Boca Juniors")
            .replace("Club Nacional de Football", "Nacional")
            .replace("Independiente del Valle", "Ind. del Valle")
            .trim()

        return Triple(cleanedName, teamFlag, crest)
    }
    
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

    if (targetId == null) return Triple(teamName, teamFlag, null)

    // 2. Buscar o jogo de origem
    val m = allMatches.find { it.id == targetId }
    val seed = com.lpstudio.bolaodagalera.data.seed.allMatches.find { it.id == targetId }

    // Determinar se o que temos no banco é válido (não é TBD)
    val isDbValid = m != null && 
                   m.homeTeamCode != "TBD" && 
                   m.homeTeamCode.isNotBlank() && 
                   m.homeTeamFlag != "🏳️"

    val matchSource = if (isDbValid) m!! else seed ?: return Triple(teamName, teamFlag, null)

    // 3. Se o jogo de origem terminou, resolvemos quem passou ou quem perdeu
    if (matchSource.isFinished) {
        val hScore = matchSource.homeScore ?: 0
        val aScore = matchSource.awayScore ?: 0
        
        val isThirdPlace = matchId == "KO-THIRD_PLACE" || matchId == "THIRD_PLACE" || id == "SF-3"
        
        // Resolvemos os nomes dos times da origem recursivamente para garantir que não pegamos placeholders
        val homeRes = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val awayRes = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        return if (isThirdPlace) {
            if (hScore < aScore) homeRes else awayRes
        } else {
            if (hScore > aScore) homeRes else awayRes
        }
    }

    // 4. Se o jogo não terminou, tentamos resolver os candidatos recursivamente (até profundidade 3 para chegar nos grupos)
    if (depth < 3) {
        val (hResName, hResFlag, hResCrest) = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val (aResName, aResFlag, aResCrest) = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        val f1 = if (hResFlag == "🏳️" || hResFlag.isBlank()) "" else hResFlag
        val f2 = if (aResFlag == "🏳️" || aResFlag.isBlank()) "" else aResFlag

        if (f1.isNotEmpty() && f2.isNotEmpty()) {
            return Triple("", "$f1 ou $f2", null)
        }
    }

    // 5. Fallback final: Nome do Vencedor Genérico
    return Triple(getFallbackName(targetId, teamName), teamFlag.ifBlank { "🏳️" }, null)
}

private fun getFallbackName(targetId: String, teamName: String): String {
    val isThirdPlace = teamName.startsWith("Perdedor")
    return when {
        targetId.contains("32-") -> "Vencedor J32-${targetId.substringAfterLast("-")}"
        targetId.contains("16-") -> "Vencedor Oitavas ${targetId.substringAfterLast("-")}"
        targetId.contains("QF-") -> "Vencedor Quartas ${targetId.substringAfterLast("-")}"
        targetId.contains("SF-") -> if (isThirdPlace) "Perdedor Semifinal ${targetId.substringAfterLast("-")}" else "Vencedor Semifinal ${targetId.substringAfterLast("-")}"
        else -> teamName
    }
}
