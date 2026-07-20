package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.data.seed.allMatches
import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class MatchDto(
    val homeTeam: String = "", val awayTeam: String = "",
    val homeTeamCode: String = "", val awayTeamCode: String = "",
    val homeTeamFlag: String = "", val awayTeamFlag: String = "",
    val homeTeamCrest: String? = null, val awayTeamCrest: String? = null,
    val matchDateMillis: Long = 0L, val phase: String = "",
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
    matchDateMillis = matchDateMillis, phase = Phase.valueOf(phase),
    group = group, homeScore = homeScore, awayScore = awayScore,
    status = status, championshipId = championshipId, 
    matchOrder = matchOrder, isManual = isManual
).also { 
    if (it.homeTeamCode == "BOT" || it.homeTeamCode == "VIT") {
        println("BOLAOLOG: DTO -> Domain: ${it.homeTeamCode} ${it.homeScore}x${it.awayScore} | ID: $id")
    }
}

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
            // Se for a mesma fase de mata-mata
            // Usamos matchOrder se disponível (>0), senão tenta inferir pelo ID
            val orderA = if (a.matchOrder > 0) a.matchOrder else a.id.split("-").lastOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 99
            val orderB = if (b.matchOrder > 0) b.matchOrder else b.id.split("-").lastOrNull()?.filter { it.isDigit() }?.toIntOrNull() ?: 99

            if (orderA != orderB) {
                orderA.compareTo(orderB)
            } else {
                val dateComp = a.matchDateMillis.compareTo(b.matchDateMillis)
                if (dateComp != 0) dateComp else a.id.compareTo(b.id)
            }
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
    }.catch { emit(emptyList()) }

    override fun getMatchesByPhase(phase: Phase): Flow<List<Match>> = collection
        .where { "phase" equalTo phase.name }
        .snapshots
        .map { snap ->
            try {
                snap.documents.map { it.data<MatchDto>().toDomain(it.id) }.sortedForUi()
            } catch (e: Exception) {
                emptyList()
            }
        }.catch { emit(emptyList()) }

    override suspend fun getMatch(matchId: String): Match {
        val doc = collection.document(matchId).get()
        return doc.data<MatchDto>().toDomain(doc.id)
    }

    override suspend fun updateMatchScore(matchId: String, homeScore: Int?, awayScore: Int?, isManual: Boolean) {
        // Quando atualizamos manualmente pelo app (Admin), setamos isManual = true
        // para que o centralizador (GitHub Action) não sobrescreva este placar.
        collection.document(matchId).set(
            mapOf(
                "homeScore" to homeScore, 
                "awayScore" to awayScore,
                "isManual" to isManual
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
        status: String?,
        isManual: Boolean
    ) {
        val updates = mutableMapOf<String, Any>(
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

        collection.document(matchId).set(updates, merge = true)
    }

    override suspend fun upsertMatch(match: Match) {
        try {
            val doc = collection.document(match.id).get()
            if (doc.exists) {
                val existing = doc.data<MatchDto>()
                if (existing.isManual) return // Não sobrescreve se foi editado manualmente
            }
            collection.document(match.id).set(match.toDto(), merge = true)
        } catch (e: Exception) {
            // Se o documento não existe, o get() pode falhar dependendo da lib, 
            // mas aqui apenas tentamos o set direto como fallback.
            collection.document(match.id).set(match.toDto(), merge = true)
        }
    }

    override suspend fun seedMatchesIfNeeded() {
        try {
            val snapshot = collection.get()
            val existingDocs = snapshot.documents.associateBy { it.id }
            val localIds = allMatches.map { it.id }.toSet()

            // 1. Limpeza Segura de Duplicados (Apenas IDs numéricos puros ou KO-5XXXX)
            snapshot.documents.forEach { doc ->
                val id = doc.id
                val isWrongKoFormat = id.startsWith("KO-") && id.removePrefix("KO-").all { it.isDigit() }
                val isNumericId = id.all { it.isDigit() }

                if (!localIds.contains(id) && (isWrongKoFormat || isNumericId)) {
                    collection.document(id).delete()
                }
            }

            // 2. Sincronização Inteligente: Só cria se não existir ou se precisar atualizar Times/Bandeiras
            // NUNCA sobrescreve placar (homeScore/awayScore) ou status se já existirem no Firestore
            allMatches.forEach { m ->
                val existing = existingDocs[m.id]
                if (existing == null) {
                    collection.document(m.id).set(m.toDto())
                } else {
                    // Sincronização básica de metadados para garantir que nomes e bandeiras estejam certos
                    val updates = mutableMapOf<String, Any?>(
                        "homeTeam" to m.homeTeam,
                        "homeTeamCode" to m.homeTeamCode,
                        "homeTeamFlag" to m.homeTeamFlag,
                        "awayTeam" to m.awayTeam,
                        "awayTeamCode" to m.awayTeamCode,
                        "awayTeamFlag" to m.awayTeamFlag,
                        "matchDateMillis" to m.matchDateMillis,
                        "phase" to m.phase.name,
                        "championshipId" to m.championshipId
                    )

                    collection.document(m.id).update(updates)
                }
            }
        } catch (e: Exception) { }
    }
}
