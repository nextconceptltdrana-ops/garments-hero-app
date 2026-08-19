package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
        AdManager.initialize(this)
        // Flush any pending unsynced points from previous session/crash
        FirebaseUserManager.syncPendingProgressToFirestore(applicationContext)
    } catch (e: Throwable) {
        // Safe fallback if initialization fails
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          val viewModel: AuthViewModel = viewModel()
          MainAuthApp(viewModel = viewModel)
        }
      }
    }
  }

  override fun onPause() {
    super.onPause()
    FirebaseUserManager.syncPendingProgressToFirestore(applicationContext)
  }

  override fun onStop() {
    super.onStop()
    FirebaseUserManager.syncPendingProgressToFirestore(applicationContext)
  }

  override fun onDestroy() {
    super.onDestroy()
    FirebaseUserManager.syncPendingProgressToFirestore(applicationContext)
  }
}

