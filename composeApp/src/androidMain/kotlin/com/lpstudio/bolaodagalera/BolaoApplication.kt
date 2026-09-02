package com.lpstudio.bolaodagalera

import android.app.Application
import co.touchlab.kermit.Logger
import com.lpstudio.bolaodagalera.di.appModule
import com.lpstudio.bolaodagalera.observability.CrashlyticsLogWriter
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

/**
 * The KoinApplication (Compose) used in App.kt does not register a global Koin
 * instance - it is only valid within the composition tree. Android components
 * outside Compose (such as FirebaseMessagingService, which runs even when the
 * app is backgrounded/killed) require an actual global instance, so Koin is
 * also started here.
 */
class BolaoApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BolaoApplication)
            modules(appModule)
        }
        // Forward Kermit logs as Crashlytics breadcrumbs in addition to the default
        // writer (Logcat), providing context on what happened before a crash.
        Logger.addLogWriter(CrashlyticsLogWriter())
    }
}
