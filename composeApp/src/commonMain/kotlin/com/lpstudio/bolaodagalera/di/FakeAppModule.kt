package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeInvitationRepository
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.dsl.module

val fakeAppModule = module {
    // Repositórios in-memory — não precisam de Firebase
    single<MatchRepository> { FakeMatchRepository() }
    single<AuthRepository> { FakeAuthRepository() }
    single<BolaoRepository> { FakeBolaoRepository() }
    single<InvitationRepository> { FakeInvitationRepository() }
    single<PredictionRepository> { FakePredictionRepository(get()) }

    // UseCases
    single { CalculatePointsUseCase() }

    // ViewModels (idênticos ao appModule)
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
