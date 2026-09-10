package com.lpstudio.bolaodagalera.presentation.home

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NotificationBannerLogicTest {
    private val now = 1_000_000_000_000L

    @Test
    fun `nunca aparece quando as notificacoes ja estao ativadas`() {
        assertFalse(
            shouldShowNotificationBanner(notificationsEnabled = true, forceShow = false, lastShownMillis = 0L, nowMillis = now)
        )
    }

    @Test
    fun `aparece na primeira vez, sem registro anterior`() {
        assertTrue(
            shouldShowNotificationBanner(notificationsEnabled = false, forceShow = false, lastShownMillis = 0L, nowMillis = now)
        )
    }

    @Test
    fun `nao aparece de novo antes de uma semana`() {
        val sixDaysAgo = now - 6 * 24 * 60 * 60 * 1000L
        assertFalse(
            shouldShowNotificationBanner(notificationsEnabled = false, forceShow = false, lastShownMillis = sixDaysAgo, nowMillis = now)
        )
    }

    @Test
    fun `aparece de novo apos uma semana`() {
        val eightDaysAgo = now - 8 * 24 * 60 * 60 * 1000L
        assertTrue(
            shouldShowNotificationBanner(notificationsEnabled = false, forceShow = false, lastShownMillis = eightDaysAgo, nowMillis = now)
        )
    }

    @Test
    fun `forceShow ignora o throttle mesmo com notificacoes ativadas`() {
        assertTrue(
            shouldShowNotificationBanner(notificationsEnabled = true, forceShow = true, lastShownMillis = now, nowMillis = now)
        )
    }
}
