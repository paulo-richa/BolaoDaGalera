package com.lpstudio.bolaodagalera.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

/**
 * Generates a Baseline Profile (`baseline-prof.txt`) covering app startup and the initial
 * screen reached before login, so AOT-compiles the classes exercised on a cold start instead of
 * leaving them to be JIT-interpreted on the user's first launch after install/update. Run via
 * `./gradlew :baselineprofile:generateBaselineProfile` on a connected device/emulator (API 28+).
 *
 * Deliberately stops at the login screen (doesn't sign in) - this generator has no dependency
 * on a seeded test account, and app startup + first-screen render is where cold-start latency
 * actually matters most; signed-in flows can get their own targeted profile later if needed.
 */
class BaselineProfileGenerator {
    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun generate() = baselineProfileRule.collect(
        packageName = "com.lpstudio.bolaodagalera"
    ) {
        startActivityAndWait()
        device.wait(Until.hasObject(By.pkg(packageName).depth(0)), STARTUP_TIMEOUT_MILLIS)
    }

    private companion object {
        private const val STARTUP_TIMEOUT_MILLIS = 5_000L
    }
}
