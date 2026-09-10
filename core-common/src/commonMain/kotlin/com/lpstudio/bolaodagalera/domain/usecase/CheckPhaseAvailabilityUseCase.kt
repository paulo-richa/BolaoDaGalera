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
 *
 * [strictMode] (from [com.lpstudio.bolaodagalera.featureflags.FeatureFlagsProvider.phaseAvailabilityStrictMode],
 * a Remote Config flag) reverts to the older, stricter "every match must still be in the future"
 * rule when true - a kill-switch so this business rule can be rolled back without shipping a new
 * app version.
 */
class CheckPhaseAvailabilityUseCase {
    operator fun invoke(allMatches: List<Match>, championshipId: String, phase: Phase, strictMode: Boolean = false): Boolean {
        val matches = allMatches.filter { it.championshipId == championshipId && it.phase == phase }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        return if (strictMode) matches.all { it.matchDateMillis > now } else matches.any { it.matchDateMillis > now }
    }
}
