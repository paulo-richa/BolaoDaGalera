package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.Match
import com.lpstudio.bolaodagalera.domain.model.Phase
import com.lpstudio.bolaodagalera.util.TimeSource

/** A championship phase is offered for a new pool only if none of its matches have kicked off yet. */
class CheckPhaseAvailabilityUseCase {
    operator fun invoke(allMatches: List<Match>, championshipId: String, phase: Phase): Boolean {
        val matches = allMatches.filter { it.championshipId == championshipId && it.phase == phase }
        if (matches.isEmpty()) return false

        val now = TimeSource.nowMillis()
        return matches.all { it.matchDateMillis > now }
    }
}
