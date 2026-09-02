package com.lpstudio.bolaodagalera.observability

/**
 * Eventos de negócio (login, cadastro, criar/entrar em bolão, salvar
 * palpite) via Firebase Analytics no Android. No iOS ainda é um no-op,
 * mas a interface já é KMP-pronta.
 */
interface AnalyticsTracker {
    fun logEvent(name: String, params: Map<String, Any?> = emptyMap())
    fun setUserId(userId: String?)
}

expect fun createAnalyticsTracker(): AnalyticsTracker
