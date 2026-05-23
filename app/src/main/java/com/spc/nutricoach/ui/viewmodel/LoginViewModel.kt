package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Base64
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
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

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

    private fun extraerIdDeToken(token: String): String {
        val partes = token.split(".")
        if (partes.size < 2) {
            throw IllegalArgumentException("Token JWT con formato inválido")
        }
        var base64Payload = partes[1]
            .replace('-', '+')
            .replace('_', '/')
        
        while (base64Payload.length % 4 != 0) {
            base64Payload += "="
        }
        
        val payloadBytes = Base64.decode(base64Payload, Base64.DEFAULT)
        val payload = String(payloadBytes, kotlin.text.Charsets.UTF_8)
        val json = JSONObject(payload)
        return json.getString("sub")
    }

    private suspend fun login(email: String, password: String) {
        try {
            val response = NutriCoachApiClient.service.login(LoginRequest(email, password))
            Log.d("LOGIN", "Login OK - token: ${response.token}, role: ${response.role}")
            val clienteId = extraerIdDeToken(response.token)
            Log.d("LOGIN", "Cliente ID extraído del JWT: $clienteId")
            sessionManager.saveSession(response.token, response.role, clienteId, email)
            statusMessage = "¡Login exitoso! Redirigiendo..."
            loginSuccess = true
        } catch (e: HttpException) {
            Log.e("LOGIN_ERROR", "HTTP ${e.code()}: ${e.message()}")
            statusMessage = if (e.code() == 401) {
                "Credenciales incorrectas"
            } else {
                "Error del servidor (${e.code()})"
            }
        } catch (e: IOException) {
            Log.e("LOGIN_ERROR", "Error de red: ${e.message}", e)
            statusMessage = "Error de conexión. Comprueba tu red e inténtalo de nuevo."
        } catch (e: Exception) {
            Log.e("LOGIN_ERROR", "Error inesperado: ${e.message}", e)
            statusMessage = "Error inesperado: ${e.message}"
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

