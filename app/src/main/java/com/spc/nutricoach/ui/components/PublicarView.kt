package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.FeedViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarView(
    navController: NavController,
    feedViewModel: FeedViewModel,
    rutinaViewModel: RutinaViewModel
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    var textoPost by remember { mutableStateOf("") }
    var rutinaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var rutinaSeleccionadaNombre by remember { mutableStateOf("Ninguna") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        rutinaViewModel.cargarRutinas()
    }

    val rutinasPublicas = rutinaViewModel.rutinas.filter { it.publica }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Crear Publicación",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush = AppBrushes.AccentGradient
                        )
                    )
                },
                actions = {
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header (Author)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(AppBrushes.MainGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = letraInicial,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Tú",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Text Input
                OutlinedTextField(
                    value = textoPost,
                    onValueChange = { textoPost = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    placeholder = { Text("¿Qué quieres compartir con la comunidad?") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Routine Dropdown
                Text(
                    text = "Vincular rutina (opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = isDropdownExpanded,
                    onExpandedChange = { isDropdownExpanded = !isDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = rutinaSeleccionadaNombre,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna") },
                            onClick = {
                                rutinaSeleccionadaId = null
                                rutinaSeleccionadaNombre = "Ninguna"
                                isDropdownExpanded = false
                            }
                        )
                        rutinasPublicas.forEach { rutina ->
                            DropdownMenuItem(
                                text = { Text(rutina.nombre) },
                                onClick = {
                                    rutinaSeleccionadaId = rutina.id
                                    rutinaSeleccionadaNombre = rutina.nombre
                                    isDropdownExpanded = false
                                }
                            )
                        }
                        if (rutinasPublicas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tienes rutinas públicas") },
                                onClick = { isDropdownExpanded = false },
                                enabled = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Submit Button
                Button(
                    onClick = {
                        if (textoPost.isNotBlank()) {
                            feedViewModel.crearPost(textoPost, rutinaSeleccionadaId) { success, _ ->
                                if (success) {
                                    textoPost = ""
                                    rutinaSeleccionadaId = null
                                    rutinaSeleccionadaNombre = "Ninguna"
                                    navController.navigate(PantallaFeed)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = textoPost.isNotBlank()
                ) {
                    Text("Publicar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
