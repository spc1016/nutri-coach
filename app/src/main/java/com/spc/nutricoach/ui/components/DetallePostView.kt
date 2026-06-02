package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.FeedViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetallePostView(
    navController: NavController,
    postId: String,
    feedViewModel: FeedViewModel,
    rutinaViewModel: RutinaViewModel,
    dietaViewModel: DietaViewModel
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val currentUserId by sessionManager.clienteIdFlow.collectAsState(initial = "")

    var nuevoComentario by remember { mutableStateOf("") }
    var enviandoComentario by remember { mutableStateOf(false) }
    val post = feedViewModel.posts.find { it.id == postId }

    LaunchedEffect(Unit) {
        if (feedViewModel.posts.isEmpty()) {
            feedViewModel.cargarPosts()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Comentarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = nuevoComentario,
                        onValueChange = { nuevoComentario = it },
                        placeholder = { Text("Escribe un comentario...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (nuevoComentario.isNotBlank() && !enviandoComentario) {
                                enviandoComentario = true
                                feedViewModel.comentarPost(postId, nuevoComentario) { success, _ ->
                                    enviandoComentario = false
                                    if (success) {
                                        nuevoComentario = ""
                                    }
                                }
                            }
                        },
                        enabled = !enviandoComentario,
                        modifier = Modifier
                            .size(48.dp)
                            .then(
                                if (enviandoComentario) {
                                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant, shape = CircleShape)
                                } else {
                                    Modifier.background(AppBrushes.MainGradient, shape = CircleShape)
                                }
                            )
                    ) {
                        if (enviandoComentario) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Enviar",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (post == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            NutriGridBackground(modifier = Modifier.padding(innerPadding)) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                        PostCard(
                            post = post,
                            currentUserId = currentUserId ?: "",
                            onLikeClick = { feedViewModel.toggleLike(post.id, currentUserId ?: "") },
                            onCommentClick = { /* Ya estamos aquí */ },
                            onRoutineClick = { rutinaId ->
                                navController.navigate(PantallaDetalleRutina(rutinaId, isReadOnly = true))
                            },
                            onDietaClick = { dietaId ->
                                navController.navigate(PantallaDetalleDieta(dietaId, isReadOnly = true))
                            },
                            onReplicateRoutineClick = { rutinaId ->
                                rutinaViewModel.clonarRutinaPorId(rutinaId) { success, error ->
                                    if (success) {
                                        Toast.makeText(context, "¡Rutina replicada exitosamente!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, error ?: "Error al replicar la rutina", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onReplicateDietaClick = { dietaId ->
                                dietaViewModel.clonarDietaPorId(dietaId) { success, error ->
                                    if (success) {
                                        Toast.makeText(context, "¡Dieta replicada exitosamente!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, error ?: "Error al replicar la dieta", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onClick = { /* Nada */ }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Comentarios",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    items(post.comentarios) { comentario ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = comentario.autorNombre,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = comentario.texto,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (post.comentarios.isEmpty()) {
                        item {
                            Text(
                                text = "Aún no hay comentarios. ¡Sé el primero!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 20.dp)
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}
