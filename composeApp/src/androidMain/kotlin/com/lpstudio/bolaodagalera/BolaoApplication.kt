package com.lpstudio.bolaodagalera

import android.app.Application
import android.content.pm.ApplicationInfo
import android.os.StrictMode
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
        enableStrictModeInDebug()
        startKoin {
            androidContext(this@BolaoApplication)
            modules(appModule)
        }
        // Forward Kermit logs as Crashlytics breadcrumbs in addition to the default
        // writer (Logcat), providing context on what happened before a crash.
        Logger.addLogWriter(CrashlyticsLogWriter())
    }

    /**
     * Logs (never crashes - `penaltyLog` only, no `penaltyDeath`) accidental disk/network I/O on
     * the main thread and common resource leaks, but only for debuggable builds - checked via
     * the app's own [ApplicationInfo] flag instead of a generated `BuildConfig.DEBUG`, since this
     * module doesn't otherwise need the `buildConfig` Gradle feature enabled.
     */
    private fun enableStrictModeInDebug() {
        val isDebuggable = (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        if (!isDebuggable) return

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
    }
}
