package com.lpstudio.bolaodagalera.data.remote

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.SerialName
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
// Nome em inglês (openfootball) → código de 3 letras usado no seed data

private val NAME_TO_CODE = mapOf(
    // Grupo A
    "Mexico"                        to "MEX",
    "South Africa"                  to "RSA",
    "Korea Republic"                to "KOR",
    "South Korea"                   to "KOR",
    "Czech Republic"                to "CZE",
    "Czechia"                       to "CZE",
    // Grupo B
    "Canada"                        to "CAN",
    "Switzerland"                   to "SUI",
    "Qatar"                         to "QAT",
    "Bosnia and Herzegovina"        to "BIH",
    "Bosnia & Herzegovina"          to "BIH",
    // Grupo C
    "Brazil"                        to "BRA",
    "Morocco"                       to "MAR",
    "Scotland"                      to "SCO",
    "Haiti"                         to "HAI",
    // Grupo D
    "United States"                 to "USA",
    "USA"                           to "USA",
    "Australia"                     to "AUS",
    "Paraguay"                      to "PAR",
    "Turkey"                        to "TUR",
    "Türkiye"                       to "TUR",
    // Grupo E
    "Germany"                       to "GER",
    "Ecuador"                       to "ECU",
    "Ivory Coast"                   to "CIV",
    "Côte d'Ivoire"                 to "CIV",
    "Cote d'Ivoire"                 to "CIV",
    "Curaçao"                       to "CUW",
    "Curacao"                       to "CUW",
    // Grupo F
    "Netherlands"                   to "NED",
    "Japan"                         to "JPN",
    "Tunisia"                       to "TUN",
    "Sweden"                        to "SWE",
    // Grupo G
    "Belgium"                       to "BEL",
    "Iran"                          to "IRN",
    "Egypt"                         to "EGY",
    "New Zealand"                   to "NZL",
    // Grupo H
    "Spain"                         to "ESP",
    "Uruguay"                       to "URU",
    "Saudi Arabia"                  to "KSA",
    "Cape Verde"                    to "CPV",
    // Grupo I
    "France"                        to "FRA",
    "Senegal"                       to "SEN",
    "Norway"                        to "NOR",
    "Iraq"                          to "IRQ",
    // Grupo J
    "Argentina"                     to "ARG",
    "Austria"                       to "AUT",
    "Algeria"                       to "ALG",
    "Jordan"                        to "JOR",
    // Grupo K
    "Portugal"                      to "POR",
    "Colombia"                      to "COL",
    "Uzbekistan"                    to "UZB",
    "DR Congo"                      to "COD",
    "Congo DR"                      to "COD",
    "Democratic Republic of Congo"  to "COD",
    "Congo, DR"                     to "COD",
    // Grupo L
    "England"                       to "ENG",
    "Croatia"                       to "CRO",
    "Panama"                        to "PAN",
    "Ghana"                         to "GHA",
)

// Índice estático: "HOMECODE|AWAYCODE" → Match.id  (construído uma vez)
private val CODE_PAIR_TO_ID: Map<String, String> by lazy {
    allMatches
        .filter { it.phase == Phase.GROUP_STAGE }
        .associate { "${it.homeTeamCode}|${it.awayTeamCode}" to it.id }
}

// ─────────────────────────────── Repositório ─────────────────────────────────

private const val OPENFOOTBALL_URL =
    "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json"

private const val REFRESH_INTERVAL_MS = 5 * 60 * 1000L // 5 minutos

class OpenFootballMatchRepository : MatchRepository {

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    // Estado inicial = dados do seed (fixtures completas em português + bandeiras)
    private val _matches = MutableStateFlow(allMatches)

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    init {
        scope.launch {
            while (true) {
                fetchAndApplyScores()
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    // ── Interface MatchRepository ─────────────────────────────────────────────

    override fun getMatches(): Flow<List<Match>> = _matches

    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> =
        _matches.map { it.filter { m -> m.phase == phase } }

    override suspend fun getMatch(matchId: String): Match =
        _matches.value.first { it.id == matchId }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        _matches.update { list ->
            list.map { if (it.id == matchId) it.copy(homeScore = homeScore, awayScore = awayScore) else it }
        }
    }

    override suspend fun seedMatchesIfNeeded() {
        // Dados são carregados via openfootball; nada a fazer no Firestore
    }

    // ── Lógica interna ────────────────────────────────────────────────────────

    private suspend fun fetchAndApplyScores() {
        try {
            val response: WcResponse = client.get(OPENFOOTBALL_URL).body()
            val scoreOverrides = buildScoreOverrides(response.matches)
            if (scoreOverrides.isEmpty()) return

            _matches.update { current ->
                current.map { match ->
                    scoreOverrides[match.id]?.let { (home, away) ->
                        match.copy(homeScore = home, awayScore = away)
                    } ?: match
                }
            }
        } catch (_: Exception) {
            // Falha silenciosa — app continua com dados do seed / cache anterior
        }
    }

    /**
     * Converte a lista de partidas do JSON em um mapa ID → (placarCasa, placarFora).
     * Só inclui partidas que já têm placar (score.ft não nulo).
     */
    private fun buildScoreOverrides(matches: List<WcMatch>): Map<String, Pair<Int, Int>> {
        val result = mutableMapOf<String, Pair<Int, Int>>()
        matches.forEach { m ->
            val ft = m.score?.ft?.takeIf { it.size >= 2 } ?: return@forEach
            val code1 = NAME_TO_CODE[m.team1] ?: return@forEach
            val code2 = NAME_TO_CODE[m.team2] ?: return@forEach

            // Tenta casa→visitante (ordem direta)
            val id = CODE_PAIR_TO_ID["$code1|$code2"]
                // Tenta visitante→casa (ordem invertida no JSON)
                ?: CODE_PAIR_TO_ID["$code2|$code1"]?.also {
                    result[it] = ft[1] to ft[0]
                    return@forEach
                }
                ?: return@forEach

            result[id] = ft[0] to ft[1]
        }
        return result
    }
}
