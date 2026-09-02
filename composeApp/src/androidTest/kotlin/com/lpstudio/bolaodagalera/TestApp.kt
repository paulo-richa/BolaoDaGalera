package com.lpstudio.bolaodagalera

import android.app.Application
import com.lpstudio.bolaodagalera.ads.AdBannerProvider
import com.lpstudio.bolaodagalera.data.fake.FakeAdBannerProvider
import com.lpstudio.bolaodagalera.data.fake.FakeAuthRepository
import com.lpstudio.bolaodagalera.data.fake.FakeBolaoRepository
import com.lpstudio.bolaodagalera.data.fake.FakeCrashReporter
import com.lpstudio.bolaodagalera.data.fake.FakeMatchRepository
import com.lpstudio.bolaodagalera.data.fake.FakePredictionRepository
import com.lpstudio.bolaodagalera.domain.repository.AuthRepository
import com.lpstudio.bolaodagalera.domain.repository.BolaoRepository
import com.lpstudio.bolaodagalera.domain.repository.MatchRepository
import com.lpstudio.bolaodagalera.domain.repository.PredictionRepository
import com.lpstudio.bolaodagalera.observability.CrashReporter
import com.lpstudio.bolaodagalera.presentation.bolao.BolaoViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

/** Application used for instrumented tests (`src/androidTest/AndroidManifest.xml`), wired with fakes only. */
class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApp)
            modules(
                module {
                    val matchRepo = FakeMatchRepository()
                    single<AuthRepository> { FakeAuthRepository() }
                    single<BolaoRepository> { FakeBolaoRepository() }
                    single<MatchRepository> { matchRepo }
                    single<PredictionRepository> { FakePredictionRepository(matchRepo) }
                    single<CrashReporter> { FakeCrashReporter() }
                    single<AdBannerProvider> { FakeAdBannerProvider() }

                    viewModel { (bolaoId: String) -> BolaoViewModel(get(), get(), get(), get(), bolaoId, get()) }
                }
            )
        }
    }
}
