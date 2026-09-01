package com.lpstudio.bolaodagalera.observability

import com.google.firebase.crashlytics.FirebaseCrashlytics

private class FirebaseCrashReporter : CrashReporter {
    private val crashlytics get() = FirebaseCrashlytics.getInstance()

    override fun recordException(throwable: Throwable, message: String?) {
        if (message != null) crashlytics.log(message)
        crashlytics.recordException(throwable)
    }

    override fun log(message: String) {
        crashlytics.log(message)
    }

    override fun setUserId(userId: String?) {
        crashlytics.setUserId(userId ?: "")
    }
}

actual fun createCrashReporter(): CrashReporter = FirebaseCrashReporter()
