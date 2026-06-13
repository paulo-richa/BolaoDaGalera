package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBolaoRepository : BolaoRepository {

    private val _boloes = MutableStateFlow(
        listOf(
            Bolao(
                id = "bolao-1",
                name = "Bolão da Galera",
                description = "Copa do Mundo 2026 🏆",
                pointsExactScore = 3,
                pointsWinnerOrDraw = 1,
                code = "COPA26",
                ownerId = "pauloricha",
                participants = listOf("pauloricha", "user-2", "u3", "u4", "u5", "u6", "u7", "u8", "u9"),
                createdAtMillis = 1781136000000L
            )
        )
    )

    override fun getUserBoloes(userId: String): Flow<List<Bolao>> =
        _boloes.map { list -> list.filter { userId in it.participants } }

    override fun getBolaoFlow(bolaoId: String): Flow<Bolao> =
        _boloes.map { list -> list.first { it.id == bolaoId } }

    override suspend fun getBolao(bolaoId: String): Bolao =
        _boloes.value.first { it.id == bolaoId }

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
        val code = ('A'..'Z').shuffled().take(3).joinToString("") + (100..999).random()
        val newBolao = Bolao(
            id = "bolao-${_boloes.value.size + 1}",
            name = name,
            description = description,
            pointsExactScore = pointsExactScore,
            pointsWinnerOrDraw = pointsWinnerOrDraw,
            code = code,
            ownerId = ownerId,
            participants = listOf(ownerId),
            championshipId = championshipId,
            scope = scope,
            specificMatchId = specificMatchId,
            createdAtMillis = 1781136000000L
        )
        _boloes.update { it + newBolao }
        return newBolao
    }

    override suspend fun joinBolao(code: String, userId: String): Bolao {
        return requestJoinBolao(code, userId)
    }

    override suspend fun requestJoinBolao(code: String, userId: String): Bolao {
        val bolao = _boloes.value.firstOrNull { it.code.equals(code, ignoreCase = true) }
            ?: error("Bolão não encontrado com o código $code")
        
        if (userId !in bolao.participants && userId !in bolao.pendingParticipants) {
            _boloes.update { list ->
                list.map { if (it.id == bolao.id) it.copy(pendingParticipants = it.pendingParticipants + userId) else it }
            }
        }
        return _boloes.value.first { it.id == bolao.id }
    }

    override suspend fun approveJoinRequest(bolaoId: String, userId: String, approve: Boolean) {
        _boloes.update { list ->
            list.map { bolao ->
                if (bolao.id == bolaoId) {
                    val newPending = bolao.pendingParticipants - userId
                    if (approve) {
                        bolao.copy(
                            participants = bolao.participants + userId,
                            pendingParticipants = newPending
                        )
                    } else {
                        bolao.copy(pendingParticipants = newPending)
                    }
                } else bolao
            }
        }
    }

    override suspend fun addParticipantDirectly(bolaoId: String, userId: String) {
        _boloes.update { list ->
            list.map { bolao ->
                if (bolao.id == bolaoId) {
                    if (userId !in bolao.participants) {
                        bolao.copy(participants = bolao.participants + userId)
                    } else bolao
                } else bolao
            }
        }
    }

    override suspend fun leaveBolao(bolaoId: String, userId: String) {
        _boloes.update { list ->
            list.map { if (it.id == bolaoId) it.copy(participants = it.participants - userId) else it }
        }
    }

    override suspend fun updateBolao(bolaoId: String, name: String, description: String, scope: BolaoScope, pointsExactScore: Int, pointsWinnerOrDraw: Int) {
        _boloes.update { list ->
            list.map { 
                if (it.id == bolaoId) it.copy(
                    name = name, 
                    description = description,
                    scope = scope,
                    pointsExactScore = pointsExactScore,
                    pointsWinnerOrDraw = pointsWinnerOrDraw
                ) else it 
            }
        }
    }

    override suspend fun deleteBolao(bolaoId: String) {
        _boloes.update { list -> list.filter { it.id != bolaoId } }
    }

    override suspend fun removeParticipant(bolaoId: String, userId: String) {
        leaveBolao(bolaoId, userId)
    }
}
