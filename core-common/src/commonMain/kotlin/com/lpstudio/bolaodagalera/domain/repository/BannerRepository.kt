package com.lpstudio.bolaodagalera.domain.repository

import com.lpstudio.bolaodagalera.domain.model.Banner
import kotlinx.coroutines.flow.Flow

/**
 * Unlike ChampionshipRepository, there's no refreshCache()/static cache here -
 * banners are consumed only by HomeViewModel, which observes getBanners()
 * directly, so there's no cross-module cache to warm up on app startup.
 */
interface BannerRepository {
    fun getBanners(): Flow<List<Banner>>
}
