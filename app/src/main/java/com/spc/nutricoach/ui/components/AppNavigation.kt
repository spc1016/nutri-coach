package com.spc.nutricoach.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable

@Serializable
object PantallaFeed

@Serializable
object PantallaPublicar

@Serializable
object PantallaPersonal

@Serializable
object PantallaLogin

@Serializable
object PantallaRegistro

@Serializable
object PantallaPerfil

@Serializable
data class PantallaDetallePost(val postId: String)

@Serializable
data class PantallaPerfilPublico(val clienteId: String)

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

@Serializable
object PantallaQrScanner

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
                color = MaterialTheme.colorScheme.primary
            )
        }
        return
    }

    val navBarRoutes = listOf(
        NavRoute(
            label = "Feed",
            icon = Icons.Default.Home,
            routeObject = PantallaFeed
        ),
        NavRoute(
            label = "Publicar",
            icon = Icons.Default.AddCircle,
            routeObject = PantallaPublicar
        ),
        NavRoute(
            label = "Personal",
            icon = Icons.Default.Person,
            routeObject = PantallaPersonal
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

    LaunchedEffect(Unit) {
        com.spc.nutricoach.workout.WorkoutManager.navigateToWorkoutEvent.collect {
            val rutinaId = com.spc.nutricoach.workout.WorkoutManager.rutinaIdActual.value
            val diaAct = com.spc.nutricoach.workout.WorkoutManager.diaActual.value
            if (rutinaId != null && diaAct != null) {
                navController.navigate(PantallaEntrenamientoDia(rutinaId, diaAct.nombre)) {
                    launchSingleTop = true
                }
            }
        }
    }

    val dietaViewModel: DietaViewModel = viewModel()
    val rutinaViewModel: com.spc.nutricoach.ui.viewmodel.RutinaViewModel = viewModel()
    val entrenamientoViewModel: com.spc.nutricoach.ui.viewmodel.EntrenamientoViewModel = viewModel()
    val notasViewModel: com.spc.nutricoach.ui.viewmodel.NotasViewModel = viewModel()
    val feedViewModel: com.spc.nutricoach.ui.viewmodel.FeedViewModel = viewModel()

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
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ){

                    navBarRoutes.forEach { navRoute ->
                        NavigationBarItem(
                            selected = currentDestination?.hasRoute(navRoute.routeObject::class) == true,
                            onClick = { navController.navigate(navRoute.routeObject) },
                            icon = {
                                Icon(imageVector = navRoute.icon, contentDescription = "Icono de ${navRoute.label}")
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.Black,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = Color.Black,
                                indicatorColor = MaterialTheme.colorScheme.primary
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
            startDestination = if (isLoggedIn == true) PantallaFeed else PantallaLogin
        ) {
            composable<PantallaFeed> {
                FeedView(navController = navController)
            }
            composable<PantallaPublicar> {
                PublicarView(
                    navController = navController,
                    feedViewModel = feedViewModel,
                    rutinaViewModel = rutinaViewModel,
                    dietaViewModel = dietaViewModel
                )
            }
            composable<PantallaPersonal> {
                PersonalView(
                    navController = navController,
                    dietaViewModel = dietaViewModel,
                    rutinaViewModel = rutinaViewModel
                )
            }
            composable<PantallaLogin> {
                LoginView(navController)
            }
            composable<PantallaRegistro> {
                RegistroView(navController)
            }
            composable<PantallaDetalleDieta> { backStackEntry ->
                val detalle = backStackEntry.toRoute<PantallaDetalleDieta>()
                DetalleDietaView(
                    navController = navController,
                    dietaId = detalle.dietaId,
                    dietaViewModel = dietaViewModel
                )
            }
            composable<PantallaDetallePost> { backStackEntry ->
                val args = backStackEntry.toRoute<PantallaDetallePost>()
                DetallePostView(
                    navController = navController,
                    postId = args.postId,
                    feedViewModel = feedViewModel
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
            composable<PantallaPerfilPublico> { backStackEntry ->
                val args = backStackEntry.toRoute<PantallaPerfilPublico>()
                PerfilPublicoView(
                    navController = navController,
                    clienteId = args.clienteId,
                    rutinaViewModel = rutinaViewModel
                )
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
            composable<PantallaQrScanner> {
                QrScannerView(
                    navController = navController,
                    rutinaViewModel = rutinaViewModel
                )
            }
        }
    }
}