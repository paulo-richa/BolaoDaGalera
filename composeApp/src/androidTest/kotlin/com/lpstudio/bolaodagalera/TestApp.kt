package com.lpstudio.bolaodagalera

import android.app.Application
import com.lpstudio.bolaodagalera.di.fakeAppModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@TestApp)
            modules(fakeAppModule)
        }
    }
}
