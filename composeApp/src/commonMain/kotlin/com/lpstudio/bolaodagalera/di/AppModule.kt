package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.data.firebase.*
import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
import com.lpstudio.bolaodagalera.domain.repository.*
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories (Firebase Production)
    single<AuthRepository>      { FirebaseAuthRepository() }
    single<BolaoRepository>     { FirebaseBolaoRepository() }
    single<MatchRepository>     { FirebaseMatchRepository() }
    single<InvitationRepository>{ FirebaseInvitationRepository(get()) }
    single<PredictionRepository>{ FirebasePredictionRepository(get()) }
    single<ChampionshipRepository>{ FirebaseChampionshipRepository() }

    // Remote Config
    single { RemoteConfigManager() }

    // UseCases
    single { CalculatePointsUseCase() }

    // ViewModels
    viewModel { AuthViewModel(get<AuthRepository>()) }
    viewModel { HomeViewModel(get<AuthRepository>(), get<BolaoRepository>(), get<MatchRepository>(), get<InvitationRepository>(), get<PredictionRepository>()) }
    viewModel { (bolaoId: String) -> BolaoViewModel(get<BolaoRepository>(), get<MatchRepository>(), get<PredictionRepository>(), get<AuthRepository>(), bolaoId) }
    viewModel { (bolaoId: String, matchId: String) -> PredictionViewModel(get<MatchRepository>(), get<PredictionRepository>(), get<BolaoRepository>(), bolaoId, matchId) }
    viewModel { (bolaoId: String) -> 
        RankingViewModel(
            predictionRepository = get<PredictionRepository>(),
            bolaoRepository = get<BolaoRepository>(),
            matchRepository = get<MatchRepository>(),
            authRepository = get<AuthRepository>(),
            calculatePointsUseCase = get<CalculatePointsUseCase>(),
            bolaoId = bolaoId
        )
    }
}
