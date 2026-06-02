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
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.ui.graphics.asImageBitmap
import com.spc.nutricoach.util.QrUtils
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.spc.nutricoach.data.SessionManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.text.style.TextAlign
import com.spc.nutricoach.model.Comida
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.DietaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDietaView(
    navController: NavController,
    dietaId: String,
    dietaViewModel: DietaViewModel,
    forceReadOnly: Boolean = false
) {
    val dieta = dietaViewModel.dietas.find { it.id == dietaId } ?: dietaViewModel.dietasPublicas.find { it.id == dietaId }
    val completedMeals by dietaViewModel.completedMealsFlow.collectAsState(initial = emptySet())
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var currentClienteId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(dietaId) {
        currentClienteId = sessionManager.getClienteId()
        if (dieta == null) {
            dietaViewModel.cargarDietaPorId(dietaId)
        }
    }
    
    val isReadOnly = forceReadOnly || (currentClienteId != null && dieta?.clienteId != currentClienteId)
    val clientId = currentClienteId

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Dieta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    if (!isReadOnly && dieta != null) {
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
                                text = { Text("Eliminar dieta", color = MaterialTheme.colorScheme.error) },
                                onClick = { 
                                    expanded = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                            )
                        }

                        if (showEditDialog) {
                            var newName by remember { mutableStateOf(dieta.nombre) }
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
                                                dietaViewModel.modificarNombreDieta(dieta.id, newName) { success, _ ->
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
                                title = { Text("Eliminar dieta") },
                                text = { Text("¿Estás seguro de que deseas eliminar esta dieta? Esta acción no se puede deshacer.") },
                                confirmButton = {
                                    Button(
                                        onClick = {
                                            isDeleting = true
                                            dietaViewModel.eliminarDieta(dieta.id) { success, _ ->
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
        if (dieta == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Error: Dieta no encontrada")
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
                    text = dieta.nombre,
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
                            checked = dieta.publica,
                            onCheckedChange = { isPublica ->
                                isToggling = true
                                dietaViewModel.toggleDietaPublica(dieta.id, isPublica) { _, _ ->
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
                        val qrBitmap = remember(dieta.id) {
                            QrUtils.generateDietaQrBitmap(dieta.id)
                        }
                        AlertDialog(
                            onDismissRequest = { showQrDialog = false },
                            title = {
                                Text(
                                    text = "QR de Dieta",
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
                                        contentDescription = "QR de la dieta",
                                        modifier = Modifier.size(250.dp)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "Escanea este código desde otra cuenta para copiar la dieta",
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
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = " ${dieta.kcalObjetivo} kcal/día recomendadas",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                if (!dieta.notasGenerales.isNullOrBlank()) {
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
                        text = dieta.notasGenerales,
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comidas",
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    TextButton(onClick = { dietaViewModel.clearDietMeals(dietaId, dieta.comidas) }) {
                        Text("Reiniciar", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (dieta.comidas.isNotEmpty()) {
                val comidasOrdenadas = dieta.comidas.sortedBy { it.orden }
                items(comidasOrdenadas.size) { index ->
                    val comida = comidasOrdenadas[index]
                    val mealKey = "${clientId}_${dietaId}_${comida.nombre}"
                    val isCompleted = completedMeals.contains(mealKey)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ComidaItemDetail(
                                comida = comida,
                                isCompleted = isCompleted,
                                onCheckedChange = { checked ->
                                    dietaViewModel.toggleMeal(dietaId, comida.nombre, checked)
                                },
                                isReadOnly = isReadOnly,
                                onDeleteComida = {
                                    dietaViewModel.eliminarComida(dietaId, index) { _, _ -> }
                                },
                                onDeleteAlimento = { alimentoIndex ->
                                    dietaViewModel.eliminarAlimento(dietaId, index, alimentoIndex) { _, _ -> }
                                },
                                onAddAlimento = { nombre, cantidad, unidad ->
                                    dietaViewModel.agregarAlimento(dietaId, index, nombre, cantidad, unidad) { _, _ -> }
                                }
                            )
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "No hay comidas asignadas.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            
            if (!isReadOnly) {
                item {
                    var showAddComidaDialog by remember { mutableStateOf(false) }
                    Button(
                        onClick = { showAddComidaDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Añadir Comida", fontWeight = FontWeight.Bold)
                    }

                    if (showAddComidaDialog) {
                        var nombreComida by remember { mutableStateOf("") }
                        var isSubmitting by remember { mutableStateOf(false) }

                        AlertDialog(
                            onDismissRequest = { if (!isSubmitting) showAddComidaDialog = false },
                            title = { Text("Nueva Comida") },
                            text = {
                                OutlinedTextField(
                                    value = nombreComida,
                                    onValueChange = { nombreComida = it },
                                    label = { Text("Nombre (ej. Desayuno)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    enabled = !isSubmitting
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        if (nombreComida.isNotBlank()) {
                                            isSubmitting = true
                                            dietaViewModel.agregarComida(dietaId, nombreComida) { _, _ ->
                                                isSubmitting = false
                                                showAddComidaDialog = false
                                            }
                                        }
                                    },
                                    enabled = !isSubmitting
                                ) { Text("Guardar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showAddComidaDialog = false }, enabled = !isSubmitting) {
                                    Text("Cancelar")
                                }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                }
            } else {
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun ComidaItemDetail(
    comida: Comida,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isReadOnly: Boolean = false,
    onDeleteComida: () -> Unit = {},
    onDeleteAlimento: (Int) -> Unit = {},
    onAddAlimento: (String, Double, String) -> Unit = { _, _, _ -> }
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Filled.Restaurant,
                    contentDescription = null,
                    tint = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = comida.nombre,
                    style = if (isCompleted) {
                        MaterialTheme.typography.titleLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        MaterialTheme.typography.titleLarge.copy(
                            brush = AppBrushes.MainGradient
                        )
                    }
                )
                if (!comida.horaSugerida.isNullOrBlank()) {
                    Text(
                        text = "· ${comida.horaSugerida}",
                        style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isReadOnly) {
                    var showDeleteDialog by remember { mutableStateOf(false) }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Eliminar Comida", tint = MaterialTheme.colorScheme.error)
                    }
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            title = { Text("Eliminar Comida") },
                            text = { Text("¿Eliminar '${comida.nombre}'?") },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        onDeleteComida()
                                        showDeleteDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) { Text("Eliminar") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
                            }
                        )
                    }
                }
                IconToggleButton(
                    checked = isCompleted,
                    onCheckedChange = onCheckedChange
                ) {
                    Icon(
                        imageVector = if (isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        comida.alimentos.forEachIndexed { alimentoIndex, alimento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val c = alimento.cantidad
                    val formatCantidad = if (c % 1.0 == 0.0) c.toInt().toString() else c.toString()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "• ${alimento.nombreSnapshot}",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        if (!isReadOnly) {
                            var showDeleteAlimentoDialog by remember { mutableStateOf(false) }
                            IconButton(onClick = { showDeleteAlimentoDialog = true }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Filled.Delete, contentDescription = "Eliminar Alimento", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                            }
                            if (showDeleteAlimentoDialog) {
                                AlertDialog(
                                    onDismissRequest = { showDeleteAlimentoDialog = false },
                                    title = { Text("Eliminar Alimento") },
                                    text = { Text("¿Eliminar '${alimento.nombreSnapshot}'?") },
                                    confirmButton = {
                                        Button(
                                            onClick = {
                                                onDeleteAlimento(alimentoIndex)
                                                showDeleteAlimentoDialog = false
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                        ) { Text("Eliminar") }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { showDeleteAlimentoDialog = false }) { Text("Cancelar") }
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$formatCantidad ${alimento.unidad}",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
        
        if (!isReadOnly) {
            Spacer(modifier = Modifier.height(16.dp))
            var showAddAlimentoDialog by remember { mutableStateOf(false) }
            
            Button(
                onClick = { showAddAlimentoDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Añadir Alimento", fontWeight = FontWeight.Bold)
            }

            if (showAddAlimentoDialog) {
                var nombreAlimento by remember { mutableStateOf("") }
                var cantidadAlimento by remember { mutableStateOf("") }
                var unidadAlimento by remember { mutableStateOf("") }

                AlertDialog(
                    onDismissRequest = { showAddAlimentoDialog = false },
                    title = { Text("Nuevo Alimento") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = nombreAlimento,
                                onValueChange = { nombreAlimento = it },
                                label = { Text("Nombre") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = cantidadAlimento,
                                    onValueChange = { cantidadAlimento = it },
                                    label = { Text("Cantidad") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = unidadAlimento,
                                    onValueChange = { unidadAlimento = it },
                                    label = { Text("Unidad (ej. g, ml)") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (nombreAlimento.isNotBlank() && cantidadAlimento.isNotBlank()) {
                                    val cant = cantidadAlimento.toDoubleOrNull() ?: 0.0
                                    onAddAlimento(nombreAlimento, cant, unidadAlimento)
                                    showAddAlimentoDialog = false
                                }
                            }
                        ) { Text("Guardar") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAddAlimentoDialog = false }) { Text("Cancelar") }
                    }
                )
            }
        }
    }
}
