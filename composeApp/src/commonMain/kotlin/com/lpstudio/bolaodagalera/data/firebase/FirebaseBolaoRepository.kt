package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
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
    private val db = Firebase.firestore
    private val collection = db.collection("boloes")

    override fun getUserBoloes(userId: String): Flow<List<Bolao>> = try {
        collection
            .where { "participants" contains userId as Any }
            .snapshots
            .map { snapshot ->
                snapshot.documents
                    .map { doc -> doc.data<BolaoDto>().toDomain(doc.id) }
                    .filter { it.deletedAtMillis == null } // Oculta bolões marcados para deleção
            }
            .catch { e ->
                crashReporter.recordException(e, "Erro ao observar bolões")
                println("BOLAOLOG: Erro ao observar bolões: ${e.message}")
                emit(emptyList())
            }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar bolões")
        println("BOLAOLOG: Erro crítico ao observar bolões: ${e.message}")
        kotlinx.coroutines.flow.flowOf(emptyList())
    }

    override fun getBolaoFlow(bolaoId: String): Flow<Bolao> = try {
        collection.document(bolaoId).snapshots.map { doc ->
            if (doc.exists) {
                doc.data<BolaoDto>().toDomain(doc.id)
            } else {
                Bolao() // Retorna um objeto vazio em vez de crashar se deletado
            }
        }.catch { e ->
            crashReporter.recordException(e, "Erro ao observar bolão $bolaoId")
            println("BOLAOLOG: Erro ao observar bolão $bolaoId: ${e.message}")
            emit(Bolao())
        }
    } catch (e: Exception) {
        crashReporter.recordException(e, "Erro crítico ao observar bolão $bolaoId")
        println("BOLAOLOG: Erro crítico ao observar bolão $bolaoId: ${e.message}")
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

        // 2. Garante que o usuário existe na coleção 'users' para aparecer no ranking
        try {
            val userRef = db.collection("users").document(userId)
            val userDoc = userRef.get()
            if (!userDoc.exists) {
                userRef.set(
                    mapOf(
                        "name" to "Novo Usuário",
                        "email" to "",
                        "username" to "user_${userId.take(5)}",
                        "createdAt" to TimeSource.nowMillis()
                    ),
                    merge = true
                )
            }
        } catch (e: Exception) {
        }

        // 3. Adiciona diretamente aos participantes
        if (userId !in bolao.participants) {
            val updatedParticipants = bolao.participants + userId
            val updatedPending = bolao.pendingParticipants - userId

            collection.document(bolao.id).update(
                "participants" to updatedParticipants,
                "pendingParticipants" to updatedPending
            )

            // Inicializa o ranking para o usuário aparecer na lista imediatamente
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
            // Inicializa o ranking ao aprovar a entrada
            initializeRankingEntry(bolaoId, userId)
        } else {
            collection.document(bolaoId).update("pendingParticipants" to FieldValue.arrayRemove(userId))
        }
    }

    override suspend fun addParticipantDirectly(bolaoId: String, userId: String) {
        println("BOLAOLOG: [BolaoRepo] addParticipantDirectly INICIO. BolaoId: $bolaoId, UserId: $userId")

        try {
            val userRef = db.collection("users").document(userId)
            println("BOLAOLOG: [BolaoRepo] Tentando atualizar lastActiveAt para $userId...")
            userRef.set(
                mapOf(
                    "lastActiveAt" to TimeSource.nowMillis()
                ),
                merge = true
            )
            println("BOLAOLOG: [BolaoRepo] lastActiveAt atualizado com sucesso.")
        } catch (e: Exception) {
            println("BOLAOLOG: [BolaoRepo] Erro (ignorado) ao atualizar users/$userId: ${e.message}")
        }

        try {
            println("BOLAOLOG: [BolaoRepo] Tentando update atômico (arrayUnion) no bolão $bolaoId...")
            collection.document(bolaoId).update(
                "participants" to FieldValue.arrayUnion(userId),
                "pendingParticipants" to FieldValue.arrayRemove(userId)
            )

            // Inicializa o ranking para o usuário aparecer na lista imediatamente
            initializeRankingEntry(bolaoId, userId)

            println("BOLAOLOG: [BolaoRepo] update atômico e ranking inicial concluídos.")
        } catch (e: Exception) {
            println("BOLAOLOG: [BolaoRepo] ERRO no update do bolão: ${e.message}")
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
            println("BOLAOLOG: [BolaoRepo] Ranking inicial criado para $userId no bolão $bolaoId")
        } catch (e: Exception) {
            crashReporter.recordException(e, "Erro ao criar ranking inicial")
            println("BOLAOLOG: [BolaoRepo] Erro ao criar ranking inicial: ${e.message}")
        }
    }

    override suspend fun leaveBolao(bolaoId: String, userId: String) {
        // 1. Remove o usuário da lista de participantes
        collection.document(bolaoId).update("participants" to FieldValue.arrayRemove(userId))

        // Nota: NÃO apagamos os palpites (predictions) do usuário.
        // Isso permite que ele volte ao bolão sem perder seu histórico.
        // O ranking já filtra apenas por participantes ativos, então ele sumirá do ranking automaticamente.

        // 2. Apaga qualquer convite pendente para este usuário neste bolão
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
        } catch (e: Exception) {
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
        // Agora o bolão não é deletado imediatamente.
        // Ele é marcado para deleção e ficará oculto para os usuários.
        // Uma Cloud Function ou processo de limpeza removerá após 7 dias.
        collection.document(bolaoId).update("deletedAtMillis" to TimeSource.nowMillis())

        // Removemos o código do bolão para que ele não possa ser encontrado via busca
        collection.document(bolaoId).update("code" to "DELETED_$bolaoId")

        // Os dados reais (documento, palpites e convites) continuam existindo por 7 dias
        // caso o admin se arrependa (recuperação via suporte ou futuramente no app).
    }

    override suspend fun removeParticipant(bolaoId: String, userId: String) {
        leaveBolao(bolaoId, userId)
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
