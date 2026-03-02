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
import kotlinx.coroutines.flow.Flow
import com.spc.nutricoach.data.RoutineTrackerManager
import retrofit2.HttpException
import java.io.IOException

class RutinaViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val routineTrackerManager = RoutineTrackerManager(application)

    var rutinas by mutableStateOf<List<Rutina>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var lastLoadedClientId by mutableStateOf<String?>(null)
        private set

    fun cargarRutinas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteId = sessionManager.getClienteId()
            if (!force && rutinas.isNotEmpty() && lastLoadedClientId == clienteId && !clienteId.isNullOrBlank()) {
                return@launch
            }
            isLoading = true
            error = null
            if (lastLoadedClientId != clienteId) {
                rutinas = emptyList()
            }
            loadRutinas(clienteId)
        }
    }
    
    private suspend fun loadRutinas(clienteId: String?){
        try {
            if (clienteId.isNullOrBlank()) {
                error = "No se encontró el ID del cliente"
                isLoading = false
                return
            }
            val resultado = NutriCoachApiClient.service.obtenerRutinasCliente(clienteId)
            Log.d("RUTINAS", "Rutinas obtenidas: ${resultado.size}")
            rutinas = resultado
            lastLoadedClientId = clienteId
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

    fun getExerciseWeightFlow(rutinaId: String, exerciseKey: String): Flow<String?> {
        val clienteId = lastLoadedClientId ?: return kotlinx.coroutines.flow.flowOf(null)
        return routineTrackerManager.getExerciseWeightFlow(clienteId, rutinaId, exerciseKey)
    }

    fun saveExerciseWeight(rutinaId: String, exerciseKey: String, weight: String) {
        viewModelScope.launch {
            val clienteId = sessionManager.getClienteId() ?: return@launch
            routineTrackerManager.saveExerciseWeight(clienteId, rutinaId, exerciseKey, weight)
        }
    }
}
