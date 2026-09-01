package com.lpstudio.bolaodagalera.observability

private class NoOpPerformanceMonitor : PerformanceMonitor {
    override suspend fun <T> trace(name: String, block: suspend () -> T): T = block()
}

actual fun createPerformanceMonitor(): PerformanceMonitor = NoOpPerformanceMonitor()
