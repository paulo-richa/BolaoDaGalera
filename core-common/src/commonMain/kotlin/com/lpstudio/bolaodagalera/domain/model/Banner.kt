package com.lpstudio.bolaodagalera.domain.model

import kotlinx.serialization.Serializable

/**
 * A marketing banner shown in the Home carousel, promoting a championship
 * users can create a pool for. Managed manually via the Firestore console
 * (no admin UI) - order/isActive control the carousel's content and
 * position directly, independent of the Remote Config flag that only
 * toggles the whole carousel on/off.
 */
@Serializable
data class Banner(
    val id: String = "",
    val imageUrl: String = "",
    // Not used for navigation yet (banner click always opens Create Pool
    // plainly) - kept for analytics and as a natural extension point.
    val championshipId: String? = null,
    val order: Int = 0,
    val isActive: Boolean = true,
    val title: String? = null,
    val subtitle: String? = null
)
