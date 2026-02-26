package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Rutina
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class RutinaViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    var rutinas by mutableStateOf<List<Rutina>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    fun cargarRutinas() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            error = null
            rutinas = emptyList()
            loadRutinas()
        }
    }
    
    private suspend fun loadRutinas(){
        try {
            val clienteId = sessionManager.getClienteId()
            if (clienteId.isNullOrBlank()) {
                error = "No se encontró el ID del cliente"
                isLoading = false
                return
            }
            val resultado = NutriCoachApiClient.service.obtenerRutinasCliente(clienteId)
            Log.d("RUTINAS", "Rutinas obtenidas: ${resultado.size}")
            rutinas = resultado
        } catch (e: HttpException) {
            Log.e("RUTINAS_ERROR", "HTTP ${e.code()}: ${e.message()}")
            error = "Error del servidor (${e.code()})"
        } catch (e: IOException) {
            Log.e("RUTINAS_ERROR", "Error de red: ${e.message}", e)
            error = "Error de conexión"
        } catch (e: Exception) {
            Log.e("RUTINAS_ERROR", "Error inesperado: ${e.message}", e)
            error = "Error inesperado: ${e.message}"
        } finally {
            isLoading = false
        }
    }
}
