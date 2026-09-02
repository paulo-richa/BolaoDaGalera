package com.lpstudio.bolaodagalera

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.net.toUri

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    // Android handle system appearance via theme
}

@Composable
actual fun rememberLauncherProvider(): LauncherProvider {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember {
        object : LauncherProvider {
            override fun shareText(text: String) {
                val intent =
                    android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, text)
                    }
                context.startActivity(android.content.Intent.createChooser(intent, null))
            }

            override fun sendEmail(address: String, subject: String, body: String) {
                val intent =
                    android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(android.content.Intent.EXTRA_EMAIL, arrayOf(address))
                        putExtra(android.content.Intent.EXTRA_SUBJECT, subject)
                        putExtra(android.content.Intent.EXTRA_TEXT, body)
                    }
                context.startActivity(intent)
            }

            override fun sendWhatsApp(phone: String, text: String) {
                val intent =
                    android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                        data = "https://wa.me/$phone?text=${android.net.Uri.encode(text)}".toUri()
                    }
                context.startActivity(intent)
            }
        }
    }
}

@Composable
actual fun CommonBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled, onBack)
}
