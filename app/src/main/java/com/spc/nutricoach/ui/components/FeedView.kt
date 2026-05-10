package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.ui.viewmodel.UsuariosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(
    navController: NavController,
    rutinaViewModel: RutinaViewModel = viewModel(),
    usuariosViewModel: UsuariosViewModel = viewModel()
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    
    LaunchedEffect(Unit) {
        rutinaViewModel.cargarRutinasPublicas()
        usuariosViewModel.cargarUsuarios()
    }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Comunidad",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush = AppBrushes.AccentGradient
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        if (selectedTabIndex == 0) rutinaViewModel.cargarRutinasPublicas(force = true)
                        else usuariosViewModel.cargarUsuarios(force = true)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar feed",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppBrushes.MainGradient)
                            .clickable { navController.navigate(PantallaPerfil) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letraInicial,
                            style = TextStyle(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        NutriGridBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = { selectedTabIndex = 0 },
                        text = { Text("Rutinas") }
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = { selectedTabIndex = 1 },
                        text = { Text("Usuarios") }
                    )
                }

                if (selectedTabIndex == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        if (rutinaViewModel.isLoading && rutinaViewModel.rutinasPublicas.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (!rutinaViewModel.isLoading && rutinaViewModel.rutinasPublicas.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay rutinas públicas en la comunidad",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 40.dp)
                                )
                            }
                        }

                        items(rutinaViewModel.rutinasPublicas) { rutina ->
                            RutinaCard(
                                rutina = rutina,
                                onClick = {
                                    navController.navigate(PantallaDetalleRutina(rutinaId = rutina.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = usuariosViewModel.searchQuery,
                            onValueChange = { usuariosViewModel.searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar usuarios...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            shape = RoundedCornerShape(20.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val filteredUsers = usuariosViewModel.usuarios.filter {
                            it.nombre.contains(usuariosViewModel.searchQuery, ignoreCase = true) ||
                            it.email.contains(usuariosViewModel.searchQuery, ignoreCase = true)
                        }

                        if (usuariosViewModel.isLoading && usuariosViewModel.usuarios.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (filteredUsers.isEmpty()) {
                            Text(
                                text = "No se encontraron usuarios",
                                style = TextStyle(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp),
                                modifier = Modifier.padding(vertical = 40.dp).align(Alignment.CenterHorizontally)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(filteredUsers) { usuario ->
                                    val isFollowing = usuariosViewModel.myFollowingIds.contains(usuario.id)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { 
                                                if (usuario.id != null) {
                                                    navController.navigate(PantallaPerfilPublico(usuario.id))
                                                }
                                            }
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = usuario.nombre,
                                                style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                                            )
                                            Text(
                                                text = "${usuario.seguidores_count} seguidores",
                                                style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            )
                                        }
                                        Button(
                                            onClick = { 
                                                usuario.id?.let { usuariosViewModel.toggleFollow(it) } 
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isFollowing) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.primary,
                                                contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                                            )
                                        ) {
                                            Text(if (isFollowing) "Siguiendo" else "Seguir")
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                }
                                item { Spacer(modifier = Modifier.height(80.dp)) }
                            }
                        }
                    }
                }
            }
        }
    }
}
