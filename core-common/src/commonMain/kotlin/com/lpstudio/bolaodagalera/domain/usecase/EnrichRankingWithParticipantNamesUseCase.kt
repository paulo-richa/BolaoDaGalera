package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.RankingEntry
import com.lpstudio.bolaodagalera.domain.model.User

/**
 * Backfills a ranking entry's display name/nickname from the resolved [User] once
 * it's known, replacing the placeholder name a participant is created with
 * (a user can join a pool - and thus appear in the ranking - before their user
 * document is readable, e.g. during an admin-side direct add).
 */
class EnrichRankingWithParticipantNamesUseCase {
    operator fun invoke(ranking: List<RankingEntry>, users: List<User>): List<RankingEntry> {
        val userMap = users.associateBy { it.id }
        return ranking.map { entry ->
            val user = userMap[entry.userId]
            val isGenericName =
                entry.userName.isBlank() ||
                    entry.userName == "Novo Participante" ||
                    entry.userName == "Usuário"
            if (user != null && isGenericName) {
                entry.copy(userName = user.name, userNickname = user.nickname.ifBlank { user.username })
            } else {
                entry
            }
        }
    }
}
