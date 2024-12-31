package com.example.geminiapikey

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.LocalContext

class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeScreen{ action ->
                handleActionButtonClick(action)
            }
        }
    }
    private fun handleActionButtonClick(action: String) {
        val intent = when (action) {
            "Chat with Bot" -> Intent(this, BakingActivity::class.java)
            "Talk with Bot" -> Intent(this, SpeakActivity::class.java)
            "Search by Image" -> Intent(this, BakingActivity::class.java)
            else -> null
        }
        intent?.let { startActivity(it) }
    }
}

