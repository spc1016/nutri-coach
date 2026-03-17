package com.spc.nutricoach

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spc.nutricoach.ui.components.AppNavigation
import com.spc.nutricoach.ui.theme.NutriCoachTheme
import com.spc.nutricoach.workout.WorkoutManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            NutriCoachTheme {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == "ACTION_NAVIGATE_TO_WORKOUT") {
            WorkoutManager.triggerNavigationToWorkout()
        }
    }
}
