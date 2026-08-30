package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeChampionshipRepository
import com.lpstudio.bolaodagalera.data.fake.FakeInvitationRepository
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.data.fake.FakeSupportRepository
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val fakeAppModule =
    module {
        // Fakes
        single<AuthRepository> { FakeAuthRepository() }
        single<BolaoRepository> { FakeBolaoRepository() }
        val matchRepo = FakeMatchRepository()
        single<MatchRepository> { matchRepo }
        single<InvitationRepository> { FakeInvitationRepository() }
        single<PredictionRepository> { FakePredictionRepository(matchRepo) }
        single<ChampionshipRepository> { FakeChampionshipRepository() }
        single<SupportRepository> { FakeSupportRepository() }

        // UseCases
        single { CalculatePointsUseCase() }

        // ViewModels
        viewModel { AuthViewModel(get()) }
        viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
        viewModel { (bolaoId: String) -> BolaoViewModel(get(), get(), get(), get(), bolaoId) }
        viewModel { (bolaoId: String, matchId: String) -> PredictionViewModel(get(), get(), get(), bolaoId, matchId) }
        viewModel { (bolaoId: String) ->
            RankingViewModel(
                predictionRepository = get(),
                bolaoRepository = get(),
                matchRepository = get(),
                authRepository = get(),
                calculatePointsUseCase = get(),
                bolaoId = bolaoId
            )
        }
    }
