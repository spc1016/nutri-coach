package com.spc.nutricoach.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.ApiResponse
import com.spc.nutricoach.data.LoginRequest
import com.spc.nutricoach.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var statusMessage by mutableStateOf("")
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun doLogin(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            statusMessage = "El email y la contraseña no pueden estar vacíos"
            return
        }
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            login(email, password)
            isLoading = false
        }
    }

    // Decodificación de JWT movida a JwtUtils

    private suspend fun login(email: String, password: String) {
        try {
            when (val response = authRepository.login(LoginRequest(email, password))) {
                is ApiResponse.Success -> {
                    val loginRes = response.data
                    Log.d("LOGIN", "Login OK - token: ${loginRes.token}, role: ${loginRes.role}")
                    val clienteId = com.spc.nutricoach.util.JwtUtils.extraerIdDeToken(loginRes.token)
                    Log.d("LOGIN", "Cliente ID extraído del JWT: $clienteId")
                    
                    authRepository.session.saveSession(loginRes.token, loginRes.role, clienteId, email)
                    statusMessage = "¡Login exitoso! Redirigiendo..."
                    loginSuccess = true
                }
                is ApiResponse.Error -> {
                    Log.e("LOGIN_ERROR", "Error HTTP ${response.code}: ${response.message}")
                    statusMessage = if (response.code == 401) {
                        "Credenciales incorrectas"
                    } else {
                        "Error del servidor (${response.code})"
                    }
                }
                is ApiResponse.Exception -> {
                    Log.e("LOGIN_ERROR", "Excepción de red", response.throwable)
                    statusMessage = "Error de conexión. Comprueba tu red e inténtalo de nuevo."
                }
            }
        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", "Error inesperado: ${e.message}", e)
            statusMessage = "Error inesperado: ${e.message}"
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            authRepository.session.clearSession()
            loginSuccess = false
            statusMessage = ""
        }
    }
}
