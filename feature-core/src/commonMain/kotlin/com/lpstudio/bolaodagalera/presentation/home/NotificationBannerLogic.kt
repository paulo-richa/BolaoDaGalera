package com.lpstudio.bolaodagalera.presentation.home

internal const val NOTIFICATION_BANNER_INTERVAL_MILLIS = 7 * 24 * 60 * 60 * 1000L

/**
 * The notifications-disabled banner is throttled to once a week per device -
 * showing it on every Home visit would be naggy for someone who's simply
 * chosen to keep notifications off. The QA override (forceShow, driven by a
 * Remote Config flag) always wins and ignores the throttle, since its whole
 * purpose is letting the banner be previewed on demand.
 */
internal fun shouldShowNotificationBanner(
    notificationsEnabled: Boolean,
    forceShow: Boolean,
    lastShownMillis: Long,
    nowMillis: Long
): Boolean = when {
    forceShow -> true
    notificationsEnabled -> false
    else -> nowMillis - lastShownMillis >= NOTIFICATION_BANNER_INTERVAL_MILLIS
}
