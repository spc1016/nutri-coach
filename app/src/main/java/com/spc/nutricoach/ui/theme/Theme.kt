package com.spc.nutricoach.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ht_primary,
    onPrimary = ht_onPrimary,
    primaryContainer = ht_primaryContainer,
    onPrimaryContainer = ht_onPrimaryContainer,
    secondary = ht_secondary,
    onSecondary = ht_onSecondary,
    secondaryContainer = ht_secondaryContainer,
    onSecondaryContainer = ht_onSecondaryContainer,
    tertiary = ht_tertiary,
    onTertiary = ht_onTertiary,
    tertiaryContainer = ht_tertiaryContainer,
    onTertiaryContainer = ht_onTertiaryContainer,
    background = ht_background,
    onBackground = ht_onBackground,
    surface = ht_surface,
    onSurface = ht_onSurface,
    surfaceVariant = ht_surfaceVariant,
    onSurfaceVariant = ht_onSurfaceVariant,
    error = ht_error,
    onError = ht_onError,
    errorContainer = ht_errorContainer,
    onErrorContainer = ht_onErrorContainer,
    outline = ht_outline
)

private val LightColorScheme = lightColorScheme(
    primary = vw_primary,
    onPrimary = vw_onPrimary,
    primaryContainer = vw_primaryContainer,
    onPrimaryContainer = vw_onPrimaryContainer,
    secondary = vw_secondary,
    onSecondary = vw_onSecondary,
    secondaryContainer = vw_secondaryContainer,
    onSecondaryContainer = vw_onSecondaryContainer,
    tertiary = vw_tertiary,
    onTertiary = vw_onTertiary,
    tertiaryContainer = vw_tertiaryContainer,
    onTertiaryContainer = vw_onTertiaryContainer,
    background = vw_background,
    onBackground = vw_onBackground,
    surface = vw_surface,
    onSurface = vw_onSurface,
    surfaceVariant = vw_surfaceVariant,
    onSurfaceVariant = vw_onSurfaceVariant,
    error = vw_error,
    onError = vw_onError,
    errorContainer = vw_errorContainer,
    onErrorContainer = vw_onErrorContainer,
    outline = vw_outline
)

@Composable
fun NutriCoachTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Disable dynamic color for brand consistency by default
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val typography = if (darkTheme) DarkTypography else LightTypography
    val shapes = if (darkTheme) DarkShapes else LightShapes

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        shapes = shapes,
        content = content
    )
}