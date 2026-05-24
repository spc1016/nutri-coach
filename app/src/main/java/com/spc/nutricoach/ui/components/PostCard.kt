package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.spc.nutricoach.model.Post
import com.spc.nutricoach.ui.theme.AppBrushes

@Composable
fun PostCard(
    post: Post,
    currentUserId: String,
    onLikeClick: () -> Unit,
    onCommentClick: () -> Unit,
    onRoutineClick: (String) -> Unit,
    onDietaClick: (String) -> Unit,
    onClick: (() -> Unit)? = null
) {
    val isLiked = post.likedBy.contains(currentUserId)
    var showFullImageIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Avatar and Name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AppBrushes.MainGradient),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = post.autorNombre.firstOrNull()?.uppercase() ?: "U",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = post.autorNombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = post.fechaCreacion.take(10), // Simplistic date formatting
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body: Text
            Text(
                text = post.texto,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Parsear la lista de imágenes (si es un array serializado en JSON o una única URL)
            val imagenes = remember(post.imagenUrl) {
                if (post.imagenUrl.isNullOrBlank()) {
                    emptyList<String>()
                } else if (post.imagenUrl.startsWith("[") && post.imagenUrl.endsWith("]")) {
                    try {
                        val regex = "\"[^\"]+\"".toRegex()
                        regex.findAll(post.imagenUrl).map { it.value.removeSurrounding("\"") }.toList()
                    } catch (e: Exception) {
                        listOf(post.imagenUrl)
                    }
                } else {
                    listOf(post.imagenUrl)
                }
            }

            if (imagenes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                if (imagenes.size == 1) {
                    // Una sola imagen, renderizado estándar
                    AsyncImage(
                        model = imagenes.first(),
                        contentDescription = "Imagen de la publicación",
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showFullImageIndex = 0 },
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // Carrusel horizontal estilo Instagram para múltiples imágenes
                    val pagerState = rememberPagerState(pageCount = { imagenes.size })
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            AsyncImage(
                                model = imagenes[page],
                                contentDescription = "Imagen ${page + 1} de la publicación",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showFullImageIndex = page },
                                contentScale = ContentScale.Crop
                            )
                        }
                        
                        // Badge indicador de página flotante en la esquina superior derecha
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.TopEnd)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1}/${imagenes.size}",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Indicadores de puntos (Dot Indicators) centrados estilo Instagram
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(imagenes.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .size(if (isSelected) 7.dp else 5.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary 
                                        else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }
                }
            }

            // Diálogo a pantalla completa interactivo estilo Instagram
            if (showFullImageIndex != null) {
                Dialog(
                    onDismissRequest = { showFullImageIndex = null },
                    properties = DialogProperties(
                        usePlatformDefaultWidth = false,
                        dismissOnBackPress = true,
                        dismissOnClickOutside = true
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFA000000))
                    ) {
                        val fullPagerState = rememberPagerState(
                            initialPage = showFullImageIndex!!,
                            pageCount = { imagenes.size }
                        )
                        
                        HorizontalPager(
                            state = fullPagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { page ->
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = imagenes[page],
                                    contentDescription = "Imagen $page completa",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.85f),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                        
                        // Header con botón de cerrar "X" y contador en pantalla completa
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(16.dp)
                                .align(Alignment.TopCenter),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${fullPagerState.currentPage + 1} de ${imagenes.size}",
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(
                                onClick = { showFullImageIndex = null },
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape)
                                    .size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Cerrar visor completo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Indicadores de puntos en la pantalla completa (si hay más de una foto)
                        if (imagenes.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .navigationBarsPadding()
                                    .padding(bottom = 24.dp)
                                    .align(Alignment.BottomCenter),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                repeat(imagenes.size) { index ->
                                    val isSelected = fullPagerState.currentPage == index
                                    Box(
                                        modifier = Modifier
                                            .padding(horizontal = 4.dp)
                                            .size(if (isSelected) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) Color.White 
                                                else Color.White.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Optional: Routine Link (just visual for now)
            if (!post.rutinaId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(
                    onClick = { post.rutinaId?.let { onRoutineClick(it) } },
                    label = { Text("Ver Rutina Asociada") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                )
            }

            // Optional: Diet Link
            if (!post.dietaId.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                SuggestionChip(
                    onClick = { post.dietaId?.let { onDietaClick(it) } },
                    label = { Text("Ver Dieta Asociada") },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Footer: Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onLikeClick) {
                        Icon(
                            imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.likes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onCommentClick) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Comment",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        text = "${post.comentarios.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
