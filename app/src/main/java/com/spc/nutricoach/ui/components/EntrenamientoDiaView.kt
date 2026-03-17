package com.spc.nutricoach.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.EntrenamientoViewModel
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel
import com.spc.nutricoach.workout.WorkoutService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrenamientoDiaView(
    navController: NavController,
    rutinaId: String,
    diaNombre: String,
    rutinaViewModel: RutinaViewModel,
    entrenamientoViewModel: EntrenamientoViewModel
) {
    val context = LocalContext.current

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { _ -> }

        LaunchedEffect(Unit) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    LaunchedEffect(rutinaId, diaNombre) {
        val rutina = rutinaViewModel.rutinas.find { it.id == rutinaId }
        if (rutina != null) {
            val dia = rutina.dias.find { it.nombre == diaNombre }
            if (dia != null) {
                entrenamientoViewModel.iniciarOReanudar(rutinaId, dia)
                val intent = Intent(context, WorkoutService::class.java).apply {
                    action = WorkoutService.ACTION_START
                }
                context.startForegroundService(intent)
            }
        }
    }

    val diaActual by entrenamientoViewModel.diaActual.collectAsState()
    val isFinished by entrenamientoViewModel.isFinished.collectAsState()
    val isStopped by entrenamientoViewModel.isStopped.collectAsState()

    LaunchedEffect(isStopped) {
        if (isStopped) {
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text(diaActual?.nombre ?: "Entrenamiento") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    if (diaActual != null) {
                        IconButton(onClick = { entrenamientoViewModel.reiniciarEntrenamiento() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reiniciar entrenamiento")
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            if (diaActual == null) {
                if (isFinished) {
                    Text("Error al cargar el entrenamiento.")
                } else {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
                return@Scaffold
            }

            if (isFinished) {
                WorkoutFinishedScreen(navController, entrenamientoViewModel)
            } else {
                ActiveWorkoutScreen(entrenamientoViewModel, rutinaViewModel, rutinaId, diaActual!!.nombre)
            }
        }
    }
}

@Composable
fun ActiveWorkoutScreen(
    viewModel: EntrenamientoViewModel,
    rutinaViewModel: RutinaViewModel,
    rutinaId: String,
    diaNombre: String
) {
    val diaState by viewModel.diaActual.collectAsState()
    val dia = diaState ?: return
    val exerciseIndex by viewModel.currentExerciseIndex.collectAsState()
    if (exerciseIndex >= dia.ejercicios.size) return
    
    val currentExercise = dia.ejercicios[exerciseIndex]
    val currentSet by viewModel.currentSet.collectAsState()
    val totalSets = currentExercise.series
    
    val totalExercises = dia.ejercicios.size
    
    val uniqueKey = "${diaNombre}_${exerciseIndex}_${currentExercise.nombreSnapshot}"
    val weightFlow = remember(rutinaId, uniqueKey) {
        rutinaViewModel.getExerciseWeightFlow(rutinaId, uniqueKey)
    }
    val savedWeight by weightFlow.collectAsState(initial = "")

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ejercicio ${exerciseIndex + 1} de $totalExercises",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { (exerciseIndex + 1).toFloat() / totalExercises.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentExercise.nombreSnapshot,
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = AppBrushes.MainGradient,
                    textAlign = TextAlign.Center
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Serie $currentSet de $totalSets",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        InfoBox("Reps", currentExercise.repeticiones.ifBlank { "-" })
                        if (!savedWeight.isNullOrBlank()) {
                            InfoBox("Peso", "$savedWeight kg")
                        }
                    }
                    
                    if (!currentExercise.notas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "💡 ${currentExercise.notas}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            val isResting by viewModel.isResting.collectAsState()
            val restTimeRemaining by viewModel.restTimeRemaining.collectAsState()

            if (isResting) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Tiempo de descanso",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Descanso",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Text(
                        text = formatTime(restTimeRemaining),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            brush = AppBrushes.MainGradient,
                            fontWeight = FontWeight.Black
                        )
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { viewModel.skipRest() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Saltar Descanso", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                Button(
                    onClick = { viewModel.finishSet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "SERIE TERMINADA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun InfoBox(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface))
    }
}

@Composable
fun WorkoutFinishedScreen(navController: NavController, viewModel: EntrenamientoViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = "Completado",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "¡Entrenamiento Completado!",
            style = MaterialTheme.typography.headlineLarge.copy(
                brush = AppBrushes.MainGradient,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Has terminado todos los ejercicios de este día.",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.Black),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Black)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Volver a la Rutina", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedButton(
            onClick = { viewModel.reiniciarEntrenamiento() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
        ) {
            Text("Volver a Empezar", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@SuppressLint("DefaultLocale")
fun formatTime(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format(java.util.Locale.getDefault(), "%02d:%02d", mins, secs)
}
