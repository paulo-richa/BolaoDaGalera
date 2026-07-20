package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Championship
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeChampionshipRepository : ChampionshipRepository {
    override fun getChampionships(): Flow<List<Championship>> = flowOf(Championship.getAll())
    override suspend fun refreshCache() {
        Championship.setCache(Championship.getAll())
    }
}
