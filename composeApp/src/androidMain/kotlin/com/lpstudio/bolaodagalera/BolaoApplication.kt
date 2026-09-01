package com.lpstudio.bolaodagalera

import android.app.Application
import com.lpstudio.bolaodagalera.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * KoinApplication (Compose) usado em App.kt não registra uma instância global
 * do Koin - ela só vale dentro da árvore de composição. Componentes Android
 * fora do Compose (como o FirebaseMessagingService, que roda mesmo com o app
 * em background/morto) precisam de uma instância global de verdade, por isso
 * iniciamos o Koin aqui também.
 */
class BolaoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BolaoApplication)
            modules(appModule)
        }
    }
}
