package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.appLogger
import com.lpstudio.bolaodagalera.util.TimeSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.FieldValue
import dev.gitlive.firebase.firestore.firestore
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Serializable
private data class BolaoDto(
    val name: String = "",
    val description: String = "",
    val pointsExactScore: Int = 3,
    val pointsWinnerOrDraw: Int = 1,
    val code: String = "",
    val ownerId: String = "",
    val participants: List<String> = emptyList(),
    val pendingParticipants: List<String> = emptyList(),
    val pendingExits: List<String> = emptyList(),
    val championshipId: String = "UNKNOWN",
    val scope: String = "FULL",
    val specificMatchId: String? = null,
    val createdAtMillis: Long = 0L,
    val deletedAtMillis: Long? = null
)

private fun BolaoDto.toDomain(id: String) = Bolao(
    id = id,
    name = name,
    description = description,
    pointsExactScore = pointsExactScore,
    pointsWinnerOrDraw = pointsWinnerOrDraw,
    code = code,
    ownerId = ownerId,
    participants = participants,
    pendingParticipants = pendingParticipants,
    pendingExits = pendingExits,
    championshipId = championshipId,
    scope =
    try {
        BolaoScope.valueOf(scope)
    } catch (e: Exception) {
        BolaoScope.FULL
    },
    specificMatchId = specificMatchId,
    createdAtMillis = createdAtMillis,
    deletedAtMillis = deletedAtMillis
)

class FirebaseBolaoRepository(private val crashReporter: CrashReporter) : BolaoRepository {
    private val logger = appLogger("FirebaseBolaoRepository")
    private val db = Firebase.firestore
    private val collection = db.collection("boloes")

    override fun getUserBoloes(userId: String): Flow<List<Bolao>> = try {
        collection
            .where { "participants" contains userId as Any }
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .map { doc -> doc.data<BolaoDto>().toDomain(doc.id) }
                    .filter { it.deletedAtMillis == null } // Hide bolões marked for deletion
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro ao observar bolões")
                logger.e(e) { "Erro ao observar bolões" }
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar bolões")
        logger.e(e) { "Erro crítico ao observar bolões" }
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override fun getBolaoFlow(bolaoId: String): Flow<Bolao> = try {
        collection.document(bolaoId).snapshots.map { doc ->
            if (doc.exists) {
                doc.data<BolaoDto>().toDomain(doc.id)
            } else {
                Bolao() // Return an empty object instead of crashing if deleted
            }
        }.catch { e ->
            crashReporter.recordException(e, "Erro ao observar bolão $bolaoId")
            logger.e(e) { "Erro ao observar bolão $bolaoId" }
            emit(Bolao())
        }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar bolão $bolaoId")
        logger.e(e) { "Erro crítico ao observar bolão $bolaoId" }
        kotlinx.coroutines.flow.flowOf(Bolao())
    }

    override suspend fun getBolao(bolaoId: String): Bolao {
        val doc = collection.document(bolaoId).get()
        return if (doc.exists) {
            doc.data<BolaoDto>().toDomain(doc.id)
        } else {
            Bolao()
        }
    }

    override suspend fun createBolao(
        name: String,
        description: String,
        ownerId: String,
        championshipId: String,
        scope: BolaoScope,
        specificMatchId: String?,
        pointsExactScore: Int,
        pointsWinnerOrDraw: Int
    ): Bolao {
        val code = generateCode()
        val dto =
            BolaoDto(
                name = name,
                description = description,
                pointsExactScore = pointsExactScore,
                pointsWinnerOrDraw = pointsWinnerOrDraw,
                code = code,
                ownerId = ownerId,
                participants = listOf(ownerId),
                championshipId = championshipId,
                scope = scope.name,
                specificMatchId = specificMatchId,
                createdAtMillis = TimeSource.nowMillis()
            )
        val ref = collection.add(dto)
        return dto.toDomain(ref.id)
    }

    override suspend fun requestLeaveBolao(bolaoId: String, userId: String) {
        collection.document(bolaoId).update("pendingExits" to FieldValue.arrayUnion(userId))
    }

    override suspend fun approveLeaveRequest(bolaoId: String, userId: String, approve: Boolean) {
        if (approve) {
            collection.document(bolaoId).update(
                "participants" to FieldValue.arrayRemove(userId),
                "pendingExits" to FieldValue.arrayRemove(userId)
            )
        } else {
            collection.document(bolaoId).update("pendingExits" to FieldValue.arrayRemove(userId))
        }
    }

    override suspend fun joinBolao(code: String, userId: String): Bolao {
        val snapshot = collection.where { "code" equalTo code.uppercase() }.get()
        if (snapshot.documents.isEmpty()) error("Bolão não encontrado com o código $code")

        val doc = snapshot.documents.first()
        val bolao = doc.data<BolaoDto>().toDomain(doc.id)

        // 2. Ensure the user exists in the 'users' collection so they appear in the ranking
        try {
            val userRef = db.collection("users").document(userId)
            val userDoc = userRef.get()
            if (!userDoc.exists) {
                userRef.set(
                    mapOf(
                        "name" to "Novo Usuário",
                        "email" to "",
                        "username" to "user_${userId.take(PLACEHOLDER_USERNAME_ID_LENGTH)}",
                        "createdAt" to TimeSource.nowMillis()
                    ),
                    merge = true
                )
            }
        } catch (ignored: Exception) {
            // Best-effort: a missing user document only affects display data, not the join itself.
        }

        // 3. Add directly to the participants
        if (userId !in bolao.participants) {
            val updatedParticipants = bolao.participants + userId
            val updatedPending = bolao.pendingParticipants - userId

            collection.document(bolao.id).update(
                "participants" to updatedParticipants,
                "pendingParticipants" to updatedPending
            )

            // Initialize the ranking entry so the user appears in the list immediately
            initializeRankingEntry(bolao.id, userId)

            return bolao.copy(participants = updatedParticipants, pendingParticipants = updatedPending)
        }

        return bolao
    }

    override suspend fun requestJoinBolao(code: String, userId: String): Bolao {
        val snapshot = collection.where { "code" equalTo code.uppercase() }.get()
        if (snapshot.documents.isEmpty()) error("Bolão não encontrado com o código $code")

        val doc = snapshot.documents.first()
        val bolao = doc.data<BolaoDto>().toDomain(doc.id)

        if (userId in bolao.participants) return bolao
        if (userId in bolao.pendingParticipants) return bolao

        val updatedPending = bolao.pendingParticipants + userId
        collection.document(bolao.id).update("pendingParticipants" to updatedPending)

        return bolao.copy(pendingParticipants = updatedPending)
    }

    override suspend fun approveJoinRequest(bolaoId: String, userId: String, approve: Boolean) {
        if (approve) {
            collection.document(bolaoId).update(
                "participants" to FieldValue.arrayUnion(userId),
                "pendingParticipants" to FieldValue.arrayRemove(userId)
            )
            // Initialize the ranking entry when approving the join request
            initializeRankingEntry(bolaoId, userId)
        } else {
            collection.document(bolaoId).update("pendingParticipants" to FieldValue.arrayRemove(userId))
        }
    }

    override suspend fun addParticipantDirectly(bolaoId: String, userId: String) {
        logger.d { "[BolaoRepo] addParticipantDirectly INICIO. BolaoId: $bolaoId, UserId: $userId" }

        try {
            val userRef = db.collection("users").document(userId)
            logger.d { "[BolaoRepo] Tentando atualizar lastActiveAt para $userId..." }
            userRef.set(
                mapOf(
                    "lastActiveAt" to TimeSource.nowMillis()
                ),
                merge = true
            )
            logger.d { "[BolaoRepo] lastActiveAt atualizado com sucesso." }
        } catch (e: Exception) {
            logger.e(e) { "[BolaoRepo] Erro (ignorado) ao atualizar users/$userId" }
        }

        try {
            logger.d { "[BolaoRepo] Tentando update atômico (arrayUnion) no bolão $bolaoId..." }
            collection.document(bolaoId).update(
                "participants" to FieldValue.arrayUnion(userId),
                "pendingParticipants" to FieldValue.arrayRemove(userId)
            )

            // Initialize the ranking entry so the user appears in the list immediately
            initializeRankingEntry(bolaoId, userId)

            logger.d { "[BolaoRepo] update atômico e ranking inicial concluídos." }
        } catch (e: Exception) {
            logger.e(e) { "[BolaoRepo] ERRO no update do bolão" }
            throw e
        }
    }

    private suspend fun initializeRankingEntry(bolaoId: String, userId: String) {
        try {
            val userDoc = db.collection("users").document(userId).get()
            val name = if (userDoc.exists) userDoc.get<String>("name") ?: "Novo Participante" else "Novo Participante"
            val nickname = if (userDoc.exists) userDoc.get<String>("nickname") ?: userDoc.get<String>("username") ?: "" else ""

            db.collection("boloes").document(bolaoId).collection("rankings").document(userId).set(
                mapOf(
                    "userId" to userId,
                    "userName" to name,
                    "userNickname" to nickname,
                    "totalPoints" to 0,
                    "totalExactScores" to 0,
                    "totalCorrectResults" to 0,
                    "matchesPlayed" to 0,
                    "lastUpdate" to TimeSource.nowMillis()
                ),
                merge = true
            )
            logger.d { "[BolaoRepo] Ranking inicial criado para $userId no bolão $bolaoId" }
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao criar ranking inicial")
            logger.e(e) { "[BolaoRepo] Erro ao criar ranking inicial" }
        }
    }

    override suspend fun leaveBolao(bolaoId: String, userId: String) {
        // 1. Remove the user from the participants list
        collection.document(bolaoId).update("participants" to FieldValue.arrayRemove(userId))

        // Note: we do NOT delete the user's predictions.
        // This lets them rejoin the bolão without losing their history.
        // The ranking already filters to active participants only, so they
        // disappear from it automatically.

        // 2. Delete any pending invitation for this user in this bolão
        try {
            val invitesSnapshot =
                db.collection("invitations")
                    .where { "bolaoId" equalTo bolaoId }
                    .get()

            invitesSnapshot.documents.forEach { inviteDoc ->
                val invitee = inviteDoc.get<String>("inviteeIdentifier")
                if (invitee == userId) {
                    db.collection("invitations").document(inviteDoc.id).delete()
                }
            }
        } catch (ignored: Exception) {
            // Best-effort: an orphaned invitation document is harmless clutter, not a correctness issue.
        }
    }

    override suspend fun updateBolao(
        bolaoId: String,
        name: String,
        description: String,
        scope: BolaoScope,
        pointsExactScore: Int,
        pointsWinnerOrDraw: Int
    ) {
        collection.document(bolaoId).update(
            "name" to name,
            "description" to description,
            "scope" to scope.name,
            "pointsExactScore" to pointsExactScore,
            "pointsWinnerOrDraw" to pointsWinnerOrDraw
        )
    }

    override suspend fun deleteBolao(bolaoId: String) {
        // The bolão is not deleted immediately.
        // It is marked for deletion and hidden from users.
        // A Cloud Function or cleanup process removes it after 7 days.
        collection.document(bolaoId).update("deletedAtMillis" to TimeSource.nowMillis())

        // Clear the bolão code so it can no longer be found via search
        collection.document(bolaoId).update("code" to "DELETED_$bolaoId")

        // The underlying data (document, predictions, invitations) still exists for 7 days
        // in case the admin reconsiders (recovery via support or, in the future, in-app).
    }

    override suspend fun removeParticipant(bolaoId: String, userId: String) {
        leaveBolao(bolaoId, userId)
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..BOLAO_CODE_LENGTH).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }

    private companion object {
        private const val PLACEHOLDER_USERNAME_ID_LENGTH = 5
        private const val BOLAO_CODE_LENGTH = 6
    }
}
