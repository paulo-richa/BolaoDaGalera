package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Bolao(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    // Estrutura para cálculo automático
    val pointsExactScore: Int = 3,      // Ex: Acertou 2x1 exatamente
    val pointsWinnerOrDraw: Int = 1,    // Ex: Apostou 1x0, jogo foi 2x1 (acertou o vencedor)
    val code: String = "",
    val ownerId: String = "",
    val participants: List<String> = emptyList(),
    val pendingParticipants: List<String> = emptyList(), // Usuários aguardando aprovação
    val championshipId: String = "COPA_2026", // "COPA_2026" ou "AMISTOSOS"
    val scope: BolaoScope = BolaoScope.FULL,
    val specificMatchId: String? = null,
    val createdAtMillis: Long = 0L
)

enum class BolaoScope(val label: String) {
    FULL("Grupos + Mata-mata"),
    ONLY_GROUPS("Apenas Grupos"),
    ONLY_KNOCKOUT("Apenas Mata-mata"),
    ONLY_BRAZIL("Apenas Jogos do Brasil")
}
