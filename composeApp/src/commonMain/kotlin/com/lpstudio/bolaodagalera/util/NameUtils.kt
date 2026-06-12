package com.lpstudio.bolaodagalera.util

fun String.getInitials(): String {
    val names = this.trim().split(" ").filter { it.isNotBlank() }
    return when {
        names.isEmpty() -> "?"
        names.size == 1 -> names[0].take(1).uppercase()
        else -> (names[0].take(1) + names[1].take(1)).uppercase()
    }
}
