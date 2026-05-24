package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Cliente
import com.spc.nutricoach.model.Rutina
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.ui.viewmodel.UsuariosViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilPublicoView(
    navController: NavController,
    clienteId: String,
    rutinaViewModel: RutinaViewModel
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    // We can reuse UsuariosViewModel to handle follow toggle or do it locally
    val usuariosViewModel: UsuariosViewModel = hiltViewModel()
    
    var cliente by remember { mutableStateOf<Cliente?>(null) }
    var rutinas by remember { mutableStateOf<List<Rutina>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(clienteId) {
        isLoading = true
        usuariosViewModel.cargarDetallesUsuario(clienteId) { c, r ->
            cliente = c
            rutinas = r ?: emptyList()
            isLoading = false
        }
    }

    val isFollowing = usuariosViewModel.myFollowingIds.contains(clienteId)
    val letra = cliente?.nombre?.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(cliente?.nombre ?: "Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        NutriGridBackground(modifier = Modifier.padding(innerPadding)) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            } else if (cliente == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error al cargar el perfil")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(AppBrushes.MainGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = letra,
                                style = TextStyle(
                                    color = Color.White,
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = cliente!!.nombre,
                            style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${cliente!!.seguidores_count}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                                Text(text = "Seguidores", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "${cliente!!.seguidos_count}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                                Text(text = "Seguidos", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = {
                                usuariosViewModel.toggleFollow(clienteId)
                                // Optimistically update local count
                                cliente = cliente!!.copy(
                                    seguidores_count = if (isFollowing) cliente!!.seguidores_count - 1 else cliente!!.seguidores_count + 1
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(text = if (isFollowing) "Siguiendo" else "Seguir", fontWeight = FontWeight.Bold)
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                        Text(
                            text = "Rutinas Públicas",
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (rutinas.isEmpty()) {
                        item {
                            Text("Este usuario no tiene rutinas públicas.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else {
                        items(rutinas) { rutina ->
                            RutinaCard(
                                rutina = rutina,
                                onClick = {
                                    navController.navigate(PantallaDetalleRutina(rutinaId = rutina.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
}
