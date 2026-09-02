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
    // Código na API (ex: BSA, CLI)
    val apiCode: String = "",
    // Se exibe a aba de "Tabela"
    val hasStandings: Boolean = false,
    // Se é formato "Pontos Corridos" (ex: Brasileirão)
    val isPointsBased: Boolean = false,
    // Se tem fase de grupos seguida de mata-mata (ex: Libertadores)
    val isGroupsAndKnockout: Boolean = false,
    // Se o mata-mata tem jogos de ida e volta
    val isTwoLegged: Boolean = false,
    // Se está liberado para criação de novos bolões
    val isAvailable: Boolean = true
) {
    companion object {
        // Fallback genérico para quando os dados ainda não foram carregados
        val DEFAULT =
            Championship(
                id = "UNKNOWN",
                displayName = "Carregando...",
                emoji = "⌛",
                isAvailable = false
            )

        // Cache local para busca rápida por ID (Sincronizado pelo Repository)
        // Usamos State para que as UIs recomponham automaticamente quando o cache for preenchido
        private val _cachedChampionships = androidx.compose.runtime.mutableStateOf(listOf<Championship>())
        val cachedChampionships: List<Championship> get() = _cachedChampionships.value

        fun setCache(list: List<Championship>) {
            _cachedChampionships.value = list
        }

        fun fromId(id: String?): Championship = cachedChampionships.find { it.id == id } ?: DEFAULT.copy(id = id ?: "UNKNOWN")

        fun getAll(): List<Championship> = cachedChampionships
    }
}
