package com.lpstudio.bolaodagalera.observability

/**
 * Reports unexpected (non-fatal) failures for later investigation - network
 * errors, corrupted data, etc. Not meant for every catch block: expected
 * flows (validation, "already exists") should only log locally, to avoid
 * polluting the dashboard. Crashlytics is not yet configured on iOS (same
 * decision as FCM: Android first), so createCrashReporter() returns a
 * no-op there.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, message: String? = null)

    fun log(message: String)

    fun setUserId(userId: String?)
}

expect fun createCrashReporter(): CrashReporter
