package com.spc.nutricoach

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spc.nutricoach.ui.components.AppNavigation
import com.spc.nutricoach.ui.theme.NutriCoachTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutriCoachTheme {
                AppNavigation()
            }
        }
    }
}
