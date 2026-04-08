package com.alarmed.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.alarmed.app.ui.theme.AlarmedTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main Activity for the Calendar-Driven Alarm App.
 * 
 * Serves as the entry point for the Jetpack Compose UI.
 * Annotated with @AndroidEntryPoint to enable Hilt injection.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            AlarmedTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmAppScreen()
                }
            }
        }
    }
}

/**
 * Root composable for the app.
 * This will be expanded as we implement the UI layers.
 */
@Composable
fun AlarmAppScreen() {
    // TODO: Implement main UI based on Development Plan (AlarmListScreen, etc.)
    androidx.compose.material3.Text("Calendar-Driven Alarm App - MVP")
}
