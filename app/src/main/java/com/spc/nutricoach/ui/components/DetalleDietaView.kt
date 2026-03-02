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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spc.nutricoach.model.Comida
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.theme.SecondaryBackground
import com.spc.nutricoach.ui.viewmodel.DietaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleDietaView(
    navController: NavController,
    dietaId: String,
    dietaViewModel: DietaViewModel
) {
    val dieta = dietaViewModel.dietas.find { it.id == dietaId }
    val completedMeals by dietaViewModel.completedMealsFlow.collectAsState(initial = emptySet())
    val clientId = dietaViewModel.lastLoadedClientId

    Scaffold(
        containerColor = MainBackground,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Dieta") },
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
                
                Row(
                   modifier = Modifier.fillMaxWidth(),
                   horizontalArrangement = Arrangement.SpaceBetween,
                   verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Comidas",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    TextButton(onClick = { dietaViewModel.clearDietMeals(dietaId, dieta.comidas) }) {
                        Text("Reiniciar", color = PrimaryGreen, fontWeight = FontWeight.Bold)
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Comidas
            if (dieta.comidas.isNotEmpty()) {
                val comidasOrdenadas = dieta.comidas.sortedBy { it.orden }
                items(comidasOrdenadas.size) { index ->
                    val comida = comidasOrdenadas[index]
                    val mealKey = "${clientId}_${dietaId}_${comida.nombre}"
                    val isCompleted = completedMeals.contains(mealKey)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) Color(0xFFF0FFF0) else Color.White
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = if (isCompleted) 2.dp else 6.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            ComidaItemDetail(
                                comida = comida,
                                isCompleted = isCompleted,
                                onCheckedChange = { checked ->
                                    dietaViewModel.toggleMeal(dietaId, comida.nombre, checked)
                                }
                            )
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
fun ComidaItemDetail(
    comida: Comida,
    isCompleted: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
                    tint = if (isCompleted) Color.Gray else PrimaryGreen,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = comida.nombre,
                    style = if (isCompleted) {
                        TextStyle(
                            color = Color.Gray,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    } else {
                        TextStyle(
                            brush = AppBrushes.Main,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                    }
                )
                if (!comida.horaSugerida.isNullOrBlank()) {
                    Text(
                        text = "· ${comida.horaSugerida}",
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
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
                    tint = if (isCompleted) PrimaryGreen else Color.Gray,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))

        // Alimentos de la comida
        comida.alimentos.forEach { alimento ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SecondaryBackground.copy(alpha = 0.3f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val c = alimento.cantidad
                    val formatCantidad = if (c % 1.0 == 0.0) c.toInt().toString() else c.toString()
                    
                    Text(
                        text = "• ${alimento.nombreSnapshot}",
                        style = TextStyle(
                            color = PrimaryGreen,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$formatCantidad ${alimento.unidad}",
                        style = TextStyle(
                            fontSize = 14.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        ),
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        }
    }
}
