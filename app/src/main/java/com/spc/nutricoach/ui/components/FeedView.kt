package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import com.spc.nutricoach.ui.theme.adaptiveContainer
import com.spc.nutricoach.ui.viewmodel.FeedViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedView(navController: NavController, feedViewModel: FeedViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        feedViewModel.cargarPosts()
        feedViewModel.cargarUsuarios()
    }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val currentUserId by sessionManager.clienteIdFlow.collectAsState(initial = "")
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
                    IconButton(onClick = { feedViewModel.cargarPosts(force = true) }) {
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
                    selectedTabIndex = feedViewModel.selectedTab,
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = feedViewModel.selectedTab == 0,
                        onClick = { feedViewModel.selectedTab = 0 },
                        text = { Text("Posts", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = feedViewModel.selectedTab == 1,
                        onClick = { feedViewModel.selectedTab = 1 },
                        text = { Text("Usuarios", fontWeight = FontWeight.Bold) }
                    )
                }
                
                if (feedViewModel.selectedTab == 0) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }

                        if (feedViewModel.isLoading && feedViewModel.posts.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        if (!feedViewModel.isLoading && feedViewModel.posts.isEmpty()) {
                            item {
                                Text(
                                    text = "No hay posts en la comunidad",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 40.dp)
                                )
                            }
                        }

                        items(feedViewModel.posts) { post ->
                            PostCard(
                                post = post,
                                currentUserId = currentUserId ?: "",
                                onLikeClick = { feedViewModel.toggleLike(post.id) },
                                onCommentClick = { 
                                    navController.navigate(PantallaDetallePost(post.id))
                                },
                                onRoutineClick = { rutinaId ->
                                    navController.navigate(PantallaDetalleRutina(rutinaId))
                                },
                                onDietaClick = { dietaId ->
                                    navController.navigate(PantallaDetalleDieta(dietaId))
                                },
                                onClick = {
                                    navController.navigate(PantallaDetallePost(post.id))
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                        
                        item {
                            OutlinedTextField(
                                value = feedViewModel.searchQuery,
                                onValueChange = { feedViewModel.searchQuery = it },
                                label = { Text("Buscar usuarios...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                        }
                        
                        val usuariosFiltrados = feedViewModel.clientes.filter { 
                            it.nombre.contains(feedViewModel.searchQuery, ignoreCase = true) 
                        }
                        
                        items(usuariosFiltrados) { cliente ->
                            val isFollowing = feedViewModel.seguidosIds.contains(cliente.id)
                            UsuarioCard(
                                cliente = cliente,
                                isFollowing = isFollowing,
                                onFollowClick = { cliente.id?.let { feedViewModel.toggleFollow(it) } },
                                onClick = { 
                                    cliente.id?.let { navController.navigate(PantallaPerfilPublico(it)) }
                                }
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioCard(
    cliente: com.spc.nutricoach.model.Cliente,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .adaptiveContainer(cornerRadius = 16.dp, elevation = 2.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(AppBrushes.MainGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = cliente.nombre.firstOrNull()?.uppercase() ?: "U",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cliente.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${cliente.seguidores_count} seguidores",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(
                onClick = onFollowClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                    contentColor = if (isFollowing) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(if (isFollowing) "Siguiendo" else "Seguir", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
