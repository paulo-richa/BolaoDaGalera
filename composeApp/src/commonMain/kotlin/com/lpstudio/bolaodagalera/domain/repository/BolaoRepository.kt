package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Bolao
import com.lpstudio.bolaodagalera.domain.model.BolaoScope
import kotlinx.coroutines.flow.Flow

interface BolaoRepository {
    fun getUserBoloes(userId: String): Flow<List<Bolao>>
    fun getBolaoFlow(bolaoId: String): Flow<Bolao>
    suspend fun getBolao(bolaoId: String): Bolao
    suspend fun createBolao(
        name: String, 
        description: String, 
        ownerId: String, 
        championshipId: String = "COPA_2026",
        scope: BolaoScope = BolaoScope.FULL,
        specificMatchId: String? = null,
        pointsExactScore: Int = 3,
        pointsWinnerOrDraw: Int = 1
    ): Bolao
    suspend fun requestJoinBolao(code: String, userId: String): Bolao
    suspend fun approveJoinRequest(bolaoId: String, userId: String, approve: Boolean)
    suspend fun joinBolao(code: String, userId: String): Bolao
    suspend fun addParticipantDirectly(bolaoId: String, userId: String)
    suspend fun leaveBolao(bolaoId: String, userId: String)
    suspend fun updateBolao(
        bolaoId: String, 
        name: String, 
        description: String, 
        scope: BolaoScope,
        pointsExactScore: Int, 
        pointsWinnerOrDraw: Int
    )
    suspend fun deleteBolao(bolaoId: String)
    suspend fun removeParticipant(bolaoId: String, userId: String)
}
