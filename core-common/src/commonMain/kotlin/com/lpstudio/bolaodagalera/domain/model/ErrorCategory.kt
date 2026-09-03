package com.lpstudio.bolaodagalera.domain.model

/** Coarse classification of a caught exception, used to pick a user-facing message per screen. */
enum class ErrorCategory {
    INVALID_CREDENTIALS,
    USER_NOT_FOUND,
    NOT_FOUND,
    ALREADY_IN_USE,
    NETWORK,
    RATE_LIMITED,
    WEAK_PASSWORD,
    INVALID_EMAIL,
    PERMISSION,
    UNKNOWN
}
