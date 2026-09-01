package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.util.TimeSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class MatchDto(
    val homeTeam: String = "",
    val awayTeam: String = "",
    val homeTeamCode: String = "",
    val awayTeamCode: String = "",
    val homeTeamFlag: String = "",
    val awayTeamFlag: String = "",
    val homeTeamCrest: String? = null,
    val awayTeamCrest: String? = null,
    val matchDateMillis: Long = Match.NO_DATE_MILLIS,
    val phase: String = "",
    val group: String? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String? = null,
    val championshipId: String = "UNKNOWN",
    val matchOrder: Int = 0,
    val isManual: Boolean = false
)

private fun MatchDto.toDomain(id: String) = Match(
    id = id, homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    homeTeamCrest = homeTeamCrest, awayTeamCrest = awayTeamCrest,
    matchDateMillis = matchDateMillis,
    phase =
    try {
        Phase.valueOf(phase)
    } catch (e: Exception) {
        Phase.GROUP_STAGE
    },
    group = group, homeScore = homeScore, awayScore = awayScore,
    status = status, championshipId = championshipId,
    matchOrder = matchOrder, isManual = isManual
)

private fun Match.toDto() = MatchDto(
    homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    homeTeamCrest = homeTeamCrest, awayTeamCrest = awayTeamCrest,
    matchDateMillis = matchDateMillis, phase = phase.name,
    group = group, homeScore = homeScore, awayScore = awayScore,
    status = status, championshipId = championshipId,
    matchOrder = matchOrder, isManual = isManual
)

/**
 * Repositório de Jogos via Firebase Firestore.
 * Organizado por Subcollections: championships/{championshipId}/matches/{matchId}
 */
class FirebaseMatchRepository(private val crashReporter: CrashReporter) : MatchRepository {
    private val logger = appLogger("FirebaseMatchRepository")
    private val db by lazy { Firebase.firestore }

    private fun getMatchesCollection(championshipId: String) = db.collection("championships").document(championshipId).collection("matches")

    private fun List<Match>.sortedForUi(): List<Match> = sortedWith { a, b ->
        if (a.phase == Phase.GROUP_STAGE && b.phase == Phase.GROUP_STAGE) {
            a.matchDateMillis.compareTo(b.matchDateMillis)
        } else if (a.phase != Phase.GROUP_STAGE && b.phase != Phase.GROUP_STAGE && a.phase == b.phase) {
            val orderA = if (a.matchOrder > 0) a.matchOrder else a.id.split("-").lastOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 99
            val orderB = if (b.matchOrder > 0) b.matchOrder else b.id.split("-").lastOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 99

            if (orderA != orderB) {
                orderA.compareTo(orderB)
            } else {
                val dateComp = a.matchDateMillis.compareTo(b.matchDateMillis)
                if (dateComp != 0) dateComp else a.id.compareTo(b.id)
            }
        } else {
            if (a.matchDateMillis != b.matchDateMillis) {
                a.matchDateMillis.compareTo(b.matchDateMillis)
            } else {
                a.phase.ordinal.compareTo(b.phase.ordinal)
            }
        }
    }

    override fun getMatches(championshipId: String): Flow<List<Match>> = getMatchesCollection(championshipId).snapshots.map { snap ->
        try {
            snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedForUi()
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao mapear matches")
            emptyList()
        }
    }.catch { e ->
        crashReporter.recordException(e, "Erro ao observar matches de $championshipId")
        logger.e(e) { "Erro ao observar matches de $championshipId" }
        emit(emptyList())
    }

    override suspend fun getMatch(championshipId: String, matchId: String): Match {
        val doc = getMatchesCollection(championshipId).document(matchId).get()
        return doc.data<MatchDto>().toDomain(doc.id)
    }

    override suspend fun updateMatchScore(championshipId: String, matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {
        getMatchesCollection(championshipId).document(matchId).set(
            mapOf(
                "homeScore" to homeScore,
                "awayScore" to awayScore,
                "status" to "FINISHED",
                "isManual" to isManual
            ),
            merge = true
        )
    }

    override suspend fun updateMatchTeams(
        championshipId: String,
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
    ) {
        val updates =
            mutableMapOf<String, Any>(
                "homeTeam" to homeTeam,
                "homeTeamCode" to homeTeamCode,
                "homeTeamFlag" to homeTeamFlag,
                "awayTeam" to awayTeam,
                "awayTeamCode" to awayTeamCode,
                "awayTeamFlag" to awayTeamFlag,
                "isManual" to isManual
            )
        dateMillis?.let { updates["matchDateMillis"] = it }
        status?.let { updates["status"] = it }

        getMatchesCollection(championshipId).document(matchId).set(updates, merge = true)
    }

    override suspend fun upsertMatch(match: Match) {
        val collection = getMatchesCollection(match.championshipId)
        try {
            val doc = collection.document(match.id).get()
            if (doc.exists) {
                val existing = doc.data<MatchDto>()
                if (existing.isManual) return
            }
            collection.document(match.id).set(match.toDto(), merge = true)
        } catch (e: Exception) {
            collection.document(match.id).set(match.toDto(), merge = true)
        }
    }

    // Usado só para achar jogos "de hoje" (lembrete de palpite pendente na Home),
    // então basta uma janela de alguns dias em vez da história inteira de todos
    // os campeonatos - reduz leituras e o tamanho do listener em aberto.
    override fun getAllMatches(): Flow<List<Match>> {
        val now = TimeSource.nowMillis()
        val window = 3 * 24 * 3600_000L
        return db.collectionGroup("matches")
            .where { "matchDateMillis" greaterThanOrEqualTo (now - window) }
            .where { "matchDateMillis" lessThanOrEqualTo (now + window) }
            .snapshots.map { snap ->
                snap.documents.map { it.data<MatchDto>().toDomain(it.id) }
            }.catch { e ->
                crashReporter.recordException(e, "Erro no getAllMatches")
                logger.e(e) { "Erro no getAllMatches" }
                emit(emptyList())
            }
    }
}
