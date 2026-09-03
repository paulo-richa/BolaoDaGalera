package com.lpstudio.bolaodagalera.observability

import com.google.firebase.perf.FirebasePerformance

private class FirebasePerformanceMonitor : PerformanceMonitor {
    override suspend fun <T> trace(name: String, block: suspend () -> T): T {
        val trace = FirebasePerformance.getInstance().newTrace(name)
        trace.start()
        return try {
            block()
        } finally {
            trace.stop()
        }
    }

    override fun startScreenTrace(name: String): ScreenTrace {
        val trace = FirebasePerformance.getInstance().newTrace(name)
        trace.start()
        return object : ScreenTrace {
            override fun stop() = trace.stop()
        }
    }
}

actual fun createPerformanceMonitor(): PerformanceMonitor = FirebasePerformanceMonitor()
