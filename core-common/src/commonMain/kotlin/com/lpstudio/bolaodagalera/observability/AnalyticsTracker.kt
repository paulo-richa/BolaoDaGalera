package com.lpstudio.bolaodagalera.observability

/**
 * Business events (login, signup, create/join pool, save prediction) via
 * Firebase Analytics on Android. Still a no-op on iOS, but the interface
 * is already KMP-ready.
 */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserId(userId: String?)
}

expect fun createAnalyticsTracker(): AnalyticsTracker
