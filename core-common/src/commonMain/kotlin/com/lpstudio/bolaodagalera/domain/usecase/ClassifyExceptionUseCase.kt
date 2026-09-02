package com.lpstudio.bolaodagalera.domain.usecase

import com.lpstudio.bolaodagalera.domain.model.ErrorCategory

/**
 * Classifies a caught exception by matching keywords in its message (Firebase Auth/Firestore
 * error strings aren't typed, so this is a best-effort heuristic) into a coarse [ErrorCategory].
 * Callers map each category to their own screen-specific user-facing message; the classification
 * itself is shared so every screen groups the same underlying causes the same way.
 */
class ClassifyExceptionUseCase {
    operator fun invoke(e: Exception): ErrorCategory {
        val msg = e.message?.lowercase() ?: ""
        return when {
            msg.contains("incorrect") || msg.contains("invalid-credential") || msg.contains("password") || msg.contains("wrong") ->
                ErrorCategory.INVALID_CREDENTIALS
            msg.contains("user-not-found") || msg.contains("no user") -> ErrorCategory.USER_NOT_FOUND
            msg.contains("email-already") || msg.contains("email já") || msg.contains("collision") || msg.contains("already-in-use") ->
                ErrorCategory.ALREADY_IN_USE
            msg.contains("network") || msg.contains("connection") || msg.contains("timeout") -> ErrorCategory.NETWORK
            msg.contains("too many requests") || msg.contains("blocked") -> ErrorCategory.RATE_LIMITED
            msg.contains("weak-password") -> ErrorCategory.WEAK_PASSWORD
            msg.contains("invalid-email") -> ErrorCategory.INVALID_EMAIL
            msg.contains("permission") || msg.contains("permissão") -> ErrorCategory.PERMISSION
            else -> ErrorCategory.UNKNOWN
        }
    }
}
