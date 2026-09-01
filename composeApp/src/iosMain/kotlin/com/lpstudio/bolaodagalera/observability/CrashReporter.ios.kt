package com.lpstudio.bolaodagalera.observability

private class NoOpCrashReporter : CrashReporter {
    override fun recordException(throwable: Throwable, message: String?) = Unit

    override fun log(message: String) = Unit

    override fun setUserId(userId: String?) = Unit
}

actual fun createCrashReporter(): CrashReporter = NoOpCrashReporter()
