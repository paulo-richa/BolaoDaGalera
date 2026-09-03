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
    fun isStuck(now: Long): Boolean = !isFinished && now > (matchDateMillis + STUCK_THRESHOLD_MILLIS)

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

        // Legacy (GS-A-1, GS-B-3, etc) - each group has 3 rounds of MATCHES_PER_ROUND
        // matches each, so match number n falls in round ceil(n / MATCHES_PER_ROUND).
        val n = id.substringAfterLast("-").toIntOrNull() ?: return 0
        if (n !in 1..LEGACY_GROUP_MATCH_COUNT) return 0
        return (n + MATCHES_PER_ROUND - 1) / MATCHES_PER_ROUND
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

        private const val HOURS_TO_MILLIS = 3_600_000L
        private const val STUCK_THRESHOLD_HOURS = 48L
        const val STUCK_THRESHOLD_MILLIS = STUCK_THRESHOLD_HOURS * HOURS_TO_MILLIS

        private const val MATCHES_PER_ROUND = 2
        private const val LEGACY_GROUP_MATCH_COUNT = 6
    }
}
