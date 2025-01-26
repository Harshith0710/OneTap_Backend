package com.example.geminiapikey

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class BakingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Retrieve the image bitmap from the intent
        val imageBitmap = intent.getParcelableExtra<Bitmap>("image")

        // Retrieve the conversations list from the intent
        val conversations = intent.getStringArrayListExtra("conversations") ?: emptyList()
        val prompt = intent.getStringExtra("PROMPT") ?: ""

        setContent {
            // Pass the image bitmap and conversations list to the BakingScreen composable
            BakingScreen(context = this, image = imageBitmap, conversations = conversations, prePrompt = prompt)
        }
    }
}