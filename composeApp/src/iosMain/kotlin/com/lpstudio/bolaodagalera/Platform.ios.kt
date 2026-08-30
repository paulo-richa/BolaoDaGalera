package com.lpstudio.bolaodagalera

import androidx.compose.runtime.Composable
import platform.UIKit.UIDevice

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    // iOS system appearance logic
}

@Composable
actual fun rememberLauncherProvider(): LauncherProvider = object : LauncherProvider {
    override fun shareText(text: String) {
        // Share logic for iOS
    }

    override fun sendEmail(address: String, subject: String, body: String) {
        // Email logic for iOS
    }

    override fun sendWhatsApp(phone: String, text: String) {
        // WhatsApp logic for iOS
    }
}

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // No back button on iOS
}
