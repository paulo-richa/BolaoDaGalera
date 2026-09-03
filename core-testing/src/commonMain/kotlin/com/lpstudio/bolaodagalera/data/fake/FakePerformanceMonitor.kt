package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import com.lpstudio.bolaodagalera.observability.ScreenTrace

class FakePerformanceMonitor : PerformanceMonitor {
    val tracedNames = mutableListOf<String>()
    val screenTraceNames = mutableListOf<String>()

    override suspend fun <T> trace(name: String, block: suspend () -> T): T {
        tracedNames.add(name)
        return block()
    }

    override fun startScreenTrace(name: String): ScreenTrace {
        screenTraceNames.add(name)
        return object : ScreenTrace {
            override fun stop() = Unit
        }
    }
}
