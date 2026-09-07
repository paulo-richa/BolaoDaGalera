package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.util.TimeSource

/**
 * The knockout stage is offered for a new pool as long as at least one of its matches
 * hasn't kicked off yet - unlike the group stage (a single flat set of matches, so a
 * pool only makes sense before any of them start), the knockout stage is a sequence of
 * rounds (round of 16, quarterfinals, semifinals, final): once the earlier rounds are
 * done, a pool covering the still-upcoming rounds remains meaningful.
 */
class CheckKnockoutAvailabilityUseCase {
    operator fun invoke(allMatches: List<Match>, championshipId: String): Boolean {
        val matches =
            allMatches.filter {
                it.championshipId == championshipId &&
                    it.phase != Phase.GROUP_STAGE &&
                    it.phase != Phase.FRIENDLIES
            }
        // No knockout matches synced yet doesn't mean this championship has no
        // knockout stage - callers already gate this on
        // championship.isGroupsAndKnockout before asking, so an empty list here
        // just means the bracket isn't known yet (e.g. the Champions League's
        // league phase is still in progress). It will be filled in later by
        // the championship's own sync once the bracket is drawn, so it still
        // counts as "available" - otherwise a pool couldn't be scoped to the
        // knockout stage in advance, forcing whoever wants one to create a new
        // pool (or re-add participants) once matches finally appear.
        if (matches.isEmpty()) return true

        val now = TimeSource.nowMillis()
        return matches.any { it.matchDateMillis > now }
    }
}
