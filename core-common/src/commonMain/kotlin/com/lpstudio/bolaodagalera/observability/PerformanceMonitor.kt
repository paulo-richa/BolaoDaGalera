package com.lpstudio.bolaodagalera.observability

/**
 * Measures the duration of critical business operations (login,
 * create/join pool, save prediction, etc.) via Firebase Performance
 * Monitoring on Android. Still a no-op on iOS, but the interface is
 * already KMP-ready.
 */
interface PerformanceMonitor {
    suspend fun <T> trace(name: String, block: suspend () -> T): T

    /**
     * Starts a span-style trace that the caller stops explicitly (typically tied to a screen's
     * composition lifetime via [ScreenTrace], since Firebase Performance only auto-instruments
     * screen rendering per-Activity, and this app is single-Activity - without this, every
     * screen's rendering data rolls up into one generic trace).
     */
    fun startScreenTrace(name: String): ScreenTrace
}

/** Handle for a trace started with [PerformanceMonitor.startScreenTrace]; call [stop] exactly once. */
interface ScreenTrace {
    fun stop()
}

expect fun createPerformanceMonitor(): PerformanceMonitor
