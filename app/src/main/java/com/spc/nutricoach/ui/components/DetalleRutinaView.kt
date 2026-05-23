package com.spc.nutricoach.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.util.QrUtils
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRutinaView(
    navController: NavController,
    rutinaId: String,
    rutinaViewModel: RutinaViewModel
) {
    val rutina = rutinaViewModel.rutinas.find { it.id == rutinaId } ?: rutinaViewModel.rutinasPublicas.find { it.id == rutinaId }

    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var currentClienteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(rutinaId) {
        currentClienteId = sessionManager.getClienteId()
        if (rutina == null) {
            rutinaViewModel.cargarRutinaPorId(rutinaId)
        }
    }
    
    val isReadOnly = currentClienteId != null && rutina?.clienteId != currentClienteId

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Rutina") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!isReadOnly && rutina != null) {
                        var expanded by remember { mutableStateOf(false) }
                        var showEditDialog by remember { mutableStateOf(false) }
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "Opciones")
                        }
                        
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Editar nombre") },
                                onClick = { 
                                    expanded = false
                                    showEditDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) }
                            )
                            DropdownMenuItem(
                                text = { Text("Eliminar rutina", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expanded = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }

                        if (showEditDialog) {
                            var newName by remember { mutableStateOf(rutina.nombre) }
                            var isSubmitting by remember { mutableStateOf(false) }
                            AlertDialog(
                                onDismissRequest = { if (!isSubmitting) showEditDialog = false },
                                title = { Text("Editar nombre") },
                                text = {
                                    OutlinedTextField(
                                        value = newName,
                                        onValueChange = { newName = it },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            if (newName.isNotBlank()) {
                                                isSubmitting = true
                                                rutinaViewModel.modificarNombreRutina(rutina.id, newName) { success, _ ->
                                                    isSubmitting = false
                                                    if (success) showEditDialog = false
                                                }
                                            }
                                        },
                                        enabled = !isSubmitting
                                    ) { Text("Guardar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showEditDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }

                        if (showDeleteDialog) {
                            var isDeleting by remember { mutableStateOf(false) }
                            AlertDialog(
                                onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
                                title = { Text("Eliminar rutina") },
                                text = { Text("¿Estás seguro de que deseas eliminar esta rutina? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            isDeleting = true
                                            rutinaViewModel.eliminarRutina(rutina.id) { success, _ ->
                                                isDeleting = false
                                                if (success) {
                                                    showDeleteDialog = false
                                                    navController.popBackStack()
                                                }
                                            }
                                        },
                                        enabled = !isDeleting,
                                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                    ) { Text("Eliminar") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                                }
                            )
                        }
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
        if (rutina == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                if (rutinaViewModel.isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                } else {
                    Text("Error: Rutina no encontrada")
                }
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = rutina.nombre,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        brush = AppBrushes.MainGradient
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (!isReadOnly) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Hacer pública",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        var isToggling by remember { mutableStateOf(false) }
                        Switch(
                            checked = rutina.publica,
                            onCheckedChange = { isPublica ->
                                isToggling = true
                                rutinaViewModel.toggleRutinaPublica(rutina.id, isPublica) { _, _ ->
                                    isToggling = false
                                }
                            },
                            enabled = !isToggling,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Botón Compartir QR
                    var showQrDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showQrDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Compartir QR", fontWeight = FontWeight.Bold)
                    }

                    if (showQrDialog) {
                        val qrBitmap = remember(rutina.id) {
                            QrUtils.generateQrBitmap(rutina.id)
                        }
                        AlertDialog(
                            onDismissRequest = { showQrDialog = false },
                            title = {
                                Text(
                                    text = "QR de Rutina",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            },
                            text = {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = "QR de la rutina",
                                        modifier = Modifier.size(250.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Escanea este código desde otra cuenta para copiar la rutina",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            },
                            confirmButton = {
                                TextButton(onClick = { showQrDialog = false }) {
                                    Text("Cerrar")
                                }
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = " ${rutina.dias.size} días de entrenamiento",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (!rutina.notasGenerales.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notas Generales",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rutina.notasGenerales,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Días de Entrenamiento",
                    style = MaterialTheme.typography.titleLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            if (rutina.dias.isNotEmpty()) {
                val diasOrdenados = rutina.dias.sortedBy { it.orden }
                items(diasOrdenados.size) { index ->
                    val dia = diasOrdenados[index]
                    val diaIndexOriginal = rutina.dias.indexOf(dia) // Obtener indice real
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DiaItemDetail(dia = dia, diaIndex = diaIndexOriginal, rutinaId = rutina.id, rutinaViewModel = rutinaViewModel, isReadOnly = isReadOnly)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    navController.navigate(PantallaEntrenamientoDia(rutina.id, dia.nombre))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FitnessCenter,
                                    contentDescription = "Empezar",
                                    tint = Color.Black
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Comenzar Entrenamiento", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "No hay días asignados.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            if (!isReadOnly) {
                item {
                    var showAddDiaDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showAddDiaDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Añadir Día", fontWeight = FontWeight.Bold)
                }

                if (showAddDiaDialog) {
                    var nombreDia by remember { mutableStateOf("") }
                    var isSubmitting by remember { mutableStateOf(false) }

                    AlertDialog(
                        onDismissRequest = { if (!isSubmitting) showAddDiaDialog = false },
                        title = { Text("Nuevo Día") },
                        text = {
                            OutlinedTextField(
                                value = nombreDia,
                                onValueChange = { nombreDia = it },
                                label = { Text("Nombre del día (ej. Día de Pierna)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSubmitting
                            )
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    if (nombreDia.isNotBlank()) {
                                        isSubmitting = true
                                        rutinaViewModel.agregarDia(rutina.id, nombreDia) { _, _ ->
                                            isSubmitting = false
                                            showAddDiaDialog = false
                                        }
                                    }
                                },
                                enabled = !isSubmitting
                            ) {
                                if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                else Text("Guardar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showAddDiaDialog = false }, enabled = !isSubmitting) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            } else {
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun DiaItemDetail(dia: Dia, diaIndex: Int, rutinaId: String, rutinaViewModel: RutinaViewModel, isReadOnly: Boolean = false) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.FitnessCenter,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = dia.nombre,
                    style = MaterialTheme.typography.titleLarge.copy(
                        brush = AppBrushes.MainGradient
                    )
                )
                if (!dia.enfoque.isNullOrBlank()) {
                    Text(
                        text = "· ${dia.enfoque}",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            if (!isReadOnly) {
                var showDeleteDiaDialog by remember { mutableStateOf(false) }
                IconButton(onClick = { showDeleteDiaDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Eliminar Día", tint = MaterialTheme.colorScheme.error)
                }
                if (showDeleteDiaDialog) {
                    AlertDialog(
                        onDismissRequest = { showDeleteDiaDialog = false },
                        title = { Text("Eliminar Día") },
                        text = { Text("¿Estás seguro de eliminar el día '${dia.nombre}'?") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    rutinaViewModel.eliminarDia(rutinaId, diaIndex) { _, _ -> }
                                    showDeleteDiaDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) { Text("Eliminar") }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDeleteDiaDialog = false }) { Text("Cancelar") }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        dia.ejercicios.forEachIndexed { index, ejercicio ->
            val uniqueKey = "${dia.nombre}_${index}_${ejercicio.nombreSnapshot}"
            val weightFlow = remember(rutinaId, uniqueKey) {
                rutinaViewModel.getExerciseWeightFlow(rutinaId, uniqueKey)
            }
            val savedWeight by weightFlow.collectAsState(initial = "")
            
            var localWeight by remember(savedWeight) { mutableStateOf(savedWeight ?: "") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• ${ejercicio.nombreSnapshot}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (!isReadOnly) {
                            var showDeleteEjDialog by remember { mutableStateOf(false) }
                            IconButton(
                                onClick = { showDeleteEjDialog = true },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                            if (showDeleteEjDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteEjDialog = false },
                                    title = { Text("Eliminar Ejercicio") },
                                    text = { Text("¿Eliminar '${ejercicio.nombreSnapshot}'?") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                rutinaViewModel.eliminarEjercicio(rutinaId, diaIndex, index) { _, _ -> }
                                                showDeleteEjDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) { Text("Eliminar") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteEjDialog = false }) { Text("Cancelar") }
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${ejercicio.series} series × ${ejercicio.repeticiones} reps" +
                            (if (!ejercicio.descanso.isNullOrBlank()) " | ⏱ ${ejercicio.descanso}s" else "") +
                            (if (!ejercicio.rir.isNullOrBlank()) " | RIR: ${ejercicio.rir}" else ""),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    )

                    if (!ejercicio.notas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💡 ${ejercicio.notas}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }

                    if (!isReadOnly) {
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Peso a levantar (kg):",
                                style = TextStyle(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            OutlinedTextField(
                                value = localWeight,
                                onValueChange = { 
                                    localWeight = it
                                    rutinaViewModel.saveExerciseWeight(rutinaId, uniqueKey, it)
                                },
                                placeholder = { Text("0") },
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(56.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }
                }
            }
        }

        if (!isReadOnly) {
            Spacer(modifier = Modifier.height(16.dp))

            var showAddEjercicioDialog by remember { mutableStateOf(false) }
            
            Button(
                onClick = { showAddEjercicioDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Añadir Ejercicio", fontWeight = FontWeight.Bold)
            }

            if (showAddEjercicioDialog) {
                var nombreEj by remember { mutableStateOf("") }
                var seriesEj by remember { mutableStateOf("") }
                var repsEj by remember { mutableStateOf("") }
                var descansoEj by remember { mutableStateOf("") }
                var isSubmitting by remember { mutableStateOf(false) }
                var errorText by remember { mutableStateOf<String?>(null) }

                AlertDialog(
                    onDismissRequest = { if (!isSubmitting) showAddEjercicioDialog = false },
                    title = { Text("Nuevo Ejercicio") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nombreEj,
                                onValueChange = { 
                                    nombreEj = it 
                                    errorText = null
                                },
                                label = { Text("Nombre del Ejercicio") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSubmitting
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = seriesEj,
                                    onValueChange = { 
                                        seriesEj = it 
                                        errorText = null
                                    },
                                    label = { Text("Series") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSubmitting
                                )
                                OutlinedTextField(
                                    value = repsEj,
                                    onValueChange = { 
                                        repsEj = it 
                                        errorText = null
                                    },
                                    label = { Text("Reps") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    enabled = !isSubmitting
                                )
                            }
                            OutlinedTextField(
                                value = descansoEj,
                                onValueChange = { 
                                    descansoEj = it 
                                    errorText = null
                                },
                                label = { Text("Descanso (s)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isSubmitting
                            )
                            if (errorText != null) {
                                Text(
                                    text = errorText!!,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nombreEj.isNotBlank() && seriesEj.isNotBlank() && repsEj.isNotBlank()) {
                                    isSubmitting = true
                                    errorText = null
                                    rutinaViewModel.agregarEjercicio(
                                        rutinaId = rutinaId,
                                        diaIndex = diaIndex,
                                        nombre = nombreEj,
                                        series = seriesEj.toIntOrNull() ?: 0,
                                        repeticiones = repsEj.toIntOrNull() ?: 0,
                                        descanso = descansoEj.toIntOrNull() ?: 0
                                    ) { success, msg ->
                                        isSubmitting = false
                                        if (success) {
                                            showAddEjercicioDialog = false
                                        } else {
                                            errorText = msg ?: "Error al agregar el ejercicio"
                                        }
                                    }
                                } else {
                                    errorText = "Por favor, completa los campos obligatorios"
                                }
                            },
                            enabled = !isSubmitting
                        ) {
                            if (isSubmitting) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Guardar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddEjercicioDialog = false }, enabled = !isSubmitting) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}
