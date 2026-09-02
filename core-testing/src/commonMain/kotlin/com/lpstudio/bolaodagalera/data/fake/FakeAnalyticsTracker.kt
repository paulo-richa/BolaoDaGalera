package com.lpstudio.bolaodagalera.data.fake

import com.lpstudio.bolaodagalera.observability.AnalyticsTracker

class FakeAnalyticsTracker : AnalyticsTracker {
    val loggedEvents = mutableListOf<Pair<String, Map<String, Any?>>>()
    var userId: String? = null
        private set

    override fun logEvent(name: String, params: Map<String, Any?>) {
        loggedEvents.add(name to params)
    }

    override fun setUserId(userId: String?) {
        this.userId = userId
    }
}
