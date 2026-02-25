package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spc.nutricoach.model.Comida
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.viewmodel.DietaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDietaView(
    navController: NavController,
    dietaId: String,
    dietaViewModel: DietaViewModel
) {
    val dieta = dietaViewModel.dietas.find { it.id == dietaId }

    Scaffold(
        containerColor = MainBackground,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Dieta") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
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
                // Nombre de la dieta
                Text(
                    text = dieta.nombre,
                    style = TextStyle(
                        brush = AppBrushes.Main,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))

                // Kcal objetivo
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocalFireDepartment,
                        contentDescription = null,
                        tint = Color(0xFFFF6B35),
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = " ${dieta.kcalObjetivo} kcal/día recomendadas",
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.DarkGray
                        )
                    )
                }

                if (!dieta.notasGenerales.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Notas Generales",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dieta.notasGenerales,
                        style = TextStyle(fontSize = 15.sp, color = Color.Gray)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Comidas",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comidas
            if (dieta.comidas.isNotEmpty()) {
                val comidasOrdenadas = dieta.comidas.sortedBy { it.orden }
                items(comidasOrdenadas.size) { index ->
                    val comida = comidasOrdenadas[index]
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ComidaItemDetail(comida = comida)
                        }
                    }
                }
            } else {
                item {
                    Text(
                        text = "No hay comidas asignadas.",
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
fun ComidaItemDetail(comida: Comida) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Restaurant,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = comida.nombre,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
            )
            if (!comida.horaSugerida.isNullOrBlank()) {
                Text(
                    text = "· ${comida.horaSugerida}",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Alimentos de la comida
        comida.alimentos.forEach { alimento ->
            Text(
                text = buildAnnotatedString {
                    withStyle(style = SpanStyle(color = PrimaryGreen, fontWeight = FontWeight.Bold)) {
                        append("• ")
                    }
                    withStyle(style = SpanStyle(fontSize = 15.sp, color = Color.DarkGray)) {
                        append("${alimento.nombreSnapshot} — ")
                    }
                    withStyle(style = SpanStyle(fontSize = 15.sp, color = Color.Black, fontWeight = FontWeight.SemiBold)) {
                        val c = alimento.cantidad
                        val formatCantidad = if (c % 1.0 == 0.0) c.toInt().toString() else c.toString()
                        append("$formatCantidad ${alimento.unidad}")
                    }
                },
                modifier = Modifier.padding(start = 26.dp, top = 4.dp)
            )
        }
    }
}
