package com.lpstudio.bolaodagalera.util

actual object TimeSource {
    actual fun nowMillis(): Long = System.currentTimeMillis()
}
