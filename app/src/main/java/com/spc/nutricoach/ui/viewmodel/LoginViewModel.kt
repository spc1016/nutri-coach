package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.LoginRequest
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

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

    private suspend fun login(email: String, password: String) {
        try {
            val response = NutriCoachApiClient.service.login(LoginRequest(email, password))
            Log.e("SESSION_SAVE", "Login OK - token: ${response.token}, role: ${response.role}")
            sessionManager.saveSession(response.token, response.role)
            statusMessage = "¡Login exitoso! Redirigiendo..."
            loginSuccess = true
        } catch (e: Exception) {
            statusMessage = "Credenciales incorrectas"
            Log.e("RETROFIT_ERROR", e.message ?: "Unknown Error")
            e.printStackTrace()
        }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            sessionManager.clearSession()
            loginSuccess = false
            statusMessage = ""
        }
    }
}
