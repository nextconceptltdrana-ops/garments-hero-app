package com.example

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class QuizApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this)
            }
            Log.d("QuizApplication", "Firebase initialized successfully")
        } catch (e: Exception) {
            Log.e("QuizApplication", "Error initializing Firebase: ${e.message}", e)
        }
    }
}

