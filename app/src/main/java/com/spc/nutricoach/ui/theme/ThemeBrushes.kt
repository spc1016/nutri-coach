package com.spc.nutricoach.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
// Importa tu PrimaryGreen aquí

object AppBrushes {
    val Main = Brush.linearGradient(
        colors = listOf(PrimaryGreen, Color.Black)
    )

    val Secondary = Brush.linearGradient(
        colors = listOf(PrimaryGreen, Color.Green)
    )
}