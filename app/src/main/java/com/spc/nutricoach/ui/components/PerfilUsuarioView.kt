package com.spc.nutricoach.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.ui.theme.AppBrushes
import com.spc.nutricoach.ui.theme.MainBackground
import com.spc.nutricoach.ui.viewmodel.LoginViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilUsuarioView(
    navController: NavController,
    loginViewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    
    val email by sessionManager.userEmailFlow.collectAsState(initial = "")
    
    val letra = email?.firstOrNull()?.uppercase() ?: "U"

    Scaffold(
        containerColor = MainBackground,
        contentColor = Color.Black,
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
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
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AppBrushes.Main),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letra,
                    style = TextStyle(
                        color = Color.White,
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = email ?: "Usuario",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                )
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(brush = AppBrushes.Secondary, shape = ButtonDefaults.shape)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                onClick = {
                    loginViewModel.logout()
                    // Redirigir y limpiar el backstack
                    navController.navigate(PantallaLogin) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            ) {
                Text(
                    text = "Cerrar Sesión",
                    style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                )
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}
