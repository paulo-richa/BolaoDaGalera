package com.lpstudio.bolaodagalera.observability

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

private val flowErrorLogger = appLogger("FlowErrorReporting")

/**
 * Reports [context] + the exception to Crashlytics and the local logger, then rethrows so
 * downstream collectors (a ViewModel's own `.catch{}`) can distinguish "load failed" from an
 * empty result - unlike swallowing to `emit(emptyList())`, which makes both look identical to
 * the user. Never swallows [CancellationException], since doing so would break structured
 * concurrency for whatever coroutine is collecting this flow.
 */
fun <T> Flow<T>.reportAndRethrow(crashReporter: CrashReporter, context: String): Flow<T> = catch { e ->
    if (e is CancellationException) throw e
    crashReporter.recordException(e, context)
    flowErrorLogger.e(e) { context }
    throw e
}
