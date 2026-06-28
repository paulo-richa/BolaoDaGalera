package com.lpstudio.bolaodagalera.data.remote

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// ──────────────────────────────── DTOs ───────────────────────────────────────

@Serializable
private data class WcResponse(
    val name: String = "",
    val matches: List<WcMatch> = emptyList()
)

@Serializable
private data class WcMatch(
    val round: String = "",
    val date: String = "",
    val team1: String = "",
    val team2: String = "",
    val group: String? = null,
    val score: WcScore? = null
)

@Serializable
private data class WcScore(
    val ft: List<Int>? = null   // [homeGoals, awayGoals]
)

// ──────────────────────────── Mapeamento de nomes ────────────────────────────

private val NAME_TO_CODE = mapOf(
    "Mexico" to "MEX", "México" to "MEX",
    "South Africa" to "RSA", "África do Sul" to "RSA",
    "Korea Republic" to "KOR", "South Korea" to "KOR", "Coreia do Sul" to "KOR",
    "Czech Republic" to "CZE", "Czechia" to "CZE", "República Tcheca" to "CZE", "Rep. Tcheca" to "CZE",
    "Canada" to "CAN", "Canadá" to "CAN",
    "Switzerland" to "SUI", "Suíça" to "SUI",
    "Qatar" to "QAT", "Catar" to "QAT",
    "Bosnia and Herzegovina" to "BIH", "Bosnia & Herzegovina" to "BIH", "Bósnia" to "BIH",
    "Brazil" to "BRA", "Brasil" to "BRA",
    "Morocco" to "MAR", "Marrocos" to "MAR",
    "Scotland" to "SCO", "Escócia" to "SCO",
    "Haiti" to "HAI",
    "United States" to "USA", "USA" to "USA", "EUA" to "USA",
    "Australia" to "AUS", "Austrália" to "AUS",
    "Paraguay" to "PAR", "Paraguai" to "PAR",
    "Turkey" to "TUR", "Turquia" to "TUR",
    "Germany" to "GER", "Alemanha" to "GER",
    "Ecuador" to "ECU", "Equador" to "ECU",
    "Ivory Coast" to "CIV", "Costa do Marfim" to "CIV",
    "Curaçao" to "CUW",
    "Netherlands" to "NED", "Holanda" to "NED",
    "Japan" to "JPN", "Japão" to "JPN",
    "Tunisia" to "TUN", "Tunísia" to "TUN",
    "Sweden" to "SWE", "Suécia" to "SWE",
    "Belgium" to "BEL", "Bélgica" to "BEL",
    "Egypt" to "EGY", "Egito" to "EGY",
    "Iran" to "IRN", "Irã" to "IRN",
    "New Zealand" to "NZL", "Nova Zelândia" to "NZL",
    "Spain" to "ESP", "Espanha" to "ESP",
    "Uruguay" to "URU", "Uruguai" to "URU",
    "Saudi Arabia" to "KSA", "Arábia Saudita" to "KSA",
    "Cape Verde" to "CPV", "Cabo Verde" to "CPV",
    "France" to "FRA", "França" to "FRA",
    "Senegal" to "SEN",
    "Norway" to "NOR", "Noruega" to "NOR",
    "Iraq" to "IRQ", "Iraque" to "IRQ",
    "Argentina" to "ARG",
    "Austria" to "AUT", "Áustria" to "AUT",
    "Algeria" to "ALG", "Argélia" to "ALG",
    "Jordan" to "JOR", "Jordânia" to "JOR",
    "Portugal" to "POR",
    "Colombia" to "COL", "Colômbia" to "COL",
    "Uzbekistan" to "UZB", "Uzbequistão" to "UZB",
    "DR Congo" to "COD", "Rep. Congo" to "COD",
    "Chile" to "CHI",
    "Iceland" to "ISL", "Islândia" to "ISL",
    "England" to "ENG", "Inglaterra" to "ENG",
    "Croatia" to "CRO", "Croácia" to "CRO",
    "Panama" to "PAN", "Panamá" to "PAN",
    "Ghana" to "GHA", "Gana" to "GHA"
)

// Mapeamento reverso para pegar nomes em PT e bandeiras (apenas times reais)
private val CODE_TO_TEAM_INFO = allMatches
    .filter { it.homeTeamCode != "TBD" }
    .flatMap { 
        listOf(it.homeTeamCode to (it.homeTeam to it.homeTeamFlag), it.awayTeamCode to (it.awayTeam to it.awayTeamFlag))
    }.toMap()

// ─────────────────────────────── Repositório ─────────────────────────────────

class OpenFootballMatchRepository : MatchRepository {

    private val client = HttpClient()

    private val _matches = MutableStateFlow(allMatches)
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        scope.launch {
            delay(1000)
            while (true) {
                fetchAndApplyUpdates()
                delay(5 * 60 * 1000L)
            }
        }
    }

    override fun getMatches(): Flow<List<Match>> = _matches
    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = _matches.map { it.filter { m -> m.phase == phase } }
    override suspend fun getMatch(matchId: String): Match = _matches.value.first { it.id == matchId }
    
    suspend fun getUpdatedMatches(): List<Match> {
        fetchAndApplyUpdates()
        return _matches.value
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        _matches.update { list ->
            list.map { if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore, isManual = true) else it }
        }
    }

    override suspend fun updateMatchTeams(
        matchId: String,
        homeTeam: String,
        homeTeamCode: String,
        homeTeamFlag: String,
        awayTeam: String,
        awayTeamCode: String,
        awayTeamFlag: String,
        dateMillis: Long?,
        status: String?
    ) {
        _matches.update { list ->
            list.map { 
                if (it.id == matchId) it.copy(
                    homeTeam = homeTeam,
                    homeTeamCode = homeTeamCode,
                    homeTeamFlag = homeTeamFlag,
                    awayTeam = awayTeam,
                    awayTeamCode = awayTeamCode,
                    awayTeamFlag = awayTeamFlag,
                    matchDateMillis = dateMillis ?: it.matchDateMillis,
                    status = status ?: it.status,
                    isManual = true
                ) else it 
            }
        }
    }

    override suspend fun seedMatchesIfNeeded() {}

    private suspend fun fetchAndApplyUpdates() {
        try {
            val url = "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json"
            val responseText = client.get(url).bodyAsText()
            val response: WcResponse = Json { ignoreUnknownKeys = true }.decodeFromString(responseText)
            
            if (response.matches.isEmpty()) return

            _matches.update { current ->
                current.map { match ->
                    if (match.isManual) return@map match
                    
                    val apiMatch = findMatchInApi(match, response.matches) ?: return@map match
                    
                    var updated = match
                    
                    // 1. Atualizar Placar
                    apiMatch.score?.ft?.let { ft ->
                        if (ft.size >= 2) {
                            updated = updated.copy(homeScore = ft[0], awayScore = ft[1])
                        }
                    }
                    
                    // 2. Atualizar Times (Mata-mata)
                    if (match.phase != Phase.GROUP_STAGE) {
                        val hName = apiMatch.team1.trim()
                        val aName = apiMatch.team2.trim()
                        
                        val hCode = NAME_TO_CODE[hName] ?: "TBD"
                        val aCode = NAME_TO_CODE[aName] ?: "TBD"
                        
                        val hInfo = CODE_TO_TEAM_INFO[hCode]
                        val aInfo = CODE_TO_TEAM_INFO[aCode]
                        
                        // Atualiza se houver qualquer diferença nos nomes ou códigos
                        if (hName != match.homeTeam || aName != match.awayTeam || hCode != match.homeTeamCode || aCode != match.awayTeamCode) {
                            updated = updated.copy(
                                homeTeam = hInfo?.first ?: hName,
                                homeTeamCode = hCode,
                                homeTeamFlag = hInfo?.second ?: "🏳️",
                                awayTeam = aInfo?.first ?: aName,
                                awayTeamCode = aCode,
                                awayTeamFlag = aInfo?.second ?: "🏳️"
                            )
                        }
                    }
                    
                    updated
                }
            }
        } catch (e: Exception) {
            // Silencioso
        }
    }

    private fun findMatchInApi(match: Match, apiMatches: List<WcMatch>): WcMatch? {
        if (match.phase == Phase.GROUP_STAGE) {
            return apiMatches.find { 
                val c1 = NAME_TO_CODE[it.team1.trim()]
                val c2 = NAME_TO_CODE[it.team2.trim()]
                (c1 == match.homeTeamCode && c2 == match.awayTeamCode) ||
                (c1 == match.awayTeamCode && c2 == match.homeTeamCode)
            }
        } else {
            val apiRoundName = when (match.phase) {
                Phase.ROUND_OF_32 -> "Round of 32"
                Phase.ROUND_OF_16 -> "Round of 16"
                Phase.QUARTERFINALS -> "Quarter-final"
                Phase.SEMIFINALS -> "Semi-final"
                Phase.THIRD_PLACE -> "Match for third place"
                Phase.FINAL -> "Final"
                else -> ""
            }
            
            val apiPhaseMatches = apiMatches.filter { 
                it.round.contains(apiRoundName, ignoreCase = true) 
            }
            
            val phaseMatchesInSeed = allMatches.filter { it.phase == match.phase }
                .sortedBy { it.matchDateMillis }
            
            val indexInPhase = phaseMatchesInSeed.indexOfFirst { it.id == match.id }
            
            return apiPhaseMatches.getOrNull(indexInPhase)
        }
    }
}
