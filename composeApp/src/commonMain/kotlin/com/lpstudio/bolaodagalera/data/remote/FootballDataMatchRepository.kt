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
private data class FdResponse(
    val matches: List<FdMatch> = emptyList()
)

@Serializable
private data class FdMatch(
    val id: Int,
    val utcDate: String,
    val status: String,
    val stage: String,
    val homeTeam: FdTeam? = null,
    val awayTeam: FdTeam? = null,
    val score: FdScore? = null
)

@Serializable
private data class FdTeam(
    val name: String? = null,
    val tla: String? = null
)

@Serializable
private data class FdScore(
    val fullTime: FdGoals? = null
)

@Serializable
private data class FdGoals(
    val home: Int? = null,
    val away: Int? = null
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
    "Bosnia and Herzegovina" to "BIH", "Bosnia-Herzegovina" to "BIH", "Bosnia & Herzegovina" to "BIH", "Bósnia" to "BIH",
    "Brazil" to "BRA", "Brasil" to "BRA",
    "Morocco" to "MAR", "Marrocos" to "MAR",
    "Scotland" to "SCO", "Escócia" to "SCO",
    "Haiti" to "HAI",
    "United States" to "USA", "USA" to "USA", "EUA" to "USA",
    "Australia" to "AUS", "Austrália" to "AUS",
    "Paraguay" to "PAR", "Paraguai" to "PAR",
    "Turkey" to "TUR", "Turquia" to "TUR", "Türkiye" to "TUR",
    "Germany" to "GER", "Alemanha" to "GER",
    "Ecuador" to "ECU", "Equador" to "ECU",
    "Ivory Coast" to "CIV", "Costa do Marfim" to "CIV", "Côte d'Ivoire" to "CIV",
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
    "Cape Verde" to "CPV", "Cabo Verde" to "CPV", "Cape Verde Islands" to "CPV",
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
    "DR Congo" to "COD", "Rep. Congo" to "COD", "Congo DR" to "COD",
    "Chile" to "CHI",
    "Iceland" to "ISL", "Islândia" to "ISL",
    "England" to "ENG", "Inglaterra" to "ENG",
    "Croatia" to "CRO", "Croácia" to "CRO",
    "Panama" to "PAN", "Panamá" to "PAN",
    "Ghana" to "GHA", "Gana" to "GHA"
)

private val CODE_TO_TEAM_INFO = allMatches
    .filter { it.homeTeamCode != "TBD" }
    .flatMap { 
        listOf(it.homeTeamCode to (it.homeTeam to it.homeTeamFlag), it.awayTeamCode to (it.awayTeam to it.awayTeamFlag))
    }.toMap()

// ─────────────────────────────── Repositório ─────────────────────────────────

class FootballDataMatchRepository : MatchRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    private val API_KEY = "***REMOVED_SECRET***"
    private val API_URL = "https://api.football-data.org/v4/competitions/WC/matches"

    private val _matches = MutableStateFlow(allMatches)

    override fun getMatches(): Flow<List<Match>> = _matches
    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = _matches.map { it.filter { m -> m.phase == phase } }
    override suspend fun getMatch(matchId: String): Match = _matches.value.first { it.id == matchId }
    
    suspend fun getUpdatedMatches(): List<Match> {
        return fetchAndMapUpdates()
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {}
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
    ) {}
    override suspend fun seedMatchesIfNeeded() {}

    private suspend fun fetchAndMapUpdates(): List<Match> {
        val responseText = client.get(API_URL) {
            header("X-Auth-Token", API_KEY)
        }.bodyAsText()
        
        val jsonParser = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }
        val response: FdResponse = jsonParser.decodeFromString(responseText)
        
        if (response.matches.isEmpty()) return emptyList()

        return allMatches.map { localMatch ->
            val apiMatch = findMatchInApi(localMatch, response.matches) ?: return@map localMatch
            
            var updated = localMatch
            
            val hScore = apiMatch.score?.fullTime?.home
            val aScore = apiMatch.score?.fullTime?.away
            
            if (hScore != null && aScore != null && (hScore != localMatch.homeScore || aScore != localMatch.awayScore)) {
                updated = updated.copy(homeScore = hScore, awayScore = aScore)
            }

            val apiDateMillis = try {
                kotlinx.datetime.Instant.parse(apiMatch.utcDate).toEpochMilliseconds()
            } catch (e: Exception) {
                0L
            }

            if (apiDateMillis > 0 && apiDateMillis != localMatch.matchDateMillis) {
                updated = updated.copy(matchDateMillis = apiDateMillis)
            }
            
            if (apiMatch.status != localMatch.status) {
                updated = updated.copy(status = apiMatch.status)
            }
            
            if (localMatch.phase != Phase.GROUP_STAGE) {
                val hName = apiMatch.homeTeam?.name ?: ""
                val aName = apiMatch.awayTeam?.name ?: ""
                
                val hCode = apiMatch.homeTeam?.tla ?: NAME_TO_CODE[hName] ?: "TBD"
                val aCode = apiMatch.awayTeam?.tla ?: NAME_TO_CODE[aName] ?: "TBD"
                
                val hInfo = CODE_TO_TEAM_INFO[hCode]
                val aInfo = CODE_TO_TEAM_INFO[aCode]
                
                if (hCode != "TBD" && (hCode != localMatch.homeTeamCode || aCode != localMatch.awayTeamCode)) {
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

    private fun findMatchInApi(match: Match, apiMatches: List<FdMatch>): FdMatch? {
        if (match.phase == Phase.GROUP_STAGE) {
            return apiMatches.find { 
                val c1 = it.homeTeam?.tla ?: NAME_TO_CODE[it.homeTeam?.name ?: ""]
                val c2 = it.awayTeam?.tla ?: NAME_TO_CODE[it.awayTeam?.name ?: ""]
                (c1 == match.homeTeamCode && c2 == match.awayTeamCode) ||
                (c1 == match.awayTeamCode && c2 == match.homeTeamCode)
            }
        } else {
            val apiStage = when (match.phase) {
                Phase.ROUND_OF_32 -> "LAST_32"
                Phase.ROUND_OF_16 -> "LAST_16"
                Phase.QUARTERFINALS -> "QUARTER_FINALS"
                Phase.SEMIFINALS -> "SEMI_FINALS"
                Phase.THIRD_PLACE -> "THIRD_PLACE"
                Phase.FINAL -> "FINAL"
                else -> ""
            }
            
            val apiPhaseMatches = apiMatches.filter { it.stage == apiStage }
            
            // Priorizamos a ordem do Bracket (ID da API) para garantir o chaveamento correto
            val sortedApiMatches = apiPhaseMatches.sortedBy { it.id }
            val phaseMatchesInSeed = allMatches.filter { it.phase == match.phase }
            
            val indexInPhase = phaseMatchesInSeed.indexOfFirst { it.id == match.id }
            return sortedApiMatches.getOrNull(indexInPhase)
        }
    }
}
