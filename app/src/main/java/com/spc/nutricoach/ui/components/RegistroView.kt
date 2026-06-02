package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.compose.material3.MaterialTheme
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.viewmodel.RegistroStep
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
                    IconButton(onClick = {
                        if (registroViewModel.currentStep == RegistroStep.VERIFICATION) {
                            registroViewModel.goBackToForm()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
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
            when (registroViewModel.currentStep) {
                RegistroStep.FORM -> {
                    // Step 1: Registration form
                    Text(
                        text = "Crea tu cuenta",
                        style = MaterialTheme.typography.displaySmall.copy(
                            brush = AppBrushes.AccentGradient,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Text(
                        text = "Solo necesitas tu nombre, email de Gmail y una contraseña. El resto lo podrás configurar después.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        ),
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
                        label = "Email de Gmail *",
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

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                            .height(55.dp),
                        onClick = { registroViewModel.sendCode() },
                        enabled = !registroViewModel.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    ) {
                        if (registroViewModel.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            Text(text = "Enviar código de verificación", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 15.sp))
                        }
                    }
                }

                RegistroStep.VERIFICATION -> {
                    // Step 2: Code verification
                    Text(
                        text = "Verifica tu email",
                        style = MaterialTheme.typography.displaySmall.copy(
                            brush = AppBrushes.AccentGradient,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 5.dp)
                    )

                    Text(
                        text = "Hemos enviado un código de 6 dígitos a:",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )

                    Text(
                        text = registroViewModel.email.trim(),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            brush = AppBrushes.MainGradient,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(bottom = 15.dp)
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
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            textAlign = TextAlign.Center,
                            letterSpacing = 8.sp,
                            brush = AppBrushes.MainGradient
                        ),
                        value = registroViewModel.verificationCode,
                        onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) registroViewModel.verificationCode = it },
                        label = { Text("Código de verificación", style = MaterialTheme.typography.titleSmall.copy(brush = AppBrushes.AccentGradient)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(brush = AppBrushes.MainGradient, shape = RoundedCornerShape(12.dp))
                            .height(55.dp),
                        onClick = { registroViewModel.verifyCode() },
                        enabled = !registroViewModel.isLoading && !registroViewModel.registroSuccess,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = Color.Black),
                    ) {
                        if (registroViewModel.isLoading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            Text(text = "Verificar y crear cuenta", style = TextStyle(color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp))
                        }
                    }

                    // Resend code button
                    TextButton(
                        onClick = { registroViewModel.resendCode() },
                        enabled = registroViewModel.canResend && !registroViewModel.isLoading
                    ) {
                        Text(
                            text = if (registroViewModel.canResend) "Reenviar código"
                                   else "Reenviar código (${registroViewModel.resendCountdown}s)",
                            style = if (registroViewModel.canResend)
                                MaterialTheme.typography.bodyMedium.copy(brush = AppBrushes.AccentGradient)
                            else
                                MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }

                    // Go back button
                    TextButton(
                        onClick = { registroViewModel.goBackToForm() },
                        enabled = !registroViewModel.isLoading
                    ) {
                        Text(
                            text = "← Cambiar email",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Status message (shown in both steps)
            if (registroViewModel.statusMessage.isNotEmpty()) {
                val color = if (registroViewModel.isError) Color.Red else MaterialTheme.colorScheme.primary
                Text(
                    text = registroViewModel.statusMessage,
                    style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = color),
                    textAlign = TextAlign.Center,
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
