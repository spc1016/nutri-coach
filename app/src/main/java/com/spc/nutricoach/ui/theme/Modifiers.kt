package com.spc.nutricoach.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun Modifier.adaptiveContainer(
    cornerRadius: Dp = 16.dp,
    elevation: Dp = 8.dp,
    containerColor: Color? = null
) = composed {
    val actualColor = containerColor ?: MaterialTheme.colorScheme.surface
    if (isSystemInDarkTheme()) {
        val glassBrush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.2f),
                Color.White.copy(alpha = 0.0f)
            )
        )
        this
            .background(
                color = actualColor.copy(alpha = 0.85f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 1.dp,
                brush = glassBrush,
                shape = RoundedCornerShape(cornerRadius)
            )
    } else {
        this.shadow(
            elevation = elevation,
            shape = RoundedCornerShape(cornerRadius),
            ambientColor = vw_primary,
            spotColor = vw_primary
        ).background(
            color = actualColor,
            shape = RoundedCornerShape(cornerRadius)
        )
    }
}

