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
    "CRE" to ("Remo" to "🦁"),
    "STR" to ("The Strongest" to "🐯"),
    "TÁC" to ("Deportivo Táchira" to "🦓"),
    "ALI" to ("Alianza Lima" to "⚪"),
    "JUV" to ("Juventud" to "🟢"),
    "CAR" to ("Carabobo" to "🍷"),
    "HUA" to ("Huachipato" to "⚽"),
    "DIM" to ("Ind. Medellín" to "🔴"),
    "SPO" to ("Sporting Cristal" to "🔵"),
    "OHI" to ("O'Higgins" to "🔵"),
    "BOL" to ("Bolívar" to "🔵"),
    "ROS" to ("Rosário Central" to "🏟️"),
    "UCV" to ("Uni. Central" to "⚽"),
    "PLA" to ("Platense" to "⚽"),
    "BOC" to ("Boca Juniors" to "🟦"),
    "IDL" to ("Ind. del Valle" to "⚫"),
    "CLA" to ("Libertad" to "⚪"),
    "NAC" to ("Nacional" to "⚪"),
    "CUD" to ("Universitario" to "⚽"),
    "EST" to ("Estudiantes" to "🔴"),
    "CCP" to ("Cerro Porteño" to "🔵"),
    "LDU" to ("LDU Quito" to "⚪"),
    "LAN" to ("Lanús" to "🍷"),
    "CUS" to ("Cusco FC" to "⚽"),
    "DLG" to ("La Guaira" to "⚽"),
    "BAR" to ("Barcelona-EQU" to "🟡"),
    "PEN" to ("Peñarol" to "🟡"),
    "CAT" to ("Uni. Católica" to "🔵"),
    "COQ" to ("Coquimbo Unido" to "🟡")
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
    
    suspend fun getUpdatedMatches(currentMatches: List<Match>, championshipId: String): List<Match> {
        // Busca todos os jogos da API e mapeia para o formato interno
        return fetchAllMatchesFromApi(championshipId)
    }

    private suspend fun fetchAllMatchesFromApi(championshipId: String): List<Match> {
        val championship = com.lpstudio.bolaodagalera.domain.model.Championship.fromId(championshipId)
        val compCode = championship.apiCode.ifBlank { return emptyList() }
        
        // Buscamos a temporada atual para pegar jogos reais
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
                .mapNotNull { apiMatch ->
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
                    val stage = apiMatch.stage
                    
                    val internalPhase = when(stage) {
                        "REGULAR_SEASON", "GROUP_STAGE" -> Phase.GROUP_STAGE
                        "ROUND_1", "ROUND_2", "ROUND_3" -> Phase.ROUND_OF_32
                        "PLAY_OFFS", "ROUND_OF_16" -> Phase.ROUND_OF_16
                        "QUARTER_FINALS" -> Phase.QUARTERFINALS
                        "SEMI_FINALS" -> Phase.SEMIFINALS
                        "FINAL" -> Phase.FINAL
                        else -> Phase.GROUP_STAGE
                    }

                    // Determina a ordem e a perna (Ida/Volta) de forma genérica
                    val geLeg = if (stage.contains("LEG2") || round == 2) 2 else 1
                    val legSuffix = if (championship.isTwoLegged) "-L$geLeg" else ""
                    
                    // ID consistente baseado no código da competição e ID da API
                    val matchId = "${compCode}-M${apiMatch.id}$legSuffix"

                    val apiDateMillis = try {
                        val instant = Instant.parse(apiMatch.utcDate)
                        instant.toEpochMilliseconds()
                    } catch (e: Exception) { 
                        0L
                    }

                    val s = apiMatch.score
                    val hScore = s?.fullTime?.home ?: s?.regularTime?.home
                    val aScore = s?.fullTime?.away ?: s?.regularTime?.away
                    
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
                        phase = internalPhase,
                        group = if (round > 0) "Rodada $round" else apiMatch.stage,
                        homeScore = hScore,
                        awayScore = aScore,
                        status = apiMatch.status,
                        championshipId = championshipId,
                        matchOrder = 0,
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
    override suspend fun upsertMatch(match: Match) {}
    override suspend fun seedMatchesIfNeeded() {}
}
