package com.lpstudio.bolaodagalera.observability

private class NoOpAnalyticsTracker : AnalyticsTracker {
    override fun logEvent(name: String, params: Map<String, Any?>) = Unit

    override fun setUserId(userId: String?) = Unit
}

actual fun createAnalyticsTracker(): AnalyticsTracker = NoOpAnalyticsTracker()
