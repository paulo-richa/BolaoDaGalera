package com.lpstudio.bolaodagalera.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.lpstudio.bolaodagalera.observability.PerformanceMonitor
import org.koin.compose.koinInject

/**
 * Starts a Firebase Performance trace for [name] when this composable enters composition and
 * stops it when it leaves - ties a span to the lifetime of one [NavGraph] destination. Firebase
 * Performance only auto-instruments screen rendering per-Activity, and this app is a single
 * Activity, so without this every screen's rendering data rolls up into one generic trace;
 * calling this once per route gives per-screen data in the dashboard instead.
 */
@Composable
fun ScreenPerformanceTrace(name: String) {
    val performanceMonitor = koinInject<PerformanceMonitor>()
    DisposableEffect(name) {
        val trace = performanceMonitor.startScreenTrace(name)
        onDispose { trace.stop() }
    }
}
