package com.lpstudio.bolaodagalera.di

import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeInvitationRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.data.remote.OpenFootballMatchRepository
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.InvitationRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.presentation.auth.AuthViewModel
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import com.lpstudio.bolaodagalera.presentation.home.HomeViewModel
import com.lpstudio.bolaodagalera.presentation.match.PredictionViewModel
import com.lpstudio.bolaodagalera.presentation.ranking.RankingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

// Módulo híbrido:
//   • Jogos (placares) → openfootball JSON (gratuito, sem chave)
//   • Auth / Bolões / Palpites → fake in-memory
//     (substitua por Firebase quando configurado)

val openFootballAppModule = module {
    single<MatchRepository>     { OpenFootballMatchRepository() }
    single<AuthRepository>      { FakeAuthRepository() }
    single<BolaoRepository>     { FakeBolaoRepository() }
    single<InvitationRepository>{ FakeInvitationRepository() }
    single<PredictionRepository>{ FakePredictionRepository(get()) }

    viewModel { AuthViewModel(get()) }
    viewModel { HomeViewModel(get(), get(), get(), get(), get()) }
    viewModel { (bolaoId: String) -> BolaoViewModel(get(), get(), get(), get(), bolaoId) }
    viewModel { (bolaoId: String, matchId: String) -> PredictionViewModel(get(), get(), get(), bolaoId, matchId) }
    viewModel { (bolaoId: String) -> RankingViewModel(get(), get(), get(), bolaoId) }
}
