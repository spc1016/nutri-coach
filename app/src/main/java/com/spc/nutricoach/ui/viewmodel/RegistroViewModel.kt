package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.RegistroClienteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RegistroViewModel(application: Application) : AndroidViewModel(application) {

    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var telefono by mutableStateOf("")
    var edad by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")

    var statusMessage by mutableStateOf("")
        private set

    var registroSuccess by mutableStateOf(false)
        private set

    var isLoading by mutableStateOf(false)
        private set

    fun doRegistro() {
        if (nombre.isBlank() || email.isBlank() || password.isBlank()) {
            statusMessage = "Nombre, email y contraseña son obligatorios"
            return
        }

        isLoading = true
        statusMessage = ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val request = RegistroClienteRequest(
                    nombre = nombre,
                    email = email,
                    password_hash = password,
                    telefono = telefono.ifBlank { null },
                    edad = edad.toIntOrNull(),
                    peso = peso.toDoubleOrNull(),
                    altura = altura.toDoubleOrNull()
                )
                
                val response = NutriCoachApiClient.service.crearCliente(request)
                Log.d("REGISTRO", "Registro OK - id: ${response.id}")
                statusMessage = "¡Registro exitoso! Puedes iniciar sesión."
                registroSuccess = true
            } catch (e: HttpException) {
                Log.e("REGISTRO_ERROR", "HTTP ${e.code()}: ${e.message()}")
                statusMessage = "Error del servidor (${e.code()})"
            } catch (e: IOException) {
                Log.e("REGISTRO_ERROR", "Error de red: ${e.message}", e)
                statusMessage = "Error de conexión. Comprueba tu red e inténtalo de nuevo."
            } catch (e: Exception) {
                Log.e("REGISTRO_ERROR", "Error inesperado: ${e.message}", e)
                statusMessage = "Error inesperado: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
