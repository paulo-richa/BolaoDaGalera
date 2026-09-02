package com.lpstudio.bolaodagalera.observability

import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Severity
import com.google.firebase.crashlytics.FirebaseCrashlytics

/**
 * Forwards Info-level and above logs to Crashlytics as breadcrumbs,
 * providing context about what happened in the app shortly before a reported crash.
 */
class CrashlyticsLogWriter : LogWriter() {
    override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
        if (severity < Severity.Info) return
        try {
            FirebaseCrashlytics.getInstance().log("[$tag] $message")
        } catch (e: IllegalStateException) {
            // Firebase may not be initialized (e.g. Robolectric tests) - the breadcrumb is
            // a "nice to have" and must never crash the app or break a test because of this.
        }
    }
}
