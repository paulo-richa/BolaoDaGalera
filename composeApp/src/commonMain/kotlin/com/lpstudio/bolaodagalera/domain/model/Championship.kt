package com.lpstudio.bolaodagalera.domain.model

/**
 * Define o comportamento e as regras de exibição de cada campeonato suportado.
 */
enum class Championship(
    val id: String,
    val displayName: String,
    val emoji: String,
    val hasStandings: Boolean,      // Se exibe a aba de "Tabela"
    val isPointsBased: Boolean,    // Se é formato "Pontos Corridos" (ex: Brasileirão)
    val isGroupsAndKnockout: Boolean // Se tem fase de grupos seguida de mata-mata (ex: Copa)
) {
    COPA_2026(
        id = "COPA_2026",
        displayName = "Copa do Mundo 2026",
        emoji = "🏆",
        hasStandings = false,
        isPointsBased = false,
        isGroupsAndKnockout = true
    ),
    BRASILEIRAO(
        id = "BRASILEIRAO",
        displayName = "Brasileirão 2026",
        emoji = "🇧🇷",
        hasStandings = true,
        isPointsBased = true,
        isGroupsAndKnockout = false
    ),
    LIBERTADORES(
        id = "LIBERTADORES",
        displayName = "Libertadores 2026",
        emoji = "🔥",
        hasStandings = true,
        isPointsBased = false,
        isGroupsAndKnockout = true
    ),
    COPA_BRASIL(
        id = "COPA_BRASIL",
        displayName = "Copa do Brasil 2026",
        emoji = "⚔️",
        hasStandings = false,
        isPointsBased = false,
        isGroupsAndKnockout = false // Apenas mata-mata
    ),
    AMISTOSOS(
        id = "AMISTOSOS",
        displayName = "Amistosos",
        emoji = "⚽",
        hasStandings = false,
        isPointsBased = false,
        isGroupsAndKnockout = false
    );

    companion object {
        fun fromId(id: String?): Championship = entries.find { it.id == id } ?: COPA_2026
    }
}
