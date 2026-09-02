package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter
import com.lpstudio.bolaodagalera.data.fake.FakeAnalyticsTracker
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeChampionshipRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeInterstitialAdCounter
import com.lpstudio.bolaodagalera.data.fake.FakeInvitationRepository
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakeNotificationRepository
import com.lpstudio.bolaodagalera.data.fake.FakePerformanceMonitor
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.data.fake.FakeSupportRepository
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.ChampionshipRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.NotificationRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.domain.repository.SupportRepository
import com.lpstudio.bolaodagalera.domain.usecase.CalculatePointsUseCase
import com.lpstudio.bolaodagalera.observability.AnalyticsTracker
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
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
        single<NotificationRepository> { FakeNotificationRepository() }
        single<CrashReporter> { FakeCrashReporter() }
        single<PerformanceMonitor> { FakePerformanceMonitor() }
        single<AnalyticsTracker> { FakeAnalyticsTracker() }
        single<InterstitialAdCounter> { FakeInterstitialAdCounter() }

        // UseCases
        single { CalculatePointsUseCase() }

        // ViewModels
        viewModel { AuthViewModel(get(), get(), get(), get()) }
        viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
        viewModel { (bolaoId: String) -> BolaoViewModel(get(), get(), get(), get(), bolaoId, get()) }
        viewModel { (bolaoId: String, matchId: String) ->
            PredictionViewModel(get(), get(), get(), get(), get(), get(), get(), bolaoId, matchId)
        }
        viewModel { (bolaoId: String) ->
            RankingViewModel(
                predictionRepository = get(),
                bolaoRepository = get(),
                matchRepository = get(),
                authRepository = get(),
                crashReporter = get(),
                calculatePointsUseCase = get(),
                bolaoId = bolaoId
            )
        }
    }
