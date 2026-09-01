package com.lpstudio.bolaodagalera.observability

/**
 * Mede a duração de operações de negócio críticas (login, criar/entrar em
 * bolão, salvar palpite etc.) via Firebase Performance Monitoring no
 * Android. No iOS ainda é um no-op, mas a interface já é KMP-pronta.
 */
interface PerformanceMonitor {
    suspend fun <T> trace(name: String, block: suspend () -> T): T
}

expect fun createPerformanceMonitor(): PerformanceMonitor
