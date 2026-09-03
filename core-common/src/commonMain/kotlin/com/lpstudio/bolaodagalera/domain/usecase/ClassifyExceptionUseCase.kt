package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.ErrorCategory

/**
 * Classifies a caught exception by matching keywords in its message (Firebase Auth/Firestore
 * error strings aren't typed, so this is a best-effort heuristic) into a coarse [ErrorCategory].
 * Callers map each category to their own screen-specific user-facing message; the classification
 * itself is shared so every screen groups the same underlying causes the same way.
 */
class ClassifyExceptionUseCase {
    private val keywordsByCategory: List<Pair<ErrorCategory, List<String>>> = listOf(
        ErrorCategory.INVALID_CREDENTIALS to listOf("incorrect", "invalid-credential", "password", "wrong"),
        ErrorCategory.USER_NOT_FOUND to listOf("user-not-found", "no user"),
        ErrorCategory.NOT_FOUND to listOf("não encontrado", "not found", "not-found"),
        ErrorCategory.ALREADY_IN_USE to listOf("email-already", "email já", "collision", "already-in-use"),
        ErrorCategory.NETWORK to listOf("network", "connection", "timeout"),
        ErrorCategory.RATE_LIMITED to listOf("too many requests", "blocked"),
        ErrorCategory.WEAK_PASSWORD to listOf("weak-password"),
        ErrorCategory.INVALID_EMAIL to listOf("invalid-email"),
        ErrorCategory.PERMISSION to listOf("permission", "permissão")
    )

    operator fun invoke(e: Exception): ErrorCategory {
        val msg = e.message?.lowercase() ?: ""
        return keywordsByCategory
            .firstOrNull { (_, keywords) -> keywords.any { msg.contains(it) } }
            ?.first
            ?: ErrorCategory.UNKNOWN
    }
}
