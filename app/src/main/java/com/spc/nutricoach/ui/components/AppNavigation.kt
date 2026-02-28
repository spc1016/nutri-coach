package com.spc.nutricoach.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

@Serializable
object PantallaInicio

@Serializable
object PantallaRutinas

@Serializable
object PantallaLogin

@Serializable
object PantallaPerfil

@Serializable
data class PantallaDetalleDieta(val dietaId: String)

@Serializable
data class PantallaDetalleRutina(val rutinaId: String)

@Serializable
data class PantallaEntrenamientoDia(val rutinaId: String, val diaNombre: String)

@Serializable
object PantallaNotas

@Serializable
data class PantallaDetalleNota(val notaId: Int)

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
            icon = Icons.Default.Restaurant,
            routeObject = PantallaInicio
        ),
        NavRoute(
            label = "Rutinas",
            icon = Icons.Default.FitnessCenter,
            routeObject = PantallaRutinas
        ),
        NavRoute(
            label = "Notas",
            icon = Icons.Default.EditNote,
            routeObject = PantallaNotas
        )
    )

    val navController = rememberNavController()
    val navBarStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBarStackEntry?.destination

    val dietaViewModel: DietaViewModel = viewModel()
    val rutinaViewModel: com.spc.nutricoach.ui.viewmodel.RutinaViewModel = viewModel()
    val entrenamientoViewModel: com.spc.nutricoach.ui.viewmodel.EntrenamientoViewModel = viewModel()
    val notasViewModel: com.spc.nutricoach.ui.viewmodel.NotasViewModel = viewModel()

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
                    containerColor = MainBackground,
                    contentColor = PrimaryGreen
                ){

                    navBarRoutes.forEach { navRoute ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(navRoute.routeObject::class) == true,
                            onClick = { navController.navigate(navRoute.routeObject) },
                            icon = {
                                Icon(imageVector = navRoute.icon, contentDescription = "Icono de ${navRoute.label}")
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,      // Color del icono cuando está seleccionado
                                unselectedIconColor = Color.Gray,    // Color del icono cuando NO está seleccionado
                                selectedTextColor = Color.White,      // Color del texto (si usas label)
                                indicatorColor = PrimaryGreen         // Color de la "píldora" o fondo circular de selección
                            )
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
                MainView(navController = navController, dietaViewModel = dietaViewModel)
            }
            composable<PantallaRutinas> {
                RutinasView(navController = navController, rutinaViewModel = rutinaViewModel)
            }
            composable<PantallaLogin> {
                LoginView(navController)
            }
            composable<PantallaDetalleDieta> { backStackEntry ->
                val detalle = backStackEntry.toRoute<PantallaDetalleDieta>()
                DetalleDietaView(
                    navController = navController,
                    dietaId = detalle.dietaId,
                    dietaViewModel = dietaViewModel
                )
            }
            composable<PantallaDetalleRutina> { backStackEntry ->
                val detalle = backStackEntry.toRoute<PantallaDetalleRutina>()
                DetalleRutinaView(
                    navController = navController,
                    rutinaId = detalle.rutinaId,
                    rutinaViewModel = rutinaViewModel
                )
            }
            composable<PantallaEntrenamientoDia> { backStackEntry ->
                val args = backStackEntry.toRoute<PantallaEntrenamientoDia>()
                EntrenamientoDiaView(
                    navController = navController,
                    rutinaId = args.rutinaId,
                    diaNombre = args.diaNombre,
                    rutinaViewModel = rutinaViewModel,
                    entrenamientoViewModel = entrenamientoViewModel
                )
            }
            composable<PantallaPerfil> {
                PerfilUsuarioView(navController)
            }
            composable<PantallaNotas> {
                NotasView(navController = navController, notasViewModel = notasViewModel)
            }
            composable<PantallaDetalleNota> { backStackEntry ->
                val args = backStackEntry.toRoute<PantallaDetalleNota>()
                DetalleNotaView(
                    navController = navController,
                    notaId = args.notaId,
                    notasViewModel = notasViewModel
                )
            }
        }
    }
}