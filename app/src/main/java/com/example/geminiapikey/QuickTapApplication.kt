package com.example.geminiapikey

import android.app.Application
import com.google.firebase.FirebaseApp

class QuickTapApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase here
        FirebaseApp.initializeApp(this)
    }
}