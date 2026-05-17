package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalView(
    navController: NavController,
    dietaViewModel: DietaViewModel = viewModel(),
    rutinaViewModel: RutinaViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        dietaViewModel.cargarDietas()
        rutinaViewModel.cargarRutinas()
    }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    var selectedTabIndex by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Dietas", "Rutinas")

    var showCrearRutinaDialog by remember { mutableStateOf(false) }
    var showCrearDietaDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Personal",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            brush = AppBrushes.AccentGradient
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { 
                        if (selectedTabIndex == 0) dietaViewModel.cargarDietas(force = true) 
                        else rutinaViewModel.cargarRutinas(force = true) 
                    }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar",
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
        },
        floatingActionButton = {
            if (selectedTabIndex == 0) {
                FloatingActionButton(
                    onClick = { showCrearDietaDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Dieta")
                }
            } else if (selectedTabIndex == 1) {
                FloatingActionButton(
                    onClick = { showCrearRutinaDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Agregar Rutina")
                }
            }
        }
    ) { innerPadding ->
        NutriGridBackground(modifier = Modifier.padding(innerPadding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { 
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                ) 
                            }
                        )
                    }
                }

                if (selectedTabIndex == 0) {
                    // Contenido Dietas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        if (dietaViewModel.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        dietaViewModel.error?.let { errorMsg ->
                            item {
                                Text(
                                    text = errorMsg,
                                    style = TextStyle(
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }

                        if (!dietaViewModel.isLoading && dietaViewModel.error == null && dietaViewModel.dietas.isEmpty()) {
                            item {
                                Text(
                                    text = "No tienes dietas asignadas",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 40.dp)
                                )
                            }
                        }

                        items(dietaViewModel.dietas) { dieta ->
                            DietaCard(
                                dieta = dieta,
                                onClick = {
                                    navController.navigate(PantallaDetalleDieta(dietaId = dieta.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // padding for fab spacing if needed later
                        }
                    }
                } else {
                    // Contenido Rutinas
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item { Spacer(modifier = Modifier.height(8.dp)) }

                        if (rutinaViewModel.isLoading) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }

                        rutinaViewModel.error?.let { errorMsg ->
                            item {
                                Text(
                                    text = errorMsg,
                                    style = TextStyle(
                                        color = Color.Red,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            }
                        }

                        if (!rutinaViewModel.isLoading && rutinaViewModel.error == null && rutinaViewModel.rutinas.isEmpty()) {
                            item {
                                Text(
                                    text = "No tienes rutinas asignadas",
                                    style = TextStyle(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 16.sp
                                    ),
                                    modifier = Modifier.padding(vertical = 40.dp)
                                )
                            }
                        }

                        items(rutinaViewModel.rutinas) { rutina ->
                            RutinaCard(
                                rutina = rutina,
                                onClick = {
                                    navController.navigate(PantallaDetalleRutina(rutinaId = rutina.id))
                                }
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        item {
                            Spacer(modifier = Modifier.height(80.dp)) // padding for fab
                        }
                    }
                }
            }
        }
        
        if (showCrearRutinaDialog) {
            var nombreRutina by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { 
                    if (!isSubmitting) showCrearRutinaDialog = false 
                },
                title = { Text("Nueva Rutina") },
                text = {
                    Column {
                        Button(
                            onClick = { 
                                showCrearRutinaDialog = false
                                navController.navigate(PantallaQrScanner) 
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer, 
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(imageVector = Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear código QR", fontWeight = FontWeight.Bold)
                        }
                        
                        Text(
                            text = "O crear manualmente:", 
                            style = MaterialTheme.typography.bodyMedium, 
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = nombreRutina,
                            onValueChange = { nombreRutina = it },
                            label = { Text("Nombre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSubmitting
                        )
                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nombreRutina.isNotBlank()) {
                                isSubmitting = true
                                errorMsg = null
                                rutinaViewModel.crearRutina(nombreRutina) { success, msg ->
                                    isSubmitting = false
                                    if (success) {
                                        showCrearRutinaDialog = false
                                    } else {
                                        errorMsg = msg ?: "Error al crear la rutina"
                                    }
                                }
                            } else {
                                errorMsg = "El nombre no puede estar vacío"
                            }
                        },
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCrearRutinaDialog = false },
                        enabled = !isSubmitting
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
        
        if (showCrearDietaDialog) {
            var nombreDieta by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { 
                    if (!isSubmitting) showCrearDietaDialog = false 
                },
                title = { Text("Nueva Dieta") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = nombreDieta,
                            onValueChange = { nombreDieta = it },
                            label = { Text("Nombre de la Dieta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSubmitting
                        )
                        if (errorMsg != null) {
                            Text(
                                text = errorMsg!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (nombreDieta.isNotBlank()) {
                                isSubmitting = true
                                errorMsg = null
                                dietaViewModel.crearDieta(nombreDieta) { success, msg ->
                                    isSubmitting = false
                                    if (success) {
                                        showCrearDietaDialog = false
                                    } else {
                                        errorMsg = msg ?: "Error al crear la dieta"
                                    }
                                }
                            } else {
                                errorMsg = "El nombre no puede estar vacío"
                            }
                        },
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showCrearDietaDialog = false },
                        enabled = !isSubmitting
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
