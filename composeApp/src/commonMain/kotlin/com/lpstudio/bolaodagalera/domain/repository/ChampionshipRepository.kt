package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Championship
import kotlinx.coroutines.flow.Flow

interface ChampionshipRepository {
    fun getChampionships(): Flow<List<Championship>>
    suspend fun refreshCache()
}
