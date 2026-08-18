package com.lpstudio.bolaodagalera

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.gms.ads.MobileAds

import com.lpstudio.bolaodagalera.util.AdManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        
        // Inicializa o AdMob
        MobileAds.initialize(this) {}
        
        // Provê a atividade para o AdManager
        AdManager.init(this)

        setContent {
            SystemAppearance(isDark = true)
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}