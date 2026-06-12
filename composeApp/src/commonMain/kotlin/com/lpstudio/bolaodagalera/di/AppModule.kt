package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.data.firebase.*
import com.lpstudio.bolaodagalera.domain.repository.*
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories (Firebase Production)
    single<AuthRepository>      { FirebaseAuthRepository() }
    single<BolaoRepository>     { FirebaseBolaoRepository() }
    single<MatchRepository>     { FirebaseMatchRepository() }
    single<InvitationRepository>{ FirebaseInvitationRepository(get()) }
    single<PredictionRepository>{ FirebasePredictionRepository(get()) }

    // UseCases
    single { CalculatePointsUseCase() }

    // ViewModels
    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { (bolaoId: String) -> BolaoViewModel(get(), get(), get(), get(), bolaoId) }
    viewModel { (bolaoId: String, matchId: String) -> PredictionViewModel(get(), get(), get(), bolaoId, matchId) }
    viewModel { (bolaoId: String) -> RankingViewModel(get(), get(), get(), bolaoId) }
}
