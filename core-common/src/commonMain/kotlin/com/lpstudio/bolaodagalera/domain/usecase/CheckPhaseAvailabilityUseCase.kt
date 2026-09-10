package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.util.TimeSource

/**
 * A championship phase is offered for a new pool as long as at least one of its matches hasn't
 * kicked off yet - unlike a single-window group draw (a handful of matches all played in a short
 * span, where the phase is only meaningful before any of them start), a league-style phase like
 * Champions League's current 36-team format (Phase.GROUP_STAGE, but really 8 rounds spread over
 * months, same shape as Brasileirão's rounds) stays meaningful for the rounds still ahead even
 * after earlier ones are done - matching [CheckKnockoutAvailabilityUseCase]'s same reasoning for
 * the knockout stage.
 */
class CheckPhaseAvailabilityUseCase {
    operator fun invoke(allMatches: List<Match>, championshipId: String, phase: Phase): Boolean {
        val matches = allMatches.filter { it.championshipId == championshipId && it.phase == phase }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        return matches.any { it.matchDateMillis > now }
    }
}
