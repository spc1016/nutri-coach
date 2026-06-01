package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.LoginViewModel
import com.spc.nutricoach.ui.viewmodel.PerfilUsuarioViewModel
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import java.io.File
import android.widget.Toast
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.width
import androidx.compose.material3.TextButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioView(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    perfilViewModel: PerfilUsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    
    val letra = email?.firstOrNull()?.uppercase() ?: "U"

    var showDialog by remember { mutableStateOf<String?>(null) } // "seguidores" or "seguidos"
    var showFotoDialog by remember { mutableStateOf(false) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                perfilViewModel.subirYActualizarFoto(context, uri)
            }
        }
    )

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success && cameraPhotoUri != null) {
                perfilViewModel.subirYActualizarFoto(context, cameraPhotoUri!!)
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
                    android.util.Log.e("PerfilUsuarioView", "Error al iniciar cámara", e)
                    Toast.makeText(context, "Error al crear archivo de imagen: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Se necesita permiso de cámara para hacer fotos", Toast.LENGTH_LONG).show()
            }
        }
    )

    if (showFotoDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showFotoDialog = false },
            title = { Text("Actualizar foto de perfil") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            showFotoDialog = false
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
                                    android.util.Log.e("PerfilUsuarioView", "Error al iniciar cámara", e)
                                    Toast.makeText(context, "Error al iniciar cámara: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Cámara", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Usar Cámara", color = Color.Black, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            showFotoDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = "Galería", tint = Color.Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Elegir de Galería", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(
                    onClick = { showFotoDialog = false }
                ) {
                    Text("Cancelar", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(AppBrushes.MainGradient)
                    .then(
                        if (perfilViewModel.isEditing) {
                            Modifier.clickable { showFotoDialog = true }
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (!perfilViewModel.fotoPerfil.isNullOrEmpty()) {
                    AsyncImage(
                        model = perfilViewModel.fotoPerfil,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = letra,
                        style = TextStyle(
                            color = Color.White,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                if (perfilViewModel.isLoading && perfilViewModel.statusMessage.contains("Subiendo")) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }

                if (perfilViewModel.isEditing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp),
                        contentAlignment = Alignment.BottomEnd
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(1.dp, Color.Black, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PhotoCamera,
                                contentDescription = "Cambiar foto",
                                tint = Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (perfilViewModel.isLoading && perfilViewModel.nombre.isEmpty()) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
            } else if (!perfilViewModel.isEditing) {
                // Modo Vista
                Text(
                    text = perfilViewModel.nombre.ifEmpty { "Usuario" },
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = perfilViewModel.email,
                    style = TextStyle(fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            perfilViewModel.cargarListas()
                            showDialog = "seguidores" 
                        }.padding(8.dp)
                    ) {
                        Text(text = "${perfilViewModel.seguidoresCount}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                        Text(text = "Seguidores", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { 
                            perfilViewModel.cargarListas()
                            showDialog = "seguidos" 
                        }.padding(8.dp)
                    ) {
                        Text(text = "${perfilViewModel.seguidosCount}", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground))
                        Text(text = "Seguidos", style = TextStyle(fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    onClick = { perfilViewModel.isEditing = true }
                ) {
                    Text(
                        text = "Editar Perfil",
                        style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    )
                }

                if (showDialog != null) {
                    androidx.compose.material3.AlertDialog(
                        onDismissRequest = { showDialog = null },
                        title = { Text(if (showDialog == "seguidores") "Seguidores" else "Seguidos") },
                        text = {
                            if (perfilViewModel.isLoadingListas) {
                                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            } else {
                                val list = if (showDialog == "seguidores") perfilViewModel.seguidoresList else perfilViewModel.seguidosList
                                if (list.isEmpty()) {
                                    Text("No hay usuarios.")
                                } else {
                                    LazyColumn(modifier = Modifier.fillMaxHeight(0.5f)) {
                                        items(list) { usuario ->
                                            Row(
                                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(usuario.nombre, fontWeight = FontWeight.Bold)
                                                if (showDialog == "seguidos") {
                                                    Button(
                                                        onClick = {
                                                            if (usuario.id != null) {
                                                                perfilViewModel.dejarDeSeguir(usuario.id)
                                                            }
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                                    ) {
                                                        Text("Dejar de seguir", fontSize = 12.sp)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            Button(onClick = { showDialog = null }) {
                                Text("Cerrar")
                            }
                        }
                    )
                }
            } else {
                // Modo Edición
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    onClick = { perfilViewModel.isEditing = false }
                ) {
                    Text("Cancelar Edición")
                }
                Spacer(modifier = Modifier.height(16.dp))
                
                PerfilTextField(
                    value = perfilViewModel.nombre,
                    onValueChange = { perfilViewModel.nombre = it },
                    label = "Nombre",
                    icon = Icons.Filled.Person,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.email,
                    onValueChange = { perfilViewModel.email = it },
                    label = "Email",
                    icon = Icons.Filled.Email,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.telefono,
                    onValueChange = { perfilViewModel.telefono = it },
                    label = "Teléfono",
                    icon = Icons.Filled.Phone,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilTextField(
                        value = perfilViewModel.edad,
                        onValueChange = { perfilViewModel.edad = it },
                        label = "Edad",
                        icon = Icons.Filled.Cake,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilTextField(
                        value = perfilViewModel.peso,
                        onValueChange = { perfilViewModel.peso = it },
                        label = "Peso (kg)",
                        icon = Icons.Filled.Scale,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    PerfilTextField(
                        value = perfilViewModel.altura,
                        onValueChange = { perfilViewModel.altura = it },
                        label = "Altura (cm)",
                        icon = Icons.Filled.Height,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.objetivo,
                    onValueChange = { perfilViewModel.objetivo = it },
                    label = "Objetivo",
                    icon = Icons.Filled.Flag,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                if (perfilViewModel.statusMessage.isNotEmpty()) {
                    val color = if (perfilViewModel.statusMessage.contains("Error") || perfilViewModel.statusMessage.contains("obligatorios")) Color.Red else MaterialTheme.colorScheme.primary
                    Text(
                        text = perfilViewModel.statusMessage,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    enabled = !perfilViewModel.isLoading,
                    onClick = {
                        perfilViewModel.guardarCambios()
                    }
                ) {
                    if (perfilViewModel.isLoading) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "Guardar Cambios",
                            style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                        .background(brush = AppBrushes.AccentGradient, shape = RoundedCornerShape(12.dp))
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                onClick = {
                    loginViewModel.logout()
                    navController.navigate(PantallaLogin) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text(
                    text = "Cerrar Sesión",
                    style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PerfilTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(brush = AppBrushes.MainGradient),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.titleSmall.copy(brush = AppBrushes.AccentGradient)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    )
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

