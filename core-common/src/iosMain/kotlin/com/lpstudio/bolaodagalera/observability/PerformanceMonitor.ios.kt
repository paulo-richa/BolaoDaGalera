package com.lpstudio.bolaodagalera.observability

private object NoOpScreenTrace : ScreenTrace {
    override fun stop() = Unit
}

private class NoOpPerformanceMonitor : PerformanceMonitor {
    override suspend fun <T> trace(name: String, block: suspend () -> T): T = block()

    override fun startScreenTrace(name: String): ScreenTrace = NoOpScreenTrace
}

actual fun createPerformanceMonitor(): PerformanceMonitor = NoOpPerformanceMonitor()
