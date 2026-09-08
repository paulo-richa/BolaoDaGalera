package com.lpstudio.bolaodagalera

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun SystemAppearance(isDark: Boolean)

@Composable
expect fun rememberLauncherProvider(): LauncherProvider

@Composable
expect fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)

/**
 * Whether the OS currently allows this app to show notifications - reevaluated
 * whenever the app resumes, since the only way this changes is the user
 * leaving to Settings and coming back (there's no in-process callback for it).
 */
@Composable
expect fun rememberAreNotificationsEnabled(): Boolean

/**
 * Persists (locally, per-device) the last time the Home notifications-disabled
 * banner was shown - lets callers throttle how often it's nagged, without
 * needing any server round-trip for something purely about this device's
 * own display history.
 */
class NotificationBannerPrefs(val lastShownMillis: Long, val markShown: () -> Unit)

@Composable
expect fun rememberNotificationBannerPrefs(): NotificationBannerPrefs

interface LauncherProvider {
    fun shareText(text: String)

    fun sendEmail(address: String, subject: String, body: String)

    fun sendWhatsApp(phone: String, text: String)

    fun openNotificationSettings()
}
