package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.LoginViewModel
import com.spc.nutricoach.ui.viewmodel.PerfilUsuarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioView(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel(),
    perfilViewModel: PerfilUsuarioViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    
    val letra = email?.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(AppBrushes.MainGradient),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            if (perfilViewModel.isLoading && perfilViewModel.nombre.isEmpty()) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(24.dp))
            } else {
                PerfilTextField(
                    value = perfilViewModel.nombre,
                    onValueChange = { perfilViewModel.nombre = it },
                    label = "Nombre",
                    icon = Icons.Filled.Person,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.email,
                    onValueChange = { perfilViewModel.email = it },
                    label = "Email",
                    icon = Icons.Filled.Email,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Email
                )
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.telefono,
                    onValueChange = { perfilViewModel.telefono = it },
                    label = "Teléfono",
                    icon = Icons.Filled.Phone,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardType = KeyboardType.Phone
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilTextField(
                        value = perfilViewModel.edad,
                        onValueChange = { perfilViewModel.edad = it },
                        label = "Edad",
                        icon = Icons.Filled.Cake,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    PerfilTextField(
                        value = perfilViewModel.peso,
                        onValueChange = { perfilViewModel.peso = it },
                        label = "Peso (kg)",
                        icon = Icons.Filled.Scale,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    PerfilTextField(
                        value = perfilViewModel.altura,
                        onValueChange = { perfilViewModel.altura = it },
                        label = "Altura (cm)",
                        icon = Icons.Filled.Height,
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))

                PerfilTextField(
                    value = perfilViewModel.objetivo,
                    onValueChange = { perfilViewModel.objetivo = it },
                    label = "Objetivo",
                    icon = Icons.Filled.Flag,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                if (perfilViewModel.statusMessage.isNotEmpty()) {
                    val color = if (perfilViewModel.statusMessage.contains("Error") || perfilViewModel.statusMessage.contains("obligatorios")) Color.Red else MaterialTheme.colorScheme.primary
                    Text(
                        text = perfilViewModel.statusMessage,
                        style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    enabled = !perfilViewModel.isLoading,
                    onClick = {
                        perfilViewModel.guardarCambios()
                    }
                ) {
                    if (perfilViewModel.isLoading) {
                        CircularProgressIndicator(color = Color.Black, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                    } else {
                        Text(
                            text = "Guardar Cambios",
                            style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                        .background(brush = AppBrushes.AccentGradient, shape = RoundedCornerShape(12.dp))
                        .height(55.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                onClick = {
                    loginViewModel.logout()
                    navController.navigate(PantallaLogin) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text(
                    text = "Cerrar Sesión",
                    style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun PerfilTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        modifier = modifier,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(20.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
        textStyle = MaterialTheme.typography.bodyLarge.copy(brush = AppBrushes.MainGradient),
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.titleSmall.copy(brush = AppBrushes.AccentGradient)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    )
}

