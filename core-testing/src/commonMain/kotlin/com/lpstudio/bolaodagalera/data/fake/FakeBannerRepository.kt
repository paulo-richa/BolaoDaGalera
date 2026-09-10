package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.domain.model.Banner
import com.lpstudio.bolaodagalera.domain.repository.BannerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBannerRepository(private val banners: List<Banner> = emptyList()) : BannerRepository {
    override fun getBanners(): Flow<List<Banner>> = flowOf(banners)
}
