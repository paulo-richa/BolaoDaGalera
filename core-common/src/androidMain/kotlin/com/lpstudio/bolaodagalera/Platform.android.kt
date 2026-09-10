package com.lpstudio.bolaodagalera

import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

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

            override fun openNotificationSettings() {
                val intent =
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        android.content.Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        android.content.Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${context.packageName}".toUri())
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

@Composable
actual fun rememberAreNotificationsEnabled(): Boolean {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return enabled
}

private const val PREFS_NAME = "bolao_local_prefs"
private const val KEY_NOTIFICATION_BANNER_LAST_SHOWN_MILLIS = "notification_banner_last_shown_millis"

@Composable
actual fun rememberNotificationBannerPrefs(): NotificationBannerPrefs {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE) }
    var lastShownMillis by remember { mutableStateOf(prefs.getLong(KEY_NOTIFICATION_BANNER_LAST_SHOWN_MILLIS, 0L)) }

    return remember(lastShownMillis) {
        NotificationBannerPrefs(
            lastShownMillis = lastShownMillis,
            markShown = {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_NOTIFICATION_BANNER_LAST_SHOWN_MILLIS, now).apply()
                lastShownMillis = now
            }
        )
    }
}
