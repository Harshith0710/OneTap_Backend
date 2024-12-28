package com.example.geminiapikey

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
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
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
    }
}