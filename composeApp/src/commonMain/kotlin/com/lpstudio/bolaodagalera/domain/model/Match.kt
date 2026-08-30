package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Match(
    val id: String,
    val homeTeam: String,
    val awayTeam: String,
    val homeTeamCode: String,
    val awayTeamCode: String,
    val homeTeamFlag: String,
    val awayTeamFlag: String,
    val homeTeamCrest: String? = null,
    val awayTeamCrest: String? = null,
    val matchDateMillis: Long,
    val phase: Phase,
    val group: String? = null,
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val status: String? = null,
    val championshipId: String = "UNKNOWN",
    val matchOrder: Int = 0,
    val isManual: Boolean = false
) {
    val isFinished: Boolean get() = status == "FINISHED"
    val isUpcoming: Boolean get() = status == "TIMED" || status == "SCHEDULED" || status == null

    /**
     * Um jogo é considerado "Preso" se não estiver finalizado mas sua data já passou há mais de 48h.
     * Isso geralmente indica erro na API ou jogo adiado que não foi atualizado.
     */
    fun isStuck(now: Long): Boolean = !isFinished && now > (matchDateMillis + 48 * 3600_000L)

    /** A API/avanço automático de chave ainda não definiu quando esse jogo acontece. */
    val hasNoConfirmedDate: Boolean get() = matchDateMillis == NO_DATE_MILLIS

    fun groupRound(): Int {
        // Prioridade 1: ID do Brasileirão (Mais confiável que o campo 'group' vindo da API)
        if (id.contains("-R")) {
            val part = id.substringAfter("-R").substringBefore("-")
            val r = part.toIntOrNull()
            if (r != null) return r
        }

        // Prioridade 2: Campo Group (Legado ou Fallback)
        if (group?.startsWith("Rodada ") == true) {
            return group.substringAfter("Rodada ").toIntOrNull() ?: 0
        }

        // Legado (GS-A-1, GS-B-3, etc)
        // Cada grupo tem 6 jogos (3 rodadas de 2 jogos cada)
        val n = id.substringAfterLast("-").toIntOrNull() ?: return 0
        return when (n) {
            1, 2 -> 1
            3, 4 -> 2
            5, 6 -> 3
            else -> 0
        }
    }

    companion object {
        /**
         * Sentinela para "sem data confirmada ainda" (ex.: mata-mata cujo
         * time avançou mas a API ainda não publicou a data do confronto).
         * Usa um valor bem distante no futuro (em vez de 0/epoch) para que
         * as comparações de "já passou / está ao vivo / está travado" não
         * tratem o jogo como se já tivesse ocorrido em 1970, e para que a
         * ordenação por data mantenha esses jogos depois dos que já têm
         * data real, sem overflow em somas como matchDateMillis + 48h.
         */
        const val NO_DATE_MILLIS = 9_999_999_999_999L
    }
}
