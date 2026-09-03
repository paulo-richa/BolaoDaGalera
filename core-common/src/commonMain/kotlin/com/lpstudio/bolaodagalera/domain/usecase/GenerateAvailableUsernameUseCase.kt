package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.repository.AuthRepository

/**
 * Derives a username suggestion from a full name during registration, trying a
 * handful of predictable candidates (first name, first+last, initials) before
 * falling back to a random numeric suffix, checking availability against
 * [AuthRepository] as it goes.
 */
class GenerateAvailableUsernameUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(fullName: String): String {
        val parts =
            fullName.trim().lowercase()
                .replace(Regex("[^a-z\\s]"), "") // Strip accents/symbols via simplified ASCII filtering
                .split(" ")
                .filter { it.length >= 2 }

        if (parts.isEmpty()) return ""

        val firstName = parts[0]
        val secondName = parts.getOrNull(1) ?: ""
        val lastPart = parts.last()

        val candidates = mutableListOf<String>()
        if (secondName.isNotEmpty()) {
            candidates.add(firstName + secondName)
            candidates.add(firstName + "." + secondName)
            candidates.add(firstName.take(1) + secondName)
        }
        candidates.add(firstName)
        if (parts.size >= MIN_PARTS_FOR_INITIALS_CANDIDATE) {
            candidates.add(firstName.take(1) + secondName.take(1) + lastPart)
        }

        // Try the predefined candidates first
        for (candidate in candidates) {
            try {
                if (!authRepository.isUsernameInUse(candidate)) return candidate
            } catch (ignored: Exception) {
                // Permission error while signed out: fall back to the first candidate as a suggestion
                return candidates.firstOrNull() ?: firstName
            }
        }

        // Fallback: append a random number when no predefined candidate is available
        return try {
            var finalCandidate: String
            var attempts = 0
            do {
                finalCandidate = firstName + kotlin.random.Random.nextInt(SUFFIX_RANGE_START, SUFFIX_RANGE_END).toString()
                attempts++
            } while (attempts < MAX_RANDOM_SUFFIX_ATTEMPTS && authRepository.isUsernameInUse(finalCandidate))
            finalCandidate
        } catch (ignored: Exception) {
            firstName + kotlin.random.Random.nextInt(SUFFIX_RANGE_START, SUFFIX_RANGE_END).toString()
        }
    }

    private companion object {
        private const val MIN_PARTS_FOR_INITIALS_CANDIDATE = 3
        private const val SUFFIX_RANGE_START = 100
        private const val SUFFIX_RANGE_END = 999
        private const val MAX_RANDOM_SUFFIX_ATTEMPTS = 5
    }
}
