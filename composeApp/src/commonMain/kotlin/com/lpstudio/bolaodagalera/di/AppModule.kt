package com.lpstudio.bolaodagalera.di

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
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule =
    module {
        // Repositories (Firebase Production)
        single<AuthRepository> { FirebaseAuthRepository() }
        single<BolaoRepository> { FirebaseBolaoRepository() }
        single<MatchRepository> { FirebaseMatchRepository() }
        single<InvitationRepository> { FirebaseInvitationRepository(get()) }
        single<PredictionRepository> { FirebasePredictionRepository(get()) }
        single<ChampionshipRepository> { FirebaseChampionshipRepository() }
        single<SupportRepository> { FirebaseSupportRepository() }
        single<NotificationRepository> { FirebaseNotificationRepository() }

        // Remote Config
        single { RemoteConfigManager() }

        // UseCases
        single { CalculatePointsUseCase() }

        // ViewModels
        viewModel { AuthViewModel(get()) }
        viewModel { HomeViewModel(get(), get(), get(), get()) }
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
