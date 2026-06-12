package com.lpstudio.bolaodagalera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun SystemAppearance(isDark: Boolean) {
}

@Composable
actual fun rememberLauncherProvider(): LauncherProvider {
    // ... (unchanged)
    return remember {
        object : LauncherProvider {
            override fun shareText(text: String) {
                val activityViewController = UIActivityViewController(
                    activityItems = listOf(text),
                    applicationActivities = null
                )
                UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(
                    activityViewController,
                    animated = true,
                    completion = null
                )
            }

            override fun sendEmail(address: String, subject: String, body: String) {
                val urlString = "mailto:$address?subject=${subject.encodeUri()}&body=${body.encodeUri()}"
                val url = NSURL.URLWithString(urlString)
                if (url != null) {
                    UIApplication.sharedApplication.openURL(url)
                }
            }

            override fun sendWhatsApp(phone: String, text: String) {
                val cleanPhone = phone.filter { it.isDigit() }
                val urlString = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${text.encodeUri()}"
                val url = NSURL.URLWithString(urlString)
                if (url != null) {
                    UIApplication.sharedApplication.openURL(url)
                }
            }
        }
    }
}

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS doesn't have a system back button like Android
}

private fun String.encodeUri(): String {
    return this.replace(" ", "%20") // Simplificação para o exemplo
        .replace("\n", "%0A")
}
