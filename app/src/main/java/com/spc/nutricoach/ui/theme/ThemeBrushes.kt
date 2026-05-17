package com.spc.nutricoach.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush

object AppBrushes {
    val MainGradient: Brush
        @Composable
        get() = if (isSystemInDarkTheme()) {
            Brush.linearGradient(colors = listOf(ht_primary, ht_secondary))
        } else {
            Brush.linearGradient(colors = listOf(vw_primary, vw_secondary))
        }

    val AccentGradient: Brush
        @Composable
        get() = if (isSystemInDarkTheme()) {
            Brush.linearGradient(colors = listOf(ht_secondary, ht_tertiary))
        } else {
            Brush.linearGradient(colors = listOf(vw_secondary, vw_tertiary))
        }
}