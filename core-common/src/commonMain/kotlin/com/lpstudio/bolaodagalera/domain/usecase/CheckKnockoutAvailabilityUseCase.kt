package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.util.TimeSource

/** The knockout stage is offered for a new pool only if none of its matches have kicked off yet. */
class CheckKnockoutAvailabilityUseCase {
    operator fun invoke(allMatches: List<Match>, championshipId: String): Boolean {
        val matches =
            allMatches.filter {
                it.championshipId == championshipId &&
                    it.phase != Phase.GROUP_STAGE &&
                    it.phase != Phase.FRIENDLIES
            }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        return matches.all { it.matchDateMillis > now }
    }
}
