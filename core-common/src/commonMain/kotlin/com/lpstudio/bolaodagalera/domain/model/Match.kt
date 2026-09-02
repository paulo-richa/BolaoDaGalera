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
     * A match is considered "stuck" if it isn't finished but its date is more than 48h in the past.
     * This usually indicates an API error or a postponed match that wasn't updated.
     */
    fun isStuck(now: Long): Boolean = !isFinished && now > (matchDateMillis + 48 * 3600_000L)

    /** The API / automatic bracket advancement hasn't set a date for this match yet. */
    val hasNoConfirmedDate: Boolean get() = matchDateMillis == NO_DATE_MILLIS

    fun groupRound(): Int {
        // Priority 1: Brasileirão ID (more reliable than the 'group' field coming from the API)
        if (id.contains("-R")) {
            val part = id.substringAfter("-R").substringBefore("-")
            val r = part.toIntOrNull()
            if (r != null) return r
        }

        // Priority 2: Group field (legacy or fallback)
        if (group?.startsWith("Rodada ") == true) {
            return group.substringAfter("Rodada ").toIntOrNull() ?: 0
        }

        // Legacy (GS-A-1, GS-B-3, etc)
        // Each group has 6 matches (3 rounds of 2 matches each)
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
         * Sentinel for "no confirmed date yet" (e.g. a knockout match whose
         * team advanced but the API hasn't published the fixture date yet).
         * Uses a value far in the future (instead of 0/epoch) so that
         * "already past / is live / is stuck" comparisons don't treat the
         * match as if it had already occurred in 1970, and so that sorting
         * by date keeps these matches after ones that already have a real
         * date, without overflowing sums like matchDateMillis + 48h.
         */
        const val NO_DATE_MILLIS = 9_999_999_999_999L
    }
}
