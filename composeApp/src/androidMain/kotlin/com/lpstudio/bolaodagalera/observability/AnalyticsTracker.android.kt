package com.lpstudio.bolaodagalera.observability

import android.os.Bundle
import com.google.firebase.Firebase
import com.google.firebase.analytics.analytics

private class FirebaseAnalyticsTracker : AnalyticsTracker {
    private val analytics = Firebase.analytics

    override fun logEvent(name: String, params: Map<String, Any?>) {
        val bundle =
            Bundle().apply {
                params.forEach { (key, value) ->
                    when (value) {
                        null -> Unit
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
            }
        analytics.logEvent(name, bundle)
    }

    override fun setUserId(userId: String?) {
        analytics.setUserId(userId)
    }
}

actual fun createAnalyticsTracker(): AnalyticsTracker = FirebaseAnalyticsTracker()
