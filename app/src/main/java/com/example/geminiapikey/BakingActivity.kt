package com.example.geminiapikey

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class BakingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BakingScreen(this)
        }
    }
}