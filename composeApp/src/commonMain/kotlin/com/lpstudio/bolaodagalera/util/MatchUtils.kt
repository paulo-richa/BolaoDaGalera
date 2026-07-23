package com.lpstudio.bolaodagalera.util

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase

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

    // Se o nome já for de um time real (não for TBD ou "Vencedor..."), usamos ele direto.
    val isPlaceholder = teamName == "TBD" || teamName.startsWith("Vencedor") || teamName.startsWith("Perdedor") || teamName.contains("/")
    if (teamName.isNotBlank() && !isPlaceholder && (teamFlag != "🏳️" || !matchId.contains("KO-"))) {
        val currentMatch = allMatches.find { it.id == matchId }
        val crest = if (isHome) currentMatch?.homeTeamCrest else currentMatch?.awayTeamCrest
        
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
    
    val id = matchId

    // 1. Determinar o ID do jogo de origem baseado na lógica sequencial
    val targetId = when {
        id.contains("QF") -> {
            val numStr = id.substringAfter("QF").substring(0, 1)
            val num = numStr.toIntOrNull() ?: 0
            val originNum = if (isHome) (num * 2 - 1) else (num * 2)
            val originMatch = allMatches.find { it.phase == Phase.ROUND_OF_16 && it.matchOrder == originNum }
            originMatch?.id
        }
        id.contains("SF") -> {
            val numStr = id.substringAfter("SF").substring(0, 1)
            val num = numStr.toIntOrNull() ?: 0
            val mapping = mapOf(1 to listOf(1, 4), 2 to listOf(2, 3))
            val originQfOrder = if (isHome) mapping[num]?.get(0) else mapping[num]?.get(1)
            val originMatch = allMatches.find { it.phase == Phase.QUARTERFINALS && it.matchOrder == originQfOrder && !it.id.contains("-L2") }
            originMatch?.id
        }
        id.contains("FINAL") -> {
            val originSfOrder = if (isHome) 1 else 2
            val originMatch = allMatches.find { it.phase == Phase.SEMIFINALS && it.matchOrder == originSfOrder && !it.id.contains("-L2") }
            originMatch?.id
        }
        else -> null
    }

    if (targetId == null) return Triple(teamName, teamFlag, null)

    // 2. Buscar o jogo de origem
    val matchSource = allMatches.find { it.id == targetId } ?: return Triple(teamName, teamFlag, null)

    // 3. Se o jogo de origem terminou, resolvemos quem passou
    if (matchSource.isFinished) {
        val hScore = matchSource.homeScore ?: 0
        val aScore = matchSource.awayScore ?: 0
        
        // Resolvemos os nomes dos times da origem recursivamente
        val homeRes = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val awayRes = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        return if (hScore > aScore) homeRes else awayRes
    }

    // 4. Se o jogo não terminou, tentamos resolver os candidatos recursivamente
    if (depth < 3) {
        val (hResName, hResFlag, _) = resolveDisplayName(matchSource.id, matchSource.homeTeam, matchSource.homeTeamFlag, allMatches, true, depth + 1)
        val (aResName, aResFlag, _) = resolveDisplayName(matchSource.id, matchSource.awayTeam, matchSource.awayTeamFlag, allMatches, false, depth + 1)

        if (hResName.isNotBlank() && aResName.isNotBlank()) {
            val name1 = hResName.split(" ").last()
            val name2 = aResName.split(" ").last()
            
            val display1 = if (hResFlag != "🏳️" && hResFlag.isNotBlank()) hResFlag else name1
            val display2 = if (aResFlag != "🏳️" && aResFlag.isNotBlank()) aResFlag else name2
            
            return Triple("", "$display1 ou $display2", null)
        }
    }

    return Triple(teamName, teamFlag.ifBlank { "🏳️" }, null)
}
