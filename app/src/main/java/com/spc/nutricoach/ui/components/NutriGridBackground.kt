package com.spc.nutricoach.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.dp

@Composable
fun NutriGridBackground(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val glowColor = if (isDark) Color(0x1A00E676) else Color(0x0D00E676)

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glowColor,
                        Color.Transparent
                    ),
                    center = center,
                    radius = size.width * 0.8f
                ),
                radius = size.width * 0.8f,
                center = center
            )
        }
        content()
    }
}
