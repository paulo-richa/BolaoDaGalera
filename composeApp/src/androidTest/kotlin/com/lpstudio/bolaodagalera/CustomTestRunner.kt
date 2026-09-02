package com.lpstudio.bolaodagalera

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Instrumented tests run in-process with the app under test (same package/targetPackage),
 * so `android:name` on the androidTest manifest's `<application>` tag is ignored - the
 * Application class must be swapped here instead, forcing [TestApp] (Koin wired with fakes
 * only) in place of the real [BolaoApplication].
 */
class CustomTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application =
        super.newApplication(cl, TestApp::class.java.name, context)
}
