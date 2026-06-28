package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class MatchDto(
    val homeTeam: String = "", val awayTeam: String = "",
    val homeTeamCode: String = "", val awayTeamCode: String = "",
    val homeTeamFlag: String = "", val awayTeamFlag: String = "",
    val matchDateMillis: Long = 0L, val phase: String = "",
    val group: String? = null, 
    val homeScore: Int? = null, 
    val awayScore: Int? = null,
    val status: String? = null,
    val isManual: Boolean = false
)

private fun MatchDto.toDomain(id: String) = Match(
    id = id, homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    matchDateMillis = matchDateMillis, phase = Phase.valueOf(phase),
    group = group, homeScore = homeScore, awayScore = awayScore,
    status = status, isManual = isManual
)

private fun Match.toDto() = MatchDto(
    homeTeam = homeTeam, awayTeam = awayTeam,
    homeTeamCode = homeTeamCode, awayTeamCode = awayTeamCode,
    homeTeamFlag = homeTeamFlag, awayTeamFlag = awayTeamFlag,
    matchDateMillis = matchDateMillis, phase = phase.name,
    group = group, homeScore = homeScore, awayScore = awayScore,
    status = status, isManual = isManual
)

/**
 * Repositório de Jogos via Firebase Firestore.
 * Agora atua apenas como consumidor dos dados sincronizados centralizadamente
 * via GitHub Actions (sync.js).
 */
class FirebaseMatchRepository : MatchRepository {

    private val db by lazy { Firebase.firestore }
    private val collection by lazy { db.collection("matches") }

    private fun List<Match>.sortedForUi(): List<Match> = sortedWith { a, b ->
        if (a.phase == Phase.GROUP_STAGE && b.phase == Phase.GROUP_STAGE) {
            a.matchDateMillis.compareTo(b.matchDateMillis)
        } else if (a.phase != Phase.GROUP_STAGE && b.phase != Phase.GROUP_STAGE && a.phase == b.phase) {
            // Se for a mesma fase de mata-mata, mantém a ordem do ID (Bracket Order)
            val numA = a.id.split("-").lastOrNull()?.toIntOrNull() ?: 0
            val numB = b.id.split("-").lastOrNull()?.toIntOrNull() ?: 0
            if (numA != numB) numA.compareTo(numB) else a.matchDateMillis.compareTo(b.matchDateMillis)
        } else {
            // Fases diferentes: segue a ordem cronológica ou a ordem do enum Phase
            if (a.matchDateMillis != b.matchDateMillis) {
                a.matchDateMillis.compareTo(b.matchDateMillis)
            } else {
                a.phase.ordinal.compareTo(b.phase.ordinal)
            }
        }
    }

    override fun getMatches(): Flow<List<Match>> = collection.snapshots.map { snap ->
        try {
            snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedForUi()
        } catch (e: Exception) {
            emptyList()
        }
    }

    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = collection
        .where { "phase" equalTo phase.name }
        .snapshots
        .map { snap ->
            try {
                snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedForUi()
            } catch (e: Exception) {
                emptyList()
            }
        }

    override suspend fun getMatch(matchId: String): Match {
        val doc = collection.document(matchId).get()
        return doc.data<MatchDto>().toDomain(doc.id)
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int, awayScore: Int) {
        // Quando atualizamos manualmente pelo app (Admin), setamos isManual = true
        // para que o centralizador (GitHub Action) não sobrescreva este placar.
        collection.document(matchId).set(
            mapOf(
                "homeScore" to homeScore, 
                "awayScore" to awayScore,
                "isManual" to true
            ),
            merge = true
        )
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
        val updates = mutableMapOf<String, Any>(
            "homeTeam" to homeTeam,
            "homeTeamCode" to homeTeamCode,
            "homeTeamFlag" to homeTeamFlag,
            "awayTeam" to awayTeam,
            "awayTeamCode" to awayTeamCode,
            "awayTeamFlag" to awayTeamFlag,
            "isManual" to true
        )
        dateMillis?.let { updates["matchDateMillis"] = it }
        status?.let { updates["status"] = it }

        collection.document(matchId).set(updates, merge = true)
    }

    override suspend fun seedMatchesIfNeeded() {
        try {
            val snapshot = collection.get()
            if (snapshot.documents.isEmpty()) {
                allMatches.forEach { m ->
                    collection.document(m.id).set(m.toDto())
                }
            }
        } catch (e: Exception) { }
    }
}
