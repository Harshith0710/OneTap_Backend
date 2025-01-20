package com.example.geminiapikey

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.geminiapikey.ui.theme.GeminiAPIKeyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isIntroShown = sharedPreferences.getBoolean("isIntroShown", false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!isIntroShown) {
            // If intro hasn't been shown, navigate to IntroActivity
            setContent {
                var keepSplashScreeOn by remember { mutableStateOf(true) }
                splashScreen.setKeepOnScreenCondition { keepSplashScreeOn }
                GeminiAPIKeyTheme {
                    val context = LocalContext.current
                    GetStartedScreen(context)
                    LaunchedEffect(key1 = true){
                        keepSplashScreeOn = false
                    }
                }
            }
        } else {
            // Proceed to main app logic
            startActivity(Intent(this,HomeActivity::class.java))
        }
    }
}