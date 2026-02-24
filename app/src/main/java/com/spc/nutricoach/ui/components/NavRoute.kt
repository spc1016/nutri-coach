package com.spc.nutricoach.ui.components

import androidx.compose.ui.graphics.vector.ImageVector

data class NavRoute(
    // Texto de la barra de navegacion
    val label: String,
    // Icono de la barra de navegacion
    val icon: ImageVector,
    // Objeto de la ruta a la que navega (el. PantallaInicio)
    val routeObject: Any
)
