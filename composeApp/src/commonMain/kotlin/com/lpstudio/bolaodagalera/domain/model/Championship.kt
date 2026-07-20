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
    val hasStandings: Boolean = false,      // Se exibe a aba de "Tabela"
    val isPointsBased: Boolean = false,    // Se é formato "Pontos Corridos" (ex: Brasileirão)
    val isGroupsAndKnockout: Boolean = false, // Se tem fase de grupos seguida de mata-mata (ex: Libertadores)
    val isAvailable: Boolean = true         // Se está liberado para criação de novos bolões
) {
    companion object {
        // Fallback para quando os dados ainda não foram carregados ou em caso de erro
        val DEFAULT = Championship(
            id = "LIBERTADORES",
            displayName = "Libertadores 2026",
            emoji = "🔥",
            hasStandings = true,
            isPointsBased = false,
            isGroupsAndKnockout = true
        )

        // Cache local para busca rápida por ID (Sincronizado pelo Repository)
        private var cachedChampionships = listOf<Championship>()

        fun setCache(list: List<Championship>) {
            cachedChampionships = list
        }

        fun fromId(id: String?): Championship {
            return cachedChampionships.find { it.id == id } ?: when(id) {
                "BRASILEIRAO" -> Championship(
                    id = "BRASILEIRAO",
                    displayName = "Brasileirão 2026",
                    emoji = "🇧🇷",
                    hasStandings = true,
                    isPointsBased = true,
                    isGroupsAndKnockout = false
                )
                "COPA_BRASIL" -> Championship(
                    id = "COPA_BRASIL",
                    displayName = "Copa do Brasil 2026",
                    emoji = "⚔️",
                    hasStandings = false,
                    isPointsBased = false,
                    isGroupsAndKnockout = false
                )
                "AMISTOSOS" -> Championship(
                    id = "AMISTOSOS",
                    displayName = "Amistosos",
                    emoji = "⚽",
                    hasStandings = false,
                    isPointsBased = false,
                    isGroupsAndKnockout = false
                )
                else -> DEFAULT
            }
        }

        fun getAll(): List<Championship> = cachedChampionships.ifEmpty { 
            listOf(
                Championship(
                    id = "BRASILEIRAO",
                    displayName = "Brasileirão 2026",
                    emoji = "🇧🇷",
                    hasStandings = true,
                    isPointsBased = true,
                    isGroupsAndKnockout = false
                ),
                DEFAULT
            )
        }
    }
}
