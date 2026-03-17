package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Height
import androidx.compose.material.icons.filled.Wc
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.RegistroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroView(navController: NavController, registroViewModel: RegistroViewModel = viewModel()) {

    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(registroViewModel.registroSuccess) {
        if (registroViewModel.registroSuccess) {
            kotlinx.coroutines.delay(1500)
            navController.popBackStack()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Registro", style = MaterialTheme.typography.titleLarge.copy(brush = AppBrushes.MainGradient)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            
            Text(
                text = "Crea tu cuenta de Nutri Coach",
                style = MaterialTheme.typography.displaySmall.copy(brush = AppBrushes.AccentGradient, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                modifier = Modifier.padding(bottom = 10.dp)
            )

            RegistroTextField(
                value = registroViewModel.nombre,
                onValueChange = { registroViewModel.nombre = it },
                label = "Nombre *",
                icon = Icons.Filled.Person,
                modifier = Modifier.fillMaxWidth()
            )

            RegistroTextField(
                value = registroViewModel.email,
                onValueChange = { registroViewModel.email = it },
                label = "Email *",
                icon = Icons.Filled.Email,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Email
            )

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                textStyle = MaterialTheme.typography.bodyLarge.copy(brush = AppBrushes.MainGradient),
                value = registroViewModel.password,
                onValueChange = { registroViewModel.password = it },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                label = { Text("Contraseña *", style = MaterialTheme.typography.titleSmall.copy(brush = AppBrushes.AccentGradient)) },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            )
            
            RegistroTextField(
                value = registroViewModel.telefono,
                onValueChange = { registroViewModel.telefono = it },
                label = "Teléfono",
                icon = Icons.Filled.Phone,
                modifier = Modifier.fillMaxWidth(),
                keyboardType = KeyboardType.Phone
            )
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RegistroTextField(
                    value = registroViewModel.edad,
                    onValueChange = { registroViewModel.edad = it },
                    label = "Edad",
                    icon = Icons.Filled.Cake,
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.weight(1f)
                )
                RegistroTextField(
                    value = registroViewModel.genero,
                    onValueChange = { registroViewModel.genero = it },
                    label = "Género (M/F)",
                    icon = Icons.Filled.Wc,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                RegistroTextField(
                    value = registroViewModel.peso,
                    onValueChange = { registroViewModel.peso = it },
                    label = "Peso (kg)",
                    icon = Icons.Filled.Scale,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
                RegistroTextField(
                    value = registroViewModel.altura,
                    onValueChange = { registroViewModel.altura = it },
                    label = "Altura (cm)",
                    icon = Icons.Filled.Height,
                    keyboardType = KeyboardType.Decimal,
                    modifier = Modifier.weight(1f)
                )
            }
            
            RegistroTextField(
                value = registroViewModel.objetivo,
                onValueChange = { registroViewModel.objetivo = it },
                label = "Objetivo (Opcional)",
                icon = Icons.Filled.Flag,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                    .height(55.dp),
                onClick = { registroViewModel.doRegistro() },
                enabled = !registroViewModel.isLoading && !registroViewModel.registroSuccess,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
            ) {
                if (registroViewModel.isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text(text = "Registrarme", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                }
            }

            if (registroViewModel.statusMessage.isNotEmpty()) {
                val color = if (registroViewModel.statusMessage.startsWith("Error") || registroViewModel.statusMessage.startsWith("Nombre")) Color.Red else MaterialTheme.colorScheme.primary
                Text(
                    text = registroViewModel.statusMessage,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun RegistroTextField(
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
        label = { Text(label, style = MaterialTheme.typography.labelMedium.copy(brush = AppBrushes.AccentGradient)) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
    )
}
