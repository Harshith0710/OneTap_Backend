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
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isIntroShown = sharedPreferences.getBoolean("isIntroShown", false)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)
        setContent {
            var keepSplashScreeOn by remember { mutableStateOf(true) }
            splashScreen.setKeepOnScreenCondition { keepSplashScreeOn }
            GeminiAPIKeyTheme {
                val context = LocalContext.current

                LaunchedEffect(key1 = true) {
                    delay(1000) // Adjust delay as needed
                    keepSplashScreeOn = false

                    if (!isIntroShown) {
                        // If intro hasn't been shown, navigate to IntroActivity
                        context.startActivity(Intent(context, GetStartedActivity::class.java))
                    }
                    else {
                        // Proceed to main app logic
                        context.startActivity(Intent(context, LoginActivity::class.java))
                        finish() // Finish MainActivity
                    }
                }
            }
        }
    }
}