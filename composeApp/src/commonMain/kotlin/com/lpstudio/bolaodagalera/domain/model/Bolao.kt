package com.lpstudio.bolaodagalera.domain.model

import androidx.compose.runtime.Immutable

@Immutable
data class Bolao(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    // Estrutura para cálculo automático
    val pointsExactScore: Int = 3, // Ex: Acertou 2x1 exatamente
    val pointsWinnerOrDraw: Int = 1, // Ex: Apostou 1x0, jogo foi 2x1 (acertou o vencedor)
    val code: String = "",
    val ownerId: String = "",
    val participants: List<String> = emptyList(),
    val pendingParticipants: List<String> = emptyList(), // Usuários aguardando aprovação para entrar
    val pendingExits: List<String> = emptyList(), // Usuários aguardando aprovação para sair
    val championshipId: String = "UNKNOWN", // Carregado dinamicamente
    val scope: BolaoScope = BolaoScope.FULL,
    val specificMatchId: String? = null,
    val createdAtMillis: Long = 0L,
    val deletedAtMillis: Long? = null, // Se preenchido, o bolão está marcado para deleção
)

enum class BolaoScope(val label: String) {
    FULL("Grupos + Mata-mata"),
    ONLY_GROUPS("Apenas Grupos"),
    ONLY_KNOCKOUT("Apenas Mata-mata"),
    PONTOS_CORRIDOS("Pontos Corridos"),
}
