package com.spc.nutricoach.ui.components

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.SecondaryTeal
import com.spc.nutricoach.ui.viewmodel.DietaViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonalView(
    navController: NavController,
    dietaViewModel: DietaViewModel = viewModel(),
    rutinaViewModel: RutinaViewModel = viewModel(),
    feedViewModel: com.spc.nutricoach.ui.viewmodel.FeedViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        dietaViewModel.cargarDietas()
        rutinaViewModel.cargarRutinas()
    }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    val tabs = listOf("Dietas", "Rutinas", "Seguimiento", "Tus Posts")

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { tabs.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val selectedTabIndex = pagerState.currentPage

    var showCrearRutinaDialog by remember { mutableStateOf(false) }
    var showCrearDietaDialog by remember { mutableStateOf(false) }

    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 2) {
            rutinaViewModel.cargarHistorialEntrenamientos()
        } else if (selectedTabIndex == 3) {
            feedViewModel.cargarPosts(force = true)
        }
    }

    val fotoPerfilUrl by sessionManager.userFotoPerfilFlow.collectAsState(initial = null)

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
                        if (!fotoPerfilUrl.isNullOrEmpty()) {
                            AsyncImage(
                                model = fotoPerfilUrl,
                                contentDescription = "Foto de perfil",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = letraInicial,
                                style = TextStyle(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            )
                        }
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
                            onClick = { 
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            },
                            text = { 
                                Text(
                                    text = title,
                                    color = if (selectedTabIndex == index) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    maxLines = 1,
                                    softWrap = false
                                ) 
                            }
                        )
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) { page ->
                    when (page) {
                        0 -> {
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
                                            text = "No tienes dietas",
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
                        }
                        1 -> {
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
                                            text = "No tienes rutinas",
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
                        2 -> {
                            // Contenido Seguimiento
                            val ejerciciosUnicos = remember(rutinaViewModel.historialEntrenamientos) {
                                val allNames = rutinaViewModel.historialEntrenamientos
                                    .flatMap { it.ejercicios }
                                    .map { it.nombre_snapshot }
                                com.spc.nutricoach.util.ExerciseNormalizer.getCanonicalList(allNames)
                            }
                            var ejercicioSeleccionado by remember { mutableStateOf<String?>(null) }
                            var searchQuery by remember { mutableStateOf("") }
                            var rangoSeleccionado by remember { mutableStateOf("Todo") }

                            // Parsear el historial de entrenamientos agrupándolo por LocalDate localmente
                            val completedDatesMap = remember(rutinaViewModel.historialEntrenamientos) {
                                rutinaViewModel.historialEntrenamientos.groupBy { log ->
                                    try {
                                        val dateStr = log.fecha.substringBefore("T")
                                        LocalDate.parse(dateStr)
                                    } catch (e: Exception) {
                                        null
                                    }
                                }.filterKeys { it != null } as Map<LocalDate, List<com.spc.nutricoach.data.EntrenamientoLog>>
                            }

                            var activeYearMonth by remember { mutableStateOf(YearMonth.now()) }
                            var selectedDate by remember { mutableStateOf<LocalDate?>(LocalDate.now()) }

                            val calendarDays = remember(activeYearMonth) {
                                val firstDayOfMonth = activeYearMonth.atDay(1)
                                val dayOfWeekVal = firstDayOfMonth.dayOfWeek.value // 1 (Mon) to 7 (Sun)
                                
                                // Espacios en blanco al inicio del mes (si no empieza el lunes)
                                val leadingEmptySpaces = dayOfWeekVal - 1
                                val totalDaysInMonth = activeYearMonth.lengthOfMonth()
                                
                                val daysList = mutableListOf<LocalDate?>()
                                repeat(leadingEmptySpaces) {
                                    daysList.add(null)
                                }
                                for (day in 1..totalDaysInMonth) {
                                    daysList.add(activeYearMonth.atDay(day))
                                }
                                
                                val remaining = daysList.size % 7
                                if (remaining > 0) {
                                    repeat(7 - remaining) {
                                        daysList.add(null)
                                    }
                                }
                                
                                daysList.chunked(7)
                            }

                            val ejerciciosFiltrados = remember(searchQuery, ejerciciosUnicos) {
                                if (searchQuery.isBlank()) ejerciciosUnicos
                                else ejerciciosUnicos.filter { it.contains(searchQuery, ignoreCase = true) }
                            }

                            // Auto-seleccionar primer elemento filtrado si cambia
                            LaunchedEffect(ejerciciosFiltrados) {
                                if (ejercicioSeleccionado == null || !ejerciciosFiltrados.contains(ejercicioSeleccionado)) {
                                    ejercicioSeleccionado = ejerciciosFiltrados.firstOrNull()
                                }
                            }
                            
                            if (ejercicioSeleccionado == null && ejerciciosFiltrados.isNotEmpty()) {
                                ejercicioSeleccionado = ejerciciosFiltrados.first()
                            }

                            val chartData = remember(ejercicioSeleccionado, rutinaViewModel.historialEntrenamientos, rangoSeleccionado) {
                                val currentEjer = ejercicioSeleccionado
                                if (currentEjer == null) emptyList<Pair<String, Double>>()
                                else {
                                    val targetNormalized = com.spc.nutricoach.util.ExerciseNormalizer.normalize(currentEjer)
                                    val limitDate = when (rangoSeleccionado) {
                                        "1M" -> LocalDate.now().minusMonths(1)
                                        "6M" -> LocalDate.now().minusMonths(6)
                                        "1A" -> LocalDate.now().minusYears(1)
                                        else -> null
                                    }

                                    rutinaViewModel.historialEntrenamientos
                                        .filter { ent ->
                                            val hasExercise = ent.ejercicios.any { com.spc.nutricoach.util.ExerciseNormalizer.normalize(it.nombre_snapshot) == targetNormalized }
                                            if (!hasExercise) return@filter false

                                            if (limitDate != null) {
                                                val logDate = try {
                                                    LocalDate.parse(ent.fecha.substringBefore("T"))
                                                } catch (e: Exception) {
                                                    null
                                                }
                                                logDate != null && !logDate.isBefore(limitDate)
                                            } else {
                                                true
                                            }
                                        }
                                        .map { ent ->
                                            val ejer = ent.ejercicios.first { com.spc.nutricoach.util.ExerciseNormalizer.normalize(it.nombre_snapshot) == targetNormalized }
                                            val maxPeso = ejer.series.map { it.peso }.maxOrNull() ?: 0.0
                                            val fechaCorta = try {
                                                val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                                                val date = sdfIn.parse(ent.fecha)
                                                val sdfOut = java.text.SimpleDateFormat("dd/MM", java.util.Locale.getDefault())
                                                if (date != null) sdfOut.format(date) else ""
                                            } catch (e: Exception) {
                                                ""
                                            }
                                            Pair(fechaCorta, maxPeso)
                                        }
                                        .reversed()
                                }
                            }

                            val mesNombre = remember(activeYearMonth) {
                                val format = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))
                                activeYearMonth.format(format).replaceFirstChar { it.uppercase() }
                            }

                            val fechaFormateada = remember(selectedDate) {
                                if (selectedDate == null) "" else {
                                    val format = DateTimeFormatter.ofPattern("dd 'de' MMMM, yyyy", Locale("es", "ES"))
                                    selectedDate!!.format(format)
                                }
                            }

                            val entrenamientosDelDiaSeleccionado = remember(selectedDate, completedDatesMap) {
                                if (selectedDate == null) emptyList() else completedDatesMap[selectedDate] ?: emptyList()
                            }

                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                item { Spacer(modifier = Modifier.height(8.dp)) }

                                if (rutinaViewModel.isLoadingHistorial) {
                                    item {
                                        Box(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        }
                                    }
                                }

                                rutinaViewModel.errorHistorial?.let { errorMsg ->
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

                                if (!rutinaViewModel.isLoadingHistorial && rutinaViewModel.errorHistorial == null && rutinaViewModel.historialEntrenamientos.isEmpty()) {
                                    item {
                                        Text(
                                            text = "No has completado ningún entrenamiento todavía",
                                            style = TextStyle(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontSize = 16.sp
                                            ),
                                            modifier = Modifier.padding(vertical = 40.dp)
                                        )
                                    }
                                }

                                if (rutinaViewModel.historialEntrenamientos.isNotEmpty()) {
                                    item {
                                        Text(
                                            text = "Evolución por Ejercicio",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                        )
                                    }

                                    // Buscador de ejercicio interactivo
                                    item {
                                        OutlinedTextField(
                                            value = searchQuery,
                                            onValueChange = { searchQuery = it },
                                            placeholder = { Text("Buscar ejercicio...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            singleLine = true,
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            ),
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = "Buscar",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            },
                                            trailingIcon = {
                                                if (searchQuery.isNotEmpty()) {
                                                    IconButton(onClick = { searchQuery = "" }) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Limpiar",
                                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                    }
                                                }
                                            }
                                        )
                                    }

                                    item {
                                        LazyRow(
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 12.dp)
                                        ) {
                                            items(ejerciciosFiltrados) { ejer ->
                                                CustomExerciseChip(
                                                    text = ejer,
                                                    selected = ejercicioSeleccionado == ejer,
                                                    onClick = { ejercicioSeleccionado = ejer }
                                                )
                                            }
                                        }
                                    }

                                    item {
                                         RangeSelector(
                                             selectedRange = rangoSeleccionado,
                                             onRangeSelected = { rangoSeleccionado = it },
                                             modifier = Modifier.padding(bottom = 12.dp)
                                         )
                                     }

                                     item {
                                         EvolutionChart(
                                             points = chartData,
                                             modifier = Modifier.fillMaxWidth()
                                         )
                                     }

                                    // --- SECCIÓN DE CALENDARIO ---
                                    item {
                                        Text(
                                            text = "Calendario de Entrenamientos",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
                                        )
                                    }

                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            colors = CardDefaults.cardColors(
                                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                            ),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(16.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                // Cabecera: Mes, Año y Botones de Navegación
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    IconButton(
                                                        onClick = { activeYearMonth = activeYearMonth.minusMonths(1) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                            contentDescription = "Mes anterior",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }

                                                    Text(
                                                        text = mesNombre,
                                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                        color = MaterialTheme.colorScheme.onSurface
                                                    )

                                                    IconButton(
                                                        onClick = { activeYearMonth = activeYearMonth.plusMonths(1) }
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                                            contentDescription = "Mes siguiente",
                                                            tint = MaterialTheme.colorScheme.primary
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(12.dp))

                                                // Fila de Días de la Semana
                                                val diasSemana = listOf("L", "M", "X", "J", "V", "S", "D")
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    diasSemana.forEach { diaS ->
                                                        Text(
                                                            text = diaS,
                                                            modifier = Modifier.width(36.dp),
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                                        )
                                                    }
                                                }

                                                Spacer(modifier = Modifier.height(8.dp))

                                                // Cuadrícula del Calendario
                                                calendarDays.forEach { semana ->
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        semana.forEach { dia ->
                                                            if (dia == null) {
                                                                Spacer(modifier = Modifier.width(36.dp))
                                                            } else {
                                                                val entrenamientosDia = completedDatesMap[dia] ?: emptyList()
                                                                val haEntrenado = entrenamientosDia.isNotEmpty()
                                                                val esSeleccionado = selectedDate == dia
                                                                val esHoy = dia == LocalDate.now()

                                                                Box(
                                                                    modifier = Modifier
                                                                        .size(36.dp)
                                                                        .clip(CircleShape)
                                                                        .then(
                                                                            if (haEntrenado) Modifier.background(AppBrushes.MainGradient)
                                                                            else if (esHoy) Modifier.border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.6f), CircleShape)
                                                                            else Modifier
                                                                        )
                                                                        .then(
                                                                            if (esSeleccionado) Modifier.border(2.dp, if (haEntrenado) Color.White else MaterialTheme.colorScheme.primary, CircleShape)
                                                                            else Modifier
                                                                        )
                                                                        .clickable { selectedDate = dia },
                                                                    contentAlignment = Alignment.Center
                                                                ) {
                                                                    Text(
                                                                        text = "${dia.dayOfMonth}",
                                                                        color = if (haEntrenado) Color.Black else MaterialTheme.colorScheme.onSurface,
                                                                        fontSize = 13.sp,
                                                                        fontWeight = if (haEntrenado || esHoy || esSeleccionado) FontWeight.Bold else FontWeight.Normal
                                                                    )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // --- SECCIÓN DE DETALLE DE ENTRENAMIENTOS PARA EL DÍA SELECCIONADO ---
                                    item {
                                        Text(
                                            text = "Entrenamientos del $fechaFormateada",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp)
                                        )
                                    }

                                    if (entrenamientosDelDiaSeleccionado.isEmpty()) {
                                        item {
                                            Card(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 8.dp),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                                ),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.08f)),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Box(
                                                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No realizaste entrenamientos este día.\n¡Toca un día destacado en el calendario para ver detalles!",
                                                        style = TextStyle(
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                            fontSize = 14.sp,
                                                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        items(entrenamientosDelDiaSeleccionado) { log ->
                                            HistorialEntrenamientoCard(log = log)
                                        }
                                    }
                                }

                                item {
                                    Spacer(modifier = Modifier.height(80.dp))
                                }
                            }
                        }
                        3 -> {
                            val currentUserId by sessionManager.clienteIdFlow.collectAsState(initial = "")
                            TusPostsContent(
                                navController = navController,
                                feedViewModel = feedViewModel,
                                currentUserId = currentUserId ?: ""
                            )
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
            var kcalDieta by remember { mutableStateOf("") }
            var isSubmitting by remember { mutableStateOf(false) }
            var errorMsg by remember { mutableStateOf<String?>(null) }
            
            AlertDialog(
                onDismissRequest = { 
                    if (!isSubmitting) showCrearDietaDialog = false 
                },
                title = { Text("Nueva Dieta") },
                text = {
                    Column {
                        Button(
                            onClick = { 
                                showCrearDietaDialog = false
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
                            value = nombreDieta,
                            onValueChange = { nombreDieta = it },
                            label = { Text("Nombre de la Dieta") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            enabled = !isSubmitting
                        )

                        OutlinedTextField(
                            value = kcalDieta,
                            onValueChange = { kcalDieta = it },
                            label = { Text("Kcal diarias objetivo") },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                            singleLine = true,
                            enabled = !isSubmitting,
                            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
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
                                val kcalInt = kcalDieta.toIntOrNull() ?: 0
                                dietaViewModel.crearDieta(nombreDieta, kcalInt) { success, msg ->
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

@Composable
fun CustomExerciseChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val background = if (selected) AppBrushes.MainGradient else Brush.linearGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
    val textColor = if (selected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
    val borderStroke = if (selected) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(background)
            .clickable { onClick() }
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(20.dp)) else Modifier)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
fun RangeSelector(
    selectedRange: String,
    onRangeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val ranges = listOf(
        Pair("1M", "1 Mes"),
        Pair("6M", "6 Meses"),
        Pair("1A", "1 Año"),
        Pair("Todo", "Todo")
    )
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ranges.forEach { (key, label) ->
            val isSelected = selectedRange == key
            val background = if (isSelected) AppBrushes.MainGradient else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            val textColor = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(background)
                    .clickable { onRangeSelected(key) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun EvolutionChart(points: List<Pair<String, Double>>, modifier: Modifier = Modifier) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("No hay suficientes datos para graficar", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        return
    }

    val maxWeight = remember(points) {
        val maxVal = points.map { it.second }.maxOrNull() ?: 10.0
        if (maxVal <= 0.0) 10.0 else maxVal
    }
    val minWeight = 0.0
    val weightRange = remember(maxWeight) { maxWeight }
    
    // Animar la gráfica al cargar
    val animProgress = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(points) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.tween(1000, easing = androidx.compose.animation.core.FastOutSlowInEasing)
        )
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val tealColor = SecondaryTeal
    val gridColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.GRAY
            textSize = 28f
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Evolución de Carga Máxima (PR)",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(20.dp))
            
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .padding(bottom = 24.dp, start = 8.dp, end = 16.dp, top = 16.dp)
            ) {
                val width = size.width
                val height = size.height
                val paddingBottom = 55f
                val paddingTop = 20f
                val paddingLeft = 110f // Espacio interno para que las etiquetas no se corten
                val chartHeight = height - paddingBottom - paddingTop
                val chartWidth = width - paddingLeft
                
                val sizePoints = points.size
                val stepX = if (sizePoints > 1) chartWidth / (sizePoints - 1) else chartWidth
                
                // 1. Dibujar líneas de rejilla horizontal (3 divisiones)
                for (i in 0..2) {
                    val y = paddingTop + (chartHeight / 2) * i
                    drawLine(
                        color = gridColor,
                        start = Offset(paddingLeft, y),
                        end = Offset(width, y),
                        strokeWidth = 1f
                    )
                    
                    // Texto del peso en el eje Y (totalmente visible)
                    val value = maxWeight - (weightRange / 2.0) * i.toDouble()
                    drawContext.canvas.nativeCanvas.drawText(
                        String.format(java.util.Locale.getDefault(), "%.1f kg", value),
                        paddingLeft - 15f,
                        y + 8f,
                        textPaint.apply { textAlign = android.graphics.Paint.Align.RIGHT }
                    )
                }
                
                // 2. Construir los puntos de coordenadas (X, Y)
                val coordinates = points.mapIndexed { index, pair ->
                    val x = paddingLeft + index * stepX
                    val rawY = if (weightRange > 0.0) {
                        height - paddingBottom - (((pair.second - minWeight) / weightRange) * chartHeight).toFloat()
                    } else {
                        height / 2f
                    }
                    val animatedY = height - paddingBottom - (height - paddingBottom - rawY) * animProgress.value
                    Offset(x, animatedY)
                }
                
                // 3. Dibujar la curva suave con gradiente bajo la curva
                if (coordinates.isNotEmpty()) {
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(coordinates.first().x, coordinates.first().y)
                        for (i in 1 until coordinates.size) {
                            val p1 = coordinates[i - 1]
                            val p2 = coordinates[i]
                            val controlPoint1 = Offset(p1.x + (p2.x - p1.x) / 2f, p1.y)
                            val controlPoint2 = Offset(p1.x + (p2.x - p1.x) / 2f, p2.y)
                            cubicTo(controlPoint1.x, controlPoint1.y, controlPoint2.x, controlPoint2.y, p2.x, p2.y)
                        }
                    }
                    
                    // Gradiente de fondo bajo la curva
                    val fillPath = androidx.compose.ui.graphics.Path().apply {
                        addPath(path)
                        lineTo(coordinates.last().x, height - paddingBottom)
                        lineTo(coordinates.first().x, height - paddingBottom)
                        close()
                    }
                    
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.25f),
                                Color.Transparent
                            )
                        )
                    )
                    
                    // Línea de la gráfica
                    drawPath(
                        path = path,
                        brush = Brush.horizontalGradient(
                            colors = listOf(primaryColor, tealColor)
                        ),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                    
                    // 4. Dibujar puntos destacados y etiquetas de fecha
                    var lastLabelX = -1000f
                    coordinates.forEachIndexed { index, offset ->
                        // Resplandor del punto
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(primaryColor.copy(alpha = 0.4f), Color.Transparent),
                                center = offset,
                                radius = 12.dp.toPx()
                            ),
                            radius = 12.dp.toPx(),
                            center = offset
                        )
                        
                        // Punto central
                        drawCircle(
                            color = Color.White,
                            radius = 4.dp.toPx(),
                            center = offset
                        )
                        drawCircle(
                            color = primaryColor,
                            radius = 4.dp.toPx(),
                            center = offset,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx())
                        )
                        
                        // Etiqueta de la fecha en el eje X
                        // Filtramos inteligentemente para evitar superposición
                        val minDistanceBetweenLabels = 145f
                        val isLast = index == coordinates.size - 1
                        val shouldDrawLabel = when {
                            index == 0 -> true
                            isLast -> (offset.x - lastLabelX) >= minDistanceBetweenLabels * 0.8f
                            else -> (offset.x - lastLabelX) >= minDistanceBetweenLabels && (coordinates.last().x - offset.x) >= minDistanceBetweenLabels * 0.8f
                        }
                        
                        if (shouldDrawLabel) {
                            drawContext.canvas.nativeCanvas.drawText(
                                points[index].first,
                                offset.x,
                                height - 12f,
                                textPaint.apply { textAlign = android.graphics.Paint.Align.CENTER }
                            )
                            lastLabelX = offset.x
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistorialEntrenamientoCard(log: com.spc.nutricoach.data.EntrenamientoLog) {
    val fechaFormateada = remember(log.fecha) {
        try {
            val sdfIn = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
            val date = sdfIn.parse(log.fecha)
            val sdfOut = java.text.SimpleDateFormat("dd MMM yyyy - HH:mm", java.util.Locale.getDefault())
            if (date != null) sdfOut.format(date) else log.fecha
        } catch (e: Exception) {
            log.fecha
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = log.rutina_nombre,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = log.dia_nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Listado de ejercicios realizados
            log.ejercicios.forEach { ejer ->
                val maxPeso = ejer.series.map { it.peso }.maxOrNull() ?: 0.0
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = com.spc.nutricoach.util.ExerciseNormalizer.formatFirstTime(ejer.nombre_snapshot),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${ejer.series.size} series • Max: $maxPeso kg",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun TusPostsContent(
    navController: NavController,
    feedViewModel: com.spc.nutricoach.ui.viewmodel.FeedViewModel,
    currentUserId: String
) {
    val misPosts = remember(feedViewModel.posts, currentUserId) {
        feedViewModel.posts.filter { it.autorId == currentUserId }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        if (feedViewModel.isLoading && misPosts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (!feedViewModel.isLoading && misPosts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No tienes publicaciones en la comunidad",
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 16.sp
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
            }
        }

        items(misPosts, key = { it.id }) { post ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                PostCard(
                    post = post,
                    currentUserId = currentUserId,
                    onLikeClick = { feedViewModel.toggleLike(post.id, currentUserId) },
                    onCommentClick = { 
                        navController.navigate(PantallaDetallePost(post.id))
                    },
                    onRoutineClick = { rutinaId ->
                        navController.navigate(PantallaDetalleRutina(rutinaId, isReadOnly = true))
                    },
                    onDietaClick = { dietaId ->
                        navController.navigate(PantallaDetalleDieta(dietaId, isReadOnly = true))
                    },
                    onReplicateRoutineClick = null,
                    onReplicateDietaClick = null,
                    onClick = {
                        navController.navigate(PantallaDetallePost(post.id))
                    }
                )

                var showConfirmDelete by remember { mutableStateOf(false) }

                Button(
                    onClick = { showConfirmDelete = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar post",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Publicación", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                if (showConfirmDelete) {
                    AlertDialog(
                        onDismissRequest = { showConfirmDelete = false },
                        title = { Text("Eliminar publicación") },
                        text = { Text("¿Estás seguro de que deseas eliminar esta publicación de la comunidad? Esta acción no se puede deshacer.") },
                        confirmButton = {
                            Button(
                                onClick = {
                                    showConfirmDelete = false
                                    feedViewModel.eliminarPost(post.id) { _, _ -> }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Eliminar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showConfirmDelete = false }) {
                                Text("Cancelar")
                            }
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
