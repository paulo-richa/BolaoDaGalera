package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.ads.AdBannerProvider
import com.lpstudio.bolaodagalera.ads.InterstitialAdCounter
import com.lpstudio.bolaodagalera.data.firebase.FirebaseAuthRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseBolaoRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseChampionshipRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseInvitationRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseMatchRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseNotificationRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebasePredictionRepository
import com.lpstudio.bolaodagalera.data.firebase.FirebaseSupportRepository
import com.lpstudio.bolaodagalera.data.remote.RemoteConfigManager
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
import com.lpstudio.bolaodagalera.observability.createAnalyticsTracker
import com.lpstudio.bolaodagalera.observability.createCrashReporter
import com.lpstudio.bolaodagalera.observability.createPerformanceMonitor
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.components.BolaoAdBannerProvider
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import com.lpstudio.bolaodagalera.util.PredictionAdCounter
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        // Repositories (Firebase Production)
        single<AuthRepository> { FirebaseAuthRepository(get()) }
        single<BolaoRepository> { FirebaseBolaoRepository(get()) }
        single<MatchRepository> { FirebaseMatchRepository(get()) }
        single<InvitationRepository> { FirebaseInvitationRepository(get(), get()) }
        single<PredictionRepository> { FirebasePredictionRepository(get(), get()) }
        single<ChampionshipRepository> { FirebaseChampionshipRepository(get()) }
        single<SupportRepository> { FirebaseSupportRepository() }
        single<NotificationRepository> { FirebaseNotificationRepository(get()) }
        single<CrashReporter> { createCrashReporter() }
        single<PerformanceMonitor> { createPerformanceMonitor() }
        single<AnalyticsTracker> { createAnalyticsTracker() }
        single<InterstitialAdCounter> { PredictionAdCounter }
        single<AdBannerProvider> { BolaoAdBannerProvider() }

        // Remote Config
        single { RemoteConfigManager() }

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
