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

/**
 * Repositório de Jogos via Firebase Firestore.
 * Agora atua apenas como consumidor dos dados sincronizados centralizadamente
 * via GitHub Actions (sync.js).
 */
class FirebaseMatchRepository : MatchRepository {

    private val db by lazy { Firebase.firestore }
    private val collection by lazy { db.collection("matches") }

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
