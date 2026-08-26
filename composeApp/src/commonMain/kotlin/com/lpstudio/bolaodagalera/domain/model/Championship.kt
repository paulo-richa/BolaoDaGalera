package com.lpstudio.bolaodagalera.domain.model

import kotlinx.serialization.Serializable

/**
 * Define o comportamento e as regras de exibição de cada campeonato suportado.
 * Agora carregado dinamicamente via Firestore.
 */
@Serializable
data class Championship(
    val id: String = "",
    val displayName: String = "",
    val emoji: String = "",
    val apiCode: String = "", // Código na API (ex: BSA, CLI)
    val hasStandings: Boolean = false, // Se exibe a aba de "Tabela"
    val isPointsBased: Boolean = false, // Se é formato "Pontos Corridos" (ex: Brasileirão)
    val isGroupsAndKnockout: Boolean = false, // Se tem fase de grupos seguida de mata-mata (ex: Libertadores)
    val isTwoLegged: Boolean = false, // Se o mata-mata tem jogos de ida e volta
    val isAvailable: Boolean = true, // Se está liberado para criação de novos bolões
) {
    companion object {
        // Fallback genérico para quando os dados ainda não foram carregados
        val DEFAULT =
            Championship(
                id = "UNKNOWN",
                displayName = "Carregando...",
                emoji = "⌛",
                isAvailable = false,
            )

        // Cache local para busca rápida por ID (Sincronizado pelo Repository)
        private var cachedChampionships = listOf<Championship>()

        fun setCache(list: List<Championship>) {
            cachedChampionships = list
        }

        fun fromId(id: String?): Championship {
            return cachedChampionships.find { it.id == id } ?: DEFAULT.copy(id = id ?: "UNKNOWN")
        }

        fun getAll(): List<Championship> = cachedChampionships
    }
}
