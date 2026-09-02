package com.lpstudio.bolaodagalera.observability

/**
 * Measures the duration of critical business operations (login,
 * create/join pool, save prediction, etc.) via Firebase Performance
 * Monitoring on Android. Still a no-op on iOS, but the interface is
 * already KMP-ready.
 */
interface PerformanceMonitor {
    suspend fun <T> trace(name: String, block: suspend () -> T): T
}

expect fun createPerformanceMonitor(): PerformanceMonitor
