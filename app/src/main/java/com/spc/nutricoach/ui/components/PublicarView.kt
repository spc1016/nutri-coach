package com.spc.nutricoach.ui.components

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.FeedViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import com.spc.nutricoach.util.CloudinaryUploader
import kotlinx.coroutines.launch
import java.io.File

// CONFIGURACIÓN DE CLOUDINARY (Subida Unsigned)
const val CLOUDINARY_CLOUD_NAME = "dsgu3bw8h"
const val CLOUDINARY_UPLOAD_PRESET = "nutricoach_preset"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublicarView(
    navController: NavController,
    feedViewModel: FeedViewModel,
    rutinaViewModel: RutinaViewModel,
    dietaViewModel: DietaViewModel
) {


    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    var textoPost by remember { mutableStateOf("") }
    var imagenesSeleccionadasUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var showAddMediaDialog by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                val currentCount = imagenesSeleccionadasUris.size
                val allowedNew = 10 - currentCount
                if (uris.size > allowedNew) {
                    android.widget.Toast.makeText(context, "Solo puedes subir hasta 10 fotos. Se agregaron las primeras $allowedNew.", android.widget.Toast.LENGTH_LONG).show()
                }
                imagenesSeleccionadasUris = (imagenesSeleccionadasUris + uris).take(10)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && cameraPhotoUri != null) {
                if (imagenesSeleccionadasUris.size < 10) {
                    imagenesSeleccionadasUris = imagenesSeleccionadasUris + cameraPhotoUri!!
                } else {
                    android.widget.Toast.makeText(context, "Ya has alcanzado el límite de 10 fotos", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                try {
                    val uri = crearUriParaFotoCamara(context)
                    cameraPhotoUri = uri
                    cameraLauncher.launch(uri)
                } catch (e: Exception) {
                    android.util.Log.e("PublicarView", "Error al iniciar cámara", e)
                    android.widget.Toast.makeText(context, "Error al crear archivo de imagen: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            } else {
                android.widget.Toast.makeText(context, "Se necesita permiso de cámara para hacer fotos", android.widget.Toast.LENGTH_LONG).show()
            }
        }
    )
    var rutinaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var rutinaSeleccionadaNombre by remember { mutableStateOf("Ninguna") }
    var isRutinaDropdownExpanded by remember { mutableStateOf(false) }

    var dietaSeleccionadaId by remember { mutableStateOf<String?>(null) }
    var dietaSeleccionadaNombre by remember { mutableStateOf("Ninguna") }
    var isDietaDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val clienteId = sessionManager.getClienteId()
        rutinaViewModel.cargarRutinas()
        if (clienteId != null) {
            dietaViewModel.loadDietas(clienteId)
        }
    }

    val rutinasPublicas = rutinaViewModel.rutinas.filter { it.publica }
    val dietasPublicas = dietaViewModel.dietas.filter { it.publica }

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

                // Muestra un contador de fotos estilo premium
                if (imagenesSeleccionadasUris.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Fotos seleccionadas",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${imagenesSeleccionadasUris.size} / 10",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = if (imagenesSeleccionadasUris.size == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (imagenesSeleccionadasUris.isEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Galería")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Galería")
                        }

                        OutlinedButton(
                            onClick = {
                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    try {
                                        val uri = crearUriParaFotoCamara(context)
                                        cameraPhotoUri = uri
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        android.util.Log.e("PublicarView", "Error al iniciar cámara", e)
                                        android.widget.Toast.makeText(context, "Error al iniciar cámara: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Cámara")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Hacer Foto")
                        }
                    }
                } else {
                    // Vista previa de las imágenes seleccionadas en scroll horizontal
                    androidx.compose.foundation.lazy.LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(end = 16.dp)
                    ) {
                        items(imagenesSeleccionadasUris.size) { index ->
                            val uri = imagenesSeleccionadasUris[index]
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = "Miniatura de foto",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                                
                                // Botón de eliminar superpuesto en la esquina superior derecha
                                FilledIconButton(
                                    onClick = {
                                        imagenesSeleccionadasUris = imagenesSeleccionadasUris.toMutableList().apply { removeAt(index) }
                                    },
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(28.dp)
                                        .align(Alignment.TopEnd),
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = Color.Black.copy(alpha = 0.6f),
                                        contentColor = Color.White
                                    )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Eliminar imagen",
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        // Agregar botón de "+ añadir más" al final si aún queda cupo
                        if (imagenesSeleccionadasUris.size < 10) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(120.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                        .clickable {
                                            showAddMediaDialog = true
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = "Añadir más fotos",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Añadir más",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Routine Dropdown
                Text(
                    text = "Vincular rutina (opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = isRutinaDropdownExpanded,
                    onExpandedChange = { isRutinaDropdownExpanded = !isRutinaDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = rutinaSeleccionadaNombre,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isRutinaDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isRutinaDropdownExpanded,
                        onDismissRequest = { isRutinaDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna") },
                            onClick = {
                                rutinaSeleccionadaId = null
                                rutinaSeleccionadaNombre = "Ninguna"
                                isRutinaDropdownExpanded = false
                            }
                        )
                        rutinasPublicas.forEach { rutina ->
                            DropdownMenuItem(
                                text = { Text(rutina.nombre) },
                                onClick = {
                                    rutinaSeleccionadaId = rutina.id
                                    rutinaSeleccionadaNombre = rutina.nombre
                                    isRutinaDropdownExpanded = false
                                }
                            )
                        }
                        if (rutinasPublicas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tienes rutinas públicas") },
                                onClick = { isRutinaDropdownExpanded = false },
                                enabled = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Diet Dropdown
                Text(
                    text = "Vincular dieta (opcional)",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = isDietaDropdownExpanded,
                    onExpandedChange = { isDietaDropdownExpanded = !isDietaDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = dietaSeleccionadaNombre,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isDietaDropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    
                    ExposedDropdownMenu(
                        expanded = isDietaDropdownExpanded,
                        onDismissRequest = { isDietaDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Ninguna") },
                            onClick = {
                                dietaSeleccionadaId = null
                                dietaSeleccionadaNombre = "Ninguna"
                                isDietaDropdownExpanded = false
                            }
                        )
                        dietasPublicas.forEach { dieta ->
                            DropdownMenuItem(
                                text = { Text(dieta.nombre) },
                                onClick = {
                                    dietaSeleccionadaId = dieta.id
                                    dietaSeleccionadaNombre = dieta.nombre
                                    isDietaDropdownExpanded = false
                                }
                            )
                        }
                        if (dietasPublicas.isEmpty()) {
                            DropdownMenuItem(
                                text = { Text("No tienes dietas públicas") },
                                onClick = { isDietaDropdownExpanded = false },
                                enabled = false
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))

                // Submit Button
                val isPublishEnabled = textoPost.isNotBlank()

                Button(
                    onClick = {
                        if (textoPost.isNotBlank()) {
                            feedViewModel.publicarPostConImagenes(
                                context = context,
                                texto = textoPost,
                                rutinaId = rutinaSeleccionadaId,
                                dietaId = dietaSeleccionadaId,
                                uris = imagenesSeleccionadasUris
                            ) {
                                // onInicio lambda: Ejecutado inmediatamente al iniciar la subida
                                textoPost = ""
                                imagenesSeleccionadasUris = emptyList()
                                cameraPhotoUri = null
                                rutinaSeleccionadaId = null
                                rutinaSeleccionadaNombre = "Ninguna"
                                dietaSeleccionadaId = null
                                dietaSeleccionadaNombre = "Ninguna"
                                navController.navigate(PantallaFeed) {
                                    popUpTo(PantallaFeed) { inclusive = false }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = isPublishEnabled
                ) {
                    Text("Publicar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
        
        if (showAddMediaDialog) {
            AlertDialog(
                onDismissRequest = { showAddMediaDialog = false },
                title = { 
                    Text(
                        text = "Añadir Foto", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                text = { 
                    Text(
                        text = "Selecciona una opción para añadir una foto a tu publicación.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                confirmButton = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                showAddMediaDialog = false
                                val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.CAMERA
                                ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                if (hasPermission) {
                                    try {
                                        val uri = crearUriParaFotoCamara(context)
                                        cameraPhotoUri = uri
                                        cameraLauncher.launch(uri)
                                    } catch (e: Exception) {
                                        android.util.Log.e("PublicarView", "Error al iniciar cámara", e)
                                        android.widget.Toast.makeText(context, "Error al iniciar cámara: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Cámara")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Usar Cámara")
                        }

                        Button(
                            onClick = {
                                showAddMediaDialog = false
                                galleryLauncher.launch("image/*")
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Galería")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Elegir de Galería")
                        }

                        TextButton(
                            onClick = { showAddMediaDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Cancelar", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            )
        }
    }
}

private fun crearUriParaFotoCamara(context: android.content.Context): Uri {
    val directorioCache = context.cacheDir
    val archivo = File.createTempFile("foto_camara_", ".jpg", directorioCache).apply {
        createNewFile()
        deleteOnExit()
    }
    return FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        archivo
    )
}
