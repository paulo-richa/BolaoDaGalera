package com.lpstudio.bolaodagalera.data.firebase

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.util.TimeSource
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.random.Random

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
    val championshipId: String = "COPA_2026",
    val scope: String = "FULL",
    val specificMatchId: String? = null,
    val createdAtMillis: Long = 0L
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
    championshipId = championshipId,
    scope = try { BolaoScope.valueOf(scope) } catch (e: Exception) { BolaoScope.FULL },
    specificMatchId = specificMatchId,
    createdAtMillis = createdAtMillis
)

class FirebaseBolaoRepository : BolaoRepository {

    private val db = Firebase.firestore
    private val collection = db.collection("boloes")

    override fun getUserBoloes(userId: String): Flow<List<Bolao>> {
        return collection
            .where { "participants" contains userId as Any }
            .snapshots
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    doc.data<BolaoDto>().toDomain(doc.id)
                }
            }
    }

    override fun getBolaoFlow(bolaoId: String): Flow<Bolao> {
        return collection.document(bolaoId).snapshots.map { doc ->
            if (doc.exists) {
                doc.data<BolaoDto>().toDomain(doc.id)
            } else {
                Bolao() // Retorna um objeto vazio em vez de crashar se deletado
            }
        }
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
        val dto = BolaoDto(
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

    override suspend fun joinBolao(code: String, userId: String): Bolao {
        // 1. Verificação de Prazo: 4ª Rodada
        val deadline = 1782604800000L // 28 de Junho de 2026
        if (TimeSource.nowMillis() >= deadline) {
            error("As inscrições para este bolão foram encerradas.")
        }

        val snapshot = collection.where { "code" equalTo code.uppercase() }.get()
        if (snapshot.documents.isEmpty()) error("Bolão não encontrado com o código $code")

        val doc = snapshot.documents.first()
        val bolao = doc.data<BolaoDto>().toDomain(doc.id)

        // 2. Garante que o usuário existe na coleção 'users' para aparecer no ranking
        try {
            val userRef = db.collection("users").document(userId)
            val userDoc = userRef.get()
            if (!userDoc.exists) {
                // Se não existe, cria um perfil básico (fallback)
                // Isso evita que o usuário entre no bolão mas fique 'invisível' no ranking
                userRef.set(mapOf(
                    "name" to "Novo Usuário",
                    "email" to "",
                    "username" to "user_${userId.take(5)}",
                    "createdAt" to TimeSource.nowMillis()
                ), merge = true)
            }
        } catch (e: Exception) { }

        // 3. Adiciona diretamente aos participantes e remove do pending se existir
        if (userId !in bolao.participants) {
            val updatedParticipants = bolao.participants + userId
            val updatedPending = bolao.pendingParticipants - userId
            
            collection.document(bolao.id).update(
                "participants" to updatedParticipants,
                "pendingParticipants" to updatedPending
            )
            return bolao.copy(participants = updatedParticipants, pendingParticipants = updatedPending)
        }

        return bolao
    }

    override suspend fun requestJoinBolao(code: String, userId: String): Bolao {
        // Verificação de Prazo: 4ª Rodada
        // Na Copa 2026, a fase de grupos acaba dia 27/06. O mata-mata (Round 4+) inicia em 28/06.
        val deadline = 1782604800000L // 28 de Junho de 2026
        if (TimeSource.nowMillis() >= deadline) {
            error("As inscrições para este bolão foram encerradas (Prazo: 3ª Rodada).")
        }

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
        val doc = collection.document(bolaoId).get()
        val dto = doc.data<BolaoDto>()
        
        val newPending = dto.pendingParticipants - userId
        if (approve) {
            val newParticipants = dto.participants + userId
            collection.document(bolaoId).update(
                "participants" to newParticipants,
                "pendingParticipants" to newPending
            )
        } else {
            collection.document(bolaoId).update("pendingParticipants" to newPending)
        }
    }

    override suspend fun addParticipantDirectly(bolaoId: String, userId: String) {
        // 1. Garante que o usuário existe na coleção 'users'
        try {
            val userRef = db.collection("users").document(userId)
            val userDoc = userRef.get()
            if (!userDoc.exists) {
                userRef.set(mapOf(
                    "name" to "Novo Usuário",
                    "email" to "",
                    "username" to "user_${userId.take(5)}",
                    "createdAt" to TimeSource.nowMillis()
                ), merge = true)
            }
        } catch (e: Exception) { }

        // 2. Adiciona ao bolão e limpa pendências
        val doc = collection.document(bolaoId).get()
        val dto = doc.data<BolaoDto>()
        
        if (userId !in dto.participants) {
            val updatedParticipants = dto.participants + userId
            val updatedPending = dto.pendingParticipants - userId
            
            collection.document(bolaoId).update(
                "participants" to updatedParticipants,
                "pendingParticipants" to updatedPending
            )
        }
    }

    override suspend fun leaveBolao(bolaoId: String, userId: String) {
        // 1. Remove o usuário da lista de participantes
        val doc = collection.document(bolaoId).get()
        val bolao = doc.data<BolaoDto>().toDomain(doc.id)
        val updatedParticipants = bolao.participants - userId
        collection.document(bolaoId).update("participants" to updatedParticipants)

        // 2. Apaga todos os palpites desse usuário NESTE bolão
        try {
            val predictionsSnapshot = db.collection("predictions")
                .where { "userId" equalTo userId }
                .where { "bolaoId" equalTo bolaoId }
                .get()
            
            predictionsSnapshot.documents.forEach { predictionDoc ->
                db.collection("predictions").document(predictionDoc.id).delete()
            }
        } catch (e: Exception) { }

        // 3. Apaga qualquer convite pendente para este usuário neste bolão
        try {
            val invitesSnapshot = db.collection("invitations")
                .where { "bolaoId" equalTo bolaoId }
                .get()

            invitesSnapshot.documents.forEach { inviteDoc ->
                val invitee = inviteDoc.get<String>("inviteeIdentifier")
                if (invitee == userId) {
                    db.collection("invitations").document(inviteDoc.id).delete()
                }
            }
        } catch (e: Exception) { }
    }

    override suspend fun updateBolao(bolaoId: String, name: String, description: String, scope: BolaoScope, pointsExactScore: Int, pointsWinnerOrDraw: Int) {
        collection.document(bolaoId).update(
            "name" to name,
            "description" to description,
            "scope" to scope.name,
            "pointsExactScore" to pointsExactScore,
            "pointsWinnerOrDraw" to pointsWinnerOrDraw
        )
    }

    override suspend fun deleteBolao(bolaoId: String) {
        // 1. Deleta o bolão
        collection.document(bolaoId).delete()

        // 2. Deleta todos os palpites deste bolão (Limpeza)
        try {
            val predictionsSnapshot = db.collection("predictions")
                .where { "bolaoId" equalTo bolaoId }
                .get()
            
            predictionsSnapshot.documents.forEach { doc ->
                db.collection("predictions").document(doc.id).delete()
            }
        } catch (e: Exception) { }
        
        // 3. Deleta convites deste bolão
        try {
            val invitesSnapshot = db.collection("invitations")
                .where { "bolaoId" equalTo bolaoId }
                .get()
            
            invitesSnapshot.documents.forEach { doc ->
                db.collection("invitations").document(doc.id).delete()
            }
        } catch (e: Exception) { }
    }

    override suspend fun removeParticipant(bolaoId: String, userId: String) {
        leaveBolao(bolaoId, userId)
    }

    private fun generateCode(): String {
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        return (1..6).map { chars[Random.nextInt(chars.length)] }.joinToString("")
    }
}
