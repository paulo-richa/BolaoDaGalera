package com.lpstudio.bolaodagalera

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

@Composable
expect fun SystemAppearance(isDark: Boolean)

@Composable
expect fun rememberLauncherProvider(): LauncherProvider

@Composable
expect fun CommonBackHandler(enabled: Boolean = true, onBack: () -> Unit)

interface LauncherProvider {
    fun shareText(text: String)
    fun sendEmail(address: String, subject: String, body: String)
    fun sendWhatsApp(phone: String, text: String)
}
