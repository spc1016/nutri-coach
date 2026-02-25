package com.spc.nutricoach.ui.components

import android.R
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.theme.SecondaryBackground
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import java.security.MessageDigest

@Serializable
object PantallaInicio

@Serializable
object PantallaLogin

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var isLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(Unit) {
        isLoggedIn = sessionManager.isLoggedIn.first()
    }

    if (isLoggedIn == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = PrimaryGreen
            )
        }
        return
    }

    val navBarRoutes = listOf(
        NavRoute(
            label = "Inicio",
            icon = Icons.Default.Home,
            routeObject = PantallaInicio
        )
    )

    val navController = rememberNavController()
    val navBarStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBarStackEntry?.destination

    val showNavBar = navBarRoutes.any { navRoute ->
        currentDestination?.hasRoute(navRoute.routeObject::class) == true
    }

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            AnimatedVisibility(
                visible = showNavBar,
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                NavigationBar (
                    containerColor = SecondaryBackground,
                    contentColor = PrimaryGreen
                ){

                    navBarRoutes.forEach { navRoute ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(navRoute.routeObject::class) == true,
                            onClick = { navController.navigate(navRoute.routeObject) },
                            icon = {
                                Icon(imageVector = navRoute.icon, contentDescription = "Icono de ${navRoute.label}")
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            modifier = Modifier.padding(bottom = innerPadding.calculateBottomPadding()),
            navController = navController,
            startDestination = if (isLoggedIn == true) PantallaInicio else PantallaLogin
        ) {
            composable<PantallaInicio> {
                MainView(navController)
            }
            composable<PantallaLogin> {
                LoginView(navController)
            }
        }
    }
}