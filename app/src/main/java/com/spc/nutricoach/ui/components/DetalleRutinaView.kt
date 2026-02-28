package com.spc.nutricoach.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.theme.SecondaryBackground
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleRutinaView(
    navController: NavController,
    rutinaId: String,
    rutinaViewModel: RutinaViewModel
) {
    val rutina = rutinaViewModel.rutinas.find { it.id == rutinaId }

    Scaffold(
        containerColor = MainBackground,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Rutina") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MainBackground,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
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
                Text("Error: Rutina no encontrada")
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
                // Nombre de la rutina
                Text(
                    text = rutina.nombre,
                    style = TextStyle(
                        brush = AppBrushes.Main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Días objetivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = " ${rutina.dias.size} días de entrenamiento",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                    )
                }

                if (!rutina.notasGenerales.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notas Generales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = rutina.notasGenerales,
                        style = TextStyle(fontSize = 15.sp, color = Color.Gray)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Días de Entrenamiento",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            // Días
            if (rutina.dias.isNotEmpty()) {
                val diasOrdenados = rutina.dias.sortedBy { it.orden }
                items(diasOrdenados.size) { index ->
                    val dia = diasOrdenados[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            DiaItemDetail(dia = dia)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = {
                                    navController.navigate(PantallaEntrenamientoDia(rutina.id, dia.nombre))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.FitnessCenter,
                                    contentDescription = "Empezar",
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Comenzar Entrenamiento", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "No hay días asignados.",
                        color = Color.Gray,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun DiaItemDetail(dia: Dia) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.FitnessCenter,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = dia.nombre,
                style = TextStyle(
                    brush = AppBrushes.Main,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 22.sp,
                )
            )
            if (!dia.enfoque.isNullOrBlank()) {
                Text(
                    text = "· ${dia.enfoque}",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Ejercicios del día
        dia.ejercicios.forEach { ejercicio ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryBackground.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "• ${ejercicio.nombreSnapshot}",
                        style = TextStyle(
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${ejercicio.series} series × ${ejercicio.repeticiones} reps" +
                            (if (!ejercicio.descanso.isNullOrBlank()) " | ⏱ ${ejercicio.descanso}s" else "") +
                            (if (!ejercicio.rir.isNullOrBlank()) " | RIR: ${ejercicio.rir}" else ""),
                        style = TextStyle(fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium),
                        modifier = Modifier.padding(start = 12.dp)
                    )

                    if (!ejercicio.notas.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "💡 ${ejercicio.notas}",
                            style = TextStyle(fontSize = 13.sp, color = Color.Gray),
                            modifier = Modifier.padding(start = 12.dp)
                        )
                    }
                }
            }
        }
    }
}
