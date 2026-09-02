package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Bolao(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    // Structure used for automatic point calculation
    val pointsExactScore: Int = 3,
    val pointsWinnerOrDraw: Int = 1,
    val code: String = "",
    val ownerId: String = "",
    val participants: List<String> = emptyList(),
    val pendingParticipants: List<String> = emptyList(),
    val pendingExits: List<String> = emptyList(),
    val championshipId: String = "UNKNOWN",
    val scope: BolaoScope = BolaoScope.FULL,
    val specificMatchId: String? = null,
    val createdAtMillis: Long = 0L,
    val deletedAtMillis: Long? = null
)

enum class BolaoScope(val label: String) {
    FULL("Grupos + Mata-mata"),
    ONLY_GROUPS("Apenas Grupos"),
    ONLY_KNOCKOUT("Apenas Mata-mata"),
    PONTOS_CORRIDOS("Pontos Corridos")
}
