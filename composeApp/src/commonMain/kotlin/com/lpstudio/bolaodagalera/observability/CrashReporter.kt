package com.lpstudio.bolaodagalera.observability

/**
 * Reporta falhas inesperadas (não-fatais) pra investigação depois - erro de
 * rede, dado corrompido, etc. Não é pra todo catch: fluxos esperados
 * (validação, "já existe") só devem logar local, sem poluir o painel.
 * No iOS ainda não temos Crashlytics configurado (mesma decisão do FCM:
 * Android primeiro), então createCrashReporter() devolve um no-op lá.
 */
interface CrashReporter {
    fun recordException(throwable: Throwable, message: String? = null)

    fun log(message: String)

    fun setUserId(userId: String?)
}

expect fun createCrashReporter(): CrashReporter
