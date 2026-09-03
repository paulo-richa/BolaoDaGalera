package com.lpstudio.bolaodagalera.observability

/**
 * Bundles [PerformanceMonitor] and [AnalyticsTracker] together to keep constructor parameter
 * lists manageable in ViewModels that already take several repositories - both are almost
 * always injected as a pair (every traced write action also fires a matching analytics event).
 */
class Telemetry(val performanceMonitor: PerformanceMonitor, val analyticsTracker: AnalyticsTracker)
