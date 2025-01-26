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

        val imageBitmap = intent.getParcelableExtra<Bitmap>("image")
        setContent {
            if (imageBitmap != null) {
                BakingScreen(this, imageBitmap)
            }
            else{
                BakingScreen(this, null)
            }
        }
    }
}