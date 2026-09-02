package com.lpstudio.bolaodagalera.domain.model

import kotlinx.serialization.Serializable

/**
 * Defines the behavior and display rules for each supported championship.
 * Now loaded dynamically via Firestore.
 */
@Serializable
data class Championship(
    val id: String = "",
    val displayName: String = "",
    val emoji: String = "",
    // Code used by the API (e.g. BSA, CLI)
    val apiCode: String = "",
    // Whether to show the "Standings" tab
    val hasStandings: Boolean = false,
    // Whether it's a "league table" format (e.g. Brasileirão)
    val isPointsBased: Boolean = false,
    // Whether it has a group stage followed by knockout (e.g. Libertadores)
    val isGroupsAndKnockout: Boolean = false,
    // Whether the knockout stage has two-legged ties
    val isTwoLegged: Boolean = false,
    // Whether new pools can be created for it
    val isAvailable: Boolean = true
) {
    companion object {
        // Generic fallback for when data hasn't loaded yet
        val DEFAULT =
            Championship(
                id = "UNKNOWN",
                displayName = "Carregando...",
                emoji = "⌛",
                isAvailable = false
            )

        // Local cache for fast lookup by ID (synchronized by the Repository)
        // Uses State so UIs automatically recompose when the cache is populated
        private val _cachedChampionships = androidx.compose.runtime.mutableStateOf(listOf<Championship>())
        val cachedChampionships: List<Championship> get() = _cachedChampionships.value

        fun setCache(list: List<Championship>) {
            _cachedChampionships.value = list
        }

        fun fromId(id: String?): Championship = cachedChampionships.find { it.id == id } ?: DEFAULT.copy(id = id ?: "UNKNOWN")

        fun getAll(): List<Championship> = cachedChampionships
    }
}
