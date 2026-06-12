package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class FootballDataResponse(val matches: List<FdMatch> = emptyList())
@Serializable
private data class FdMatch(
    val homeTeam: FdTeam? = null,
    val awayTeam: FdTeam? = null,
    val score: FdScore? = null,
    val status: String? = null
)
@Serializable
private data class FdTeam(val tla: String? = null)
@Serializable
private data class FdScore(val fullTime: FdTimeScore? = null)
@Serializable
private data class FdTimeScore(val home: Int? = null, val away: Int? = null)

@Serializable
private data class MatchDto(
    val homeTeam: String = "", val awayTeam: String = "",
    val homeTeamCode: String = "", val awayTeamCode: String = "",
    val homeTeamFlag: String = "", val awayTeamFlag: String = "",
    val matchDateMillis: Long = 0L, val phase: String = "",
    val group: String? = null, 
    val homeScore: Int? = null, 
    val awayScore: Int? = null,
    val isManual: Boolean = false
)

private fun MatchDto.toDomain(id: String) = Match(
    id = id, homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    matchDateMillis = matchDateMillis, phase = Phase.valueOf(phase),
    group = group, homeScore = homeScore, awayScore = awayScore,
    isManual = isManual
)

private fun Match.toDto() = MatchDto(
    homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    matchDateMillis = matchDateMillis, phase = phase.name,
    group = group, homeScore = homeScore, awayScore = awayScore,
    isManual = isManual
)

class FirebaseMatchRepository : MatchRepository {

    private val db by lazy { Firebase.firestore }
    private val collection by lazy { db.collection("matches") }
    private val client = HttpClient {
        install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true; isLenient = true }) }
    }
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val FD_API_URL = "https://api.football-data.org/v4/competitions/WC/matches"
    private val API_KEY = "c366f1ec224c43a28100c97ea5aab282" 

    init {
        scope.launch {
            delay(5000)
            while (true) {
                try {
                    syncRealTimeScores()
                } catch (e: Exception) { }
                delay(300_000L) // 5 minutos
            }
        }
    }

    override fun getMatches(): Flow<List<Match>> = collection.snapshots.map { snap ->
        try {
            snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedBy { it.matchDateMillis }
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = collection
        .where { "phase" equalTo phase.name }
        .snapshots
        .map { snap ->
            try {
                snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedBy { it.matchDateMillis }
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun getMatch(matchId: String): Match {
        val doc = collection.document(matchId).get()
        return doc.data<MatchDto>().toDomain(doc.id)
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        // Quando atualizamos manualmente pelo app, setamos isManual = true
        collection.document(matchId).set(
            mapOf(
                "homeScore" to homeScore, 
                "awayScore" to awayScore,
                "isManual" to true
            ),
            merge = true
        )
    }

    override suspend fun seedMatchesIfNeeded() {
        try {
            val snapshot = collection.get()
            if (snapshot.documents.isEmpty()) {
                allMatches.forEach { m ->
                    // Ao dar seed, não enviamos scores (null) para não resetar nada
                    collection.document(m.id).set(m.toDto())
                }
            }
        } catch (e: Exception) { }
    }

    private suspend fun syncRealTimeScores() {
        try {
            val response: FootballDataResponse = client.get(FD_API_URL) {
                header("X-Auth-Token", API_KEY)
            }.body()

            response.matches.forEach { fdMatch ->
                val hScore = fdMatch.score?.fullTime?.home ?: return@forEach
                val aScore = fdMatch.score.fullTime.away ?: return@forEach
                val hCode = fdMatch.homeTeam?.tla ?: return@forEach
                val aCode = fdMatch.awayTeam?.tla ?: return@forEach

                val internalMatch = allMatches.find { 
                    (it.homeTeamCode == hCode && it.awayTeamCode == aCode) ||
                    (it.homeTeamCode == aCode && it.awayTeamCode == hCode)
                } ?: return@forEach

                val currentInDb = getMatch(internalMatch.id)
                
                // TRAVA 1: Se foi alterado manualmente pelo Admin, a API não mexe.
                if (currentInDb.isManual) return@forEach

                // TRAVA 2: A API só pode atualizar se o novo placar tiver MAIS ou IGUAL gols que o atual.
                // Isso impede que um reset para 0x0 apague gols já capturados.
                val currentTotalGols = (currentInDb.homeScore ?: 0) + (currentInDb.awayScore ?: 0)
                val apiTotalGols = hScore + aScore
                
                if (apiTotalGols < currentTotalGols) return@forEach

                // Se passou pelas travas, atualiza o placar (mas mantém isManual = false)
                collection.document(internalMatch.id).set(
                    mapOf("homeScore" to hScore, "awayScore" to aScore),
                    merge = true
                )
            }
        } catch (e: Exception) { }
    }
}
