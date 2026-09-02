package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.observability.PerformanceMonitor

class FakePerformanceMonitor : PerformanceMonitor {
    val tracedNames = mutableListOf<String>()

    override suspend fun <T> trace(name: String, block: suspend () -> T): T {
        tracedNames.add(name)
        return block()
    }
}
