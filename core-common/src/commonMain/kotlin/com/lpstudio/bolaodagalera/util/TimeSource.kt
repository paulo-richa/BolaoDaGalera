package com.lpstudio.bolaodagalera.util

expect object TimeSource {
    fun nowMillis(): Long
}
