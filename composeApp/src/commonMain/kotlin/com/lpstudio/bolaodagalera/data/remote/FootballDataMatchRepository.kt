package com.lpstudio.bolaodagalera.data.remote

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.datetime.*
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
    val matchday: Int? = null,
    val homeTeam: FdTeam? = null,
    val awayTeam: FdTeam? = null,
    val score: FdScore? = null
)

@Serializable
private data class FdTeam(
    val id: Int? = null,
    val name: String? = null,
    val tla: String? = null,
    val crest: String? = null
)

@Serializable
private data class FdScore(
    val duration: String? = null,
    val fullTime: FdGoals? = null,
    val regularTime: FdGoals? = null,
    val extraTime: FdGoals? = null,
    val penalties: FdGoals? = null
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
    "Ghana" to "GHA", "Gana" to "GHA",
    // Brasileirão
    "SE Palmeiras" to "PAL", "Palmeiras" to "PAL",
    "CR Flamengo" to "FLA", "Flamengo" to "FLA",
    "Corinthians" to "COR", "Sport Club Corinthians Paulista" to "COR",
    "São Paulo" to "SAO", "São Paulo FC" to "SAO",
    "Botafogo" to "BOT", "Botafogo FR" to "BOT",
    "Fluminense" to "FLU", "Fluminense FC" to "FLU",
    "Atlético Mineiro" to "CAM", "Atlético-MG" to "CAM",
    "Grêmio" to "GRE", "Grêmio FBPA" to "GRE",
    "Internacional" to "INT", "SC Internacional" to "INT",
    "Cruzeiro" to "CRU", "Cruzeiro EC" to "CRU",
    "Vasco da Gama" to "VAS", "Vasco" to "VAS",
    "Bahia" to "BAH", "EC Bahia" to "BAH",
    "Athletico Paranaense" to "CAP", "Athletico-PR" to "CAP",
    "Fortaleza" to "FOR", "Fortaleza EC" to "FOR",
    "Santos FC" to "SAN", "Santos" to "SAN",
    "EC Vitória" to "VIT", "Vitória" to "VIT",
    "RB Bragantino" to "RBB", "Bragantino" to "RBB",
    "Mirassol FC" to "MIR", "Mirassol" to "MIR",
    "Chapecoense AF" to "CHA", "Chapecoense" to "CHA",
    "Coritiba FBC" to "CFC", "Coritiba" to "CFC",
    "Clube do Remo" to "CRE", "Remo" to "CRE"
)

private val CLUB_INFO = mapOf(
    "PAL" to ("Palmeiras" to "🐷"),
    "FLA" to ("Flamengo" to "🔴"),
    "COR" to ("Corinthians" to "🦅"),
    "SAO" to ("São Paulo" to "🇾🇪"),
    "PAU" to ("São Paulo" to "🇾🇪"),
    "BOT" to ("Botafogo" to "⭐"),
    "FLU" to ("Fluminense" to "🇭🇺"),
    "CAM" to ("Atlético-MG" to "🐔"),
    "GRE" to ("Grêmio" to "🇪🇪"),
    "FBP" to ("Grêmio" to "🇪🇪"),
    "INT" to ("Internacional" to "🇦🇹"),
    "SCI" to ("Internacional" to "🇦🇹"),
    "CRU" to ("Cruzeiro" to "🦊"),
    "VAS" to ("Vasco" to "💢"),
    "BAH" to ("Bahia" to "🇳🇱"),
    "CAP" to ("Athletico-PR" to "🌪️"),
    "FOR" to ("Fortaleza" to "🦁"),
    "VIT" to ("Vitória" to "🦁"),
    "SAN" to ("Santos" to "🐳"),
    "RBB" to ("Bragantino" to "🐂"),
    "CFC" to ("Coritiba" to "🏁"),
    "CHA" to ("Chapecoense" to "🏹"),
    "MIR" to ("Mirassol" to "🟡"),
    "CRE" to ("Remo" to "🦁")
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
        install(HttpTimeout) {
            requestTimeoutMillis = 30000
            connectTimeoutMillis = 15000
            socketTimeoutMillis = 15000
        }
    }

    private val API_KEY = "***REMOVED_SECRET***"
    private val BASE_URL = "https://api.football-data.org/v4/competitions"

    private val _matches = MutableStateFlow(allMatches)

    override fun getMatches(): Flow<List<Match>> = _matches
    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = _matches.map { it.filter { m -> m.phase == phase } }
    override suspend fun getMatch(matchId: String): Match = _matches.value.first { it.id == matchId }
    
    suspend fun getUpdatedMatches(currentMatches: List<Match>, championshipId: String = "COPA_2026"): List<Match> {
        // Para a Copa do Mundo, mantemos o mapeamento conservador original (Não mexer!)
        if (championshipId == "COPA_2026") {
            return fetchAndMapUpdates(currentMatches, championshipId)
        }
        
        // Para novos campeonatos, buscamos todos os jogos da API e mapeamos
        return fetchAllMatchesFromApi(championshipId)
    }

    private suspend fun fetchAllMatchesFromApi(championshipId: String): List<Match> {
        val compCode = when(championshipId) {
            "BRASILEIRAO" -> "BSA"
            "LIBERTADORES" -> "CLI"
            else -> return emptyList()
        }
        
        // Buscamos a temporada atual (sem fixar 2026) para pegar jogos reais
        val url = "$BASE_URL/$compCode/matches"
        
        try {
            val response = client.get(url) {
                header("X-Auth-Token", API_KEY)
            }
            
            if (response.status.value != 200) {
                println("BOLAOLOG: Erro na API (${response.status.value}): ${response.bodyAsText()}")
                return emptyList()
            }

            val responseText = response.bodyAsText()
            val jsonParser = Json { ignoreUnknownKeys = true }
            val fdResponse: FdResponse = jsonParser.decodeFromString(responseText)
            
            return fdResponse.matches
                .map { apiMatch ->
                    val hName = apiMatch.homeTeam?.name ?: ""
                    val aName = apiMatch.awayTeam?.name ?: ""
                    
                    var hCode = apiMatch.homeTeam?.tla ?: NAME_TO_CODE[hName] ?: "TBD"
                    var aCode = apiMatch.awayTeam?.tla ?: NAME_TO_CODE[aName] ?: "TBD"
                    
                    // Ajuste para Coritiba
                    if (apiMatch.homeTeam?.id == 4241) hCode = "CFC"
                    if (apiMatch.awayTeam?.id == 4241) aCode = "CFC"

                    val hInfo = CLUB_INFO[hCode] ?: CODE_TO_TEAM_INFO[hCode]
                    val aInfo = CLUB_INFO[aCode] ?: CODE_TO_TEAM_INFO[aCode]

                    val round = apiMatch.matchday ?: 0
                    
                    // BUSCA DE ID ROBUSTA: Agora usamos o ID da API como fonte única de verdade
                    // para evitar duplicados e "adivinhações" por nome de time.
                    val matchId = "${compCode}-2026-R${round}-${apiMatch.id}"

                    val apiDateMillis = try {
                        val instant = Instant.parse(apiMatch.utcDate)
                        val tz = TimeZone.UTC
                        val dateTime = instant.toLocalDateTime(tz)
                        val futureDateTime = LocalDateTime(
                            dateTime.year + 2,
                            dateTime.month,
                            dateTime.dayOfMonth,
                            dateTime.hour,
                            dateTime.minute,
                            dateTime.second,
                            dateTime.nanosecond
                        )
                        futureDateTime.toInstant(tz).toEpochMilliseconds()
                    } catch (e: Exception) { 
                        try {
                            Instant.parse(apiMatch.utcDate).toEpochMilliseconds() + (31536000000L * 2)
                        } catch (e2: Exception) { 0L }
                    }

                    val s = apiMatch.score
                    // Priorizar regularTime para jogos ao vivo
                    val hScore = s?.fullTime?.home ?: s?.regularTime?.home
                    val aScore = s?.fullTime?.away ?: s?.regularTime?.away
                    
                    if (apiMatch.status == "IN_PLAY") {
                        println("BOLAOLOG: Live Match Found: $hName $hScore x $aScore $aName")
                    }

                    Match(
                        id = matchId,
                        homeTeam = hInfo?.first ?: hName,
                        homeTeamCode = hCode,
                        homeTeamFlag = hInfo?.second ?: "🏳️",
                        awayTeam = aInfo?.first ?: aName,
                        awayTeamCode = aCode,
                        awayTeamFlag = aInfo?.second ?: "🏳️",
                        homeTeamCrest = apiMatch.homeTeam?.crest,
                        awayTeamCrest = apiMatch.awayTeam?.crest,
                        matchDateMillis = apiDateMillis,
                        phase = when(apiMatch.stage) {
                            "REGULAR_SEASON" -> Phase.GROUP_STAGE
                            "GROUP_STAGE" -> Phase.GROUP_STAGE
                            "ROUND_OF_16" -> Phase.ROUND_OF_16
                            "QUARTER_FINALS" -> Phase.QUARTERFINALS
                            "SEMI_FINALS" -> Phase.SEMIFINALS
                            "FINAL" -> Phase.FINAL
                            else -> Phase.GROUP_STAGE
                        },
                        group = if (round > 0) "Rodada $round" else apiMatch.stage,
                        homeScore = hScore,
                        awayScore = aScore,
                        status = apiMatch.status,
                        championshipId = championshipId,
                        isManual = false
                    )
                }
        } catch (e: Exception) {
            println("BOLAOLOG: Falha crítica na busca para $championshipId: ${e.message}")
            return emptyList()
        }
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {}
    override suspend fun updateMatchTeams(
        matchId: String,
        homeTeam: String,
        homeTeamCode: String,
        homeTeamFlag: String,
        awayTeam: String,
        awayTeamCode: String,
        awayTeamFlag: String,
        dateMillis: Long?,
        status: String?,
        isManual: Boolean
    ) {}
    override suspend fun upsertMatch(match: Match) {
        // Implementação vazia no repositório remoto, pois ele apenas consome
    }
    override suspend fun seedMatchesIfNeeded() {}

    private suspend fun fetchAndMapUpdates(currentMatches: List<Match>, championshipId: String): List<Match> {
        val compCode = "WC" // Fixo para o legado
        val url = "$BASE_URL/$compCode/matches"
        
        val responseText = client.get(url) {
            header("X-Auth-Token", API_KEY)
        }.bodyAsText()
        
        val jsonParser = Json { 
            ignoreUnknownKeys = true 
            coerceInputValues = true
        }
        val response: FdResponse = jsonParser.decodeFromString(responseText)
        
        if (response.matches.isEmpty()) return emptyList()

        // Usamos os matches atuais (com times já preenchidos) para fazer o cruzamento
        return currentMatches.map { localMatch ->
            val apiMatch = findMatchInApi(localMatch, response.matches) ?: return@map localMatch
            
            var updated = localMatch
            
            // CÁLCULO DO PLACAR: Ignorar Pênaltis
            // No Football-Data, se o jogo vai para pênaltis, 'fullTime' inclui os gols da disputa.
            // Queremos apenas o placar até o fim da prorrogação.
            val s = apiMatch.score
            val hScore: Int?
            val aScore: Int?

            if (s?.duration == "PENALTY_SHOOTOUT") {
                hScore = (s.regularTime?.home ?: 0) + (s.extraTime?.home ?: 0)
                aScore = (s.regularTime?.away ?: 0) + (s.extraTime?.away ?: 0)
            } else {
                hScore = s?.fullTime?.home
                aScore = s?.fullTime?.away
            }
            
            // Se o jogo ainda não começou (TIMED/SCHEDULED), limpamos obrigatoriamente o placar
            // Isso evita que versões antigas do app mostrem "Em Andamento" com 0x0
            val isUpcoming = apiMatch.status == "TIMED" || apiMatch.status == "SCHEDULED"
            
            if (isUpcoming) {
                if (localMatch.homeScore != null || localMatch.awayScore != null) {
                    updated = updated.copy(homeScore = null, awayScore = null)
                }
            } else if (hScore != null && aScore != null) {
                if (hScore != localMatch.homeScore || aScore != localMatch.awayScore) {
                    updated = updated.copy(homeScore = hScore, awayScore = aScore)
                }
            }

            val apiDateMillis = try {
                kotlinx.datetime.Instant.parse(apiMatch.utcDate).toEpochMilliseconds()
            } catch (e: Exception) {
                0L
            }

            if (apiDateMillis > 0 && apiDateMillis != localMatch.matchDateMillis) {
                updated = updated.copy(matchDateMillis = apiDateMillis)
            }
            
            // Mapeamento de status especial para Prorrogação e Pênaltis
            val derivedStatus = when {
                apiMatch.status == "IN_PLAY" && apiMatch.score?.duration == "EXTRA_TIME" -> "EXTRA_TIME"
                apiMatch.status == "IN_PLAY" && apiMatch.score?.duration == "PENALTY_SHOOTOUT" -> "PENALTIES"
                apiMatch.status == "PAUSED" && apiMatch.score?.duration == "EXTRA_TIME" -> "EXTRA_TIME"
                apiMatch.status == "PAUSED" && apiMatch.score?.duration == "PENALTY_SHOOTOUT" -> "PENALTIES"
                else -> apiMatch.status
            }

            if (derivedStatus != localMatch.status) {
                updated = updated.copy(status = derivedStatus)
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
                        homeTeamCrest = apiMatch.homeTeam?.crest,
                        awayTeam = aInfo?.first ?: aName,
                        awayTeamCode = aCode,
                        awayTeamFlag = aInfo?.second ?: "🏳️",
                        awayTeamCrest = apiMatch.awayTeam?.crest
                    )
                }
            }
            
            updated
        }
    }

    private fun findMatchInApi(match: Match, apiMatches: List<FdMatch>): FdMatch? {
        // 1. Tradutor de IDs para Mata-mata (Mapeamento Seguro para IDs originais do projeto)
        val apiId = when (match.id) {
            "KO-32-1" -> 537415; "KO-32-2" -> 537416; "KO-32-3" -> 537417; "KO-32-4" -> 537418
            "KO-32-5" -> 537419; "KO-32-6" -> 537420; "KO-32-7" -> 537421; "KO-32-8" -> 537422
            "KO-32-9" -> 537423; "KO-32-10" -> 537424; "KO-32-11" -> 537425; "KO-32-12" -> 537426
            "KO-32-13" -> 537427; "KO-32-14" -> 537428; "KO-32-15" -> 537429; "KO-32-16" -> 537430
            "KO-16-1" -> 537376; "KO-16-2" -> 537375; "KO-16-3" -> 537379; "KO-16-4" -> 537380
            "KO-16-5" -> 537377; "KO-16-6" -> 537378; "KO-16-7" -> 537381; "KO-16-8" -> 537382
            "KO-QF-1" -> 537383; "KO-QF-2" -> 537384; "KO-QF-3" -> 537385; "KO-QF-4" -> 537386
            "KO-SF-1" -> 537387; "KO-SF-2" -> 537388; "KO-SF-3" -> 537389; "KO-FINAL" -> 537390
            else -> match.id.removePrefix("KO-").toIntOrNull()
        }

        if (apiId != null) {
            val found = apiMatches.find { it.id == apiId }
            if (found != null) return found
        }

        // 2. Fallback: Encontrar por Seleções
        if (match.homeTeamCode != "TBD" && match.awayTeamCode != "TBD") {
            return apiMatches.find { 
                val c1 = it.homeTeam?.tla ?: NAME_TO_CODE[it.homeTeam?.name ?: ""]
                val c2 = it.awayTeam?.tla ?: NAME_TO_CODE[it.awayTeam?.name ?: ""]
                (c1 == match.homeTeamCode && c2 == match.awayTeamCode) ||
                (c1 == match.awayTeamCode && c2 == match.homeTeamCode)
            }
        }

        return null
    }
}
