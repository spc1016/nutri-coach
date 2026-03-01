package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.spc.nutricoach.model.Rutina
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.viewmodel.RutinaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RutinasView(
    navController: NavController,
    rutinaViewModel: RutinaViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        rutinaViewModel.cargarRutinas()
    }
    
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    val letraInicial = email?.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = MainBackground,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "Mis Rutinas",
                        style = TextStyle(
                            brush = AppBrushes.Secondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    )
                },
                actions = {
                    IconButton(onClick = { rutinaViewModel.cargarRutinas(force = true) }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Actualizar rutinas",
                            tint = PrimaryGreen
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AppBrushes.Main)
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
                    containerColor = MainBackground,
                    titleContentColor = Color.Black
                )
            )
        }
    ) { innerPadding ->

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
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
                        CircularProgressIndicator(color = PrimaryGreen)
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
                            color = Color.Gray,
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
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun RutinaCard(rutina: Rutina, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rutina.nombre,
                    style = TextStyle(
                        brush = AppBrushes.Main,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.FitnessCenter,
                        contentDescription = null,
                        tint = PrimaryGreen,
                        modifier = Modifier.size(22.dp)
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
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = rutina.notasGenerales,
                        style = TextStyle(fontSize = 14.sp, color = Color.Gray)
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = "Ver detalle",
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}
