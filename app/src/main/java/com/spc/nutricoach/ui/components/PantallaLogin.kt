package com.spc.nutricoach.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
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
import com.spc.nutricoach.R
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.LoginViewModel

@Composable
fun LoginView(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {


    var inputEmail by remember { mutableStateOf("") }
    var inputPassw by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    LaunchedEffect(loginViewModel.loginSuccess) {
        if (loginViewModel.loginSuccess) {
            navController.navigate(PantallaInicio) {
                popUpTo(PantallaLogin) { inclusive = true }
            }
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .padding(top = 70.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                modifier = Modifier.size(120.dp),
                painter = painterResource(id = R.drawable. logo),
                contentDescription = null
            )
            Spacer(modifier = Modifier.padding(10.dp))
            Text(
                text = "Nutri Coach",
                style = MaterialTheme.typography.headlineLarge.copy(
                    brush = AppBrushes.MainGradient
                )
            )
            Spacer(modifier = Modifier.padding(15.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 60.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = {
                    Text("Email", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
                },
                leadingIcon = {
                    Icon(Icons.Filled.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
            )
            Spacer(modifier = Modifier.padding(15.dp))

            OutlinedTextField(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 60.dp),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                ),
                textStyle = MaterialTheme.typography.bodyLarge,
                value = inputPassw,
                onValueChange = { inputPassw = it },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                label = {
                    Text("Contraseña", style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary))
                },
                leadingIcon = {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
            )
            Spacer(modifier = Modifier.padding(15.dp))


            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp)
                    .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                    .height(60.dp),
                onClick = {
                    loginViewModel.doLogin(inputEmail,inputPassw)
                },
                enabled = !loginViewModel.isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
            ) {
                if (loginViewModel.isLoading) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp)
                    )
                } else {
                    Text(
                        text = "Iniciar Sesión",
                        style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.padding(12.dp))

            if (loginViewModel.statusMessage.isNotEmpty()) {
                val color = if (loginViewModel.statusMessage.startsWith("Error") ||
                    loginViewModel.statusMessage.startsWith("Sin"))
                    Color.Red else MaterialTheme.colorScheme.primary
                Text(
                    text = loginViewModel.statusMessage,
                    style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                )
                Spacer(modifier = Modifier.padding(8.dp))
            }

            Text(
                text = "¿No tienes cuenta? Regístrate Aquí",
                style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.primary),
                modifier = Modifier.clickable { navController.navigate(PantallaRegistro) }.padding(8.dp)
            )
        }
    }
}