package com.lpstudio.bolaodagalera

/**
 * iOS configuration overrides
 * iOS uses fake data to avoid Koin compatibility issues with lifecycle-viewmodel-compose
 */
actual val USE_FAKE_DATA: Boolean = true
actual val USE_OPEN_FOOTBALL: Boolean = false

