package com.spc.nutricoach.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.spc.nutricoach.data.local.entity.NotasEntity
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.theme.PrimaryGreen
import com.spc.nutricoach.ui.viewmodel.NotasViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleNotaView(
    navController: NavController,
    notaId: Int,
    notasViewModel: NotasViewModel
) {
    var nota by remember { mutableStateOf<NotasEntity?>(null) }
    var titulo by remember { mutableStateOf("") }
    var contenido by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(notaId) {
        val notaEncontrada = notasViewModel.obtenerNotaPorId(notaId)
        if (notaEncontrada != null) {
            nota = notaEncontrada
            titulo = notaEncontrada.titulo
            contenido = notaEncontrada.contenido
        }
        isLoading = false
    }

    Scaffold(
        containerColor = MainBackground,
        topBar = {
            TopAppBar(
                title = { Text("Detalle de Nota", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            nota?.let {
                                val notaActualizada = it.copy(titulo = titulo, contenido = contenido)
                                notasViewModel.actualizarNota(notaActualizada)
                                navController.popBackStack()
                            }
                        },
                        enabled = titulo.isNotBlank() && contenido.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = "Guardar nota",
                            tint = if (titulo.isNotBlank() && contenido.isNotBlank()) PrimaryGreen else Color.Gray
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
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (nota == null) {
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Text("Nota no encontrada", color = Color.Gray, fontSize = 18.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Fecha
                val dateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale.getDefault())
                val fechaFormatada = dateFormat.format(Date(nota!!.fechaCreacion))
                
                Text(
                    text = "Creada el $fechaFormatada",
                    style = TextStyle(color = Color.Gray, fontSize = 12.sp),
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Edición Título
                TextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    placeholder = { Text("Título", style = TextStyle(fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color.LightGray)) },
                    textStyle = TextStyle(
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        brush = AppBrushes.Main
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Edición Contenido
                TextField(
                    value = contenido,
                    onValueChange = { contenido = it },
                    placeholder = { Text("Escribe tu nota aquí...", style = TextStyle(fontSize = 18.sp, color = Color.LightGray)) },
                    textStyle = TextStyle(
                        fontSize = 18.sp,
                        color = Color.DarkGray
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Toma el resto del espacio en pantalla
                    maxLines = Int.MAX_VALUE // Permite crecer sin límite definido
                )
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
