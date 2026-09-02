package com.lpstudio.bolaodagalera

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.net.toUri
import androidx.core.view.WindowCompat

actual fun getPlatform(): Platform = object : Platform {
    override val name: String = "Android ${android.os.Build.VERSION.SDK_INT}"
}

@Composable
actual fun SystemAppearance(isDark: Boolean) {
    // enableEdgeToEdge() picks status/navigation bar icon color from the system's
    // light/dark setting, not from the app's own theme - since this app is always
    // dark regardless of the device's setting, that default leaves dark (near-invisible)
    // icons on a dark background whenever the device itself is in light mode. Force
    // light (white) icons here instead, matching the app's actual background color.
    val view = LocalView.current
    if (view.isInEditMode) return
    SideEffect {
        val window = (view.context as? android.app.Activity)?.window ?: return@SideEffect
        val controller = WindowCompat.getInsetsController(window, view)
        controller.isAppearanceLightStatusBars = !isDark
        controller.isAppearanceLightNavigationBars = !isDark
    }
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
