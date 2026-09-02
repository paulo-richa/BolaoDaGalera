package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.observability.CrashReporter

class FakeCrashReporter : CrashReporter {
    val recordedExceptions = mutableListOf<Throwable>()
    val loggedMessages = mutableListOf<String>()
    var userId: String? = null
        private set

    override fun recordException(throwable: Throwable, message: String?) {
        message?.let { loggedMessages.add(it) }
        recordedExceptions.add(throwable)
    }

    override fun log(message: String) {
        loggedMessages.add(message)
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }
}
