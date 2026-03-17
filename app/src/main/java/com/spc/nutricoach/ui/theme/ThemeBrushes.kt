package com.spc.nutricoach.ui.theme

import androidx.compose.ui.graphics.Brush

object AppBrushes {
    val MainGradient = Brush.linearGradient(
        colors = listOf(PrimaryNeon, SecondaryTeal)
    )

    val AccentGradient = Brush.linearGradient(
        colors = listOf(SecondaryTeal, AccentYellowGreen)
    )
}