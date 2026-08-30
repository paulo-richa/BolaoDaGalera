package com.lpstudio.bolaodagalera

import android.app.Application

class TestApp : Application() {
    override fun onCreate() {
        super.onCreate()
        /* startKoin {
            androidContext(this@TestApp)
            modules(fakeAppModule)
        } */
    }
}
