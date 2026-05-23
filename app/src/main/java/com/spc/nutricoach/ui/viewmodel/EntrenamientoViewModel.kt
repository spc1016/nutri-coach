package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.workout.WorkoutManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntrenamientoViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    var isSavingLog by mutableStateOf(false)
        private set

    var saveLogError by mutableStateOf<String?>(null)
        private set

    val diaActual: StateFlow<Dia?> = WorkoutManager.diaActual
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentExerciseIndex: StateFlow<Int> = WorkoutManager.currentExerciseIndex
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val currentSet: StateFlow<Int> = WorkoutManager.currentSet
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val isResting: StateFlow<Boolean> = WorkoutManager.isResting
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val restTimeRemaining: StateFlow<Int> = WorkoutManager.restTimeRemaining
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val isFinished: StateFlow<Boolean> = WorkoutManager.isFinished
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
        
    val isStopped: StateFlow<Boolean> = WorkoutManager.isStopped
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    fun iniciarOReanudar(rutinaId: String, dia: Dia) {
        WorkoutManager.iniciarOReanudar(rutinaId, dia)
    }

    fun reiniciarEntrenamiento() {
        WorkoutManager.reiniciarEntrenamiento()
    }

    fun finishSet(reps: String = "", peso: Double = 0.0, descanso: Int = -1) {
        WorkoutManager.finishSet(reps, peso, descanso)
    }
    
    fun skipRest() {
        WorkoutManager.skipRest()
    }

    fun registrarEntrenamiento(rutinaNombre: String, diaNombre: String, onComplete: (Boolean) -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            isSavingLog = true
            saveLogError = null
            try {
                val token = sessionManager.getToken()
                val clienteId = sessionManager.getClienteId()
                if (token.isNullOrBlank() || clienteId.isNullOrBlank()) {
                    saveLogError = "No se encontró sesión o ID de cliente"
                    onComplete(false)
                    return@launch
                }
                
                val log = WorkoutManager.getEntrenamientoLog(rutinaNombre, diaNombre)
                com.spc.nutricoach.data.NutriCoachApiClient.service.registrarEntrenamiento(
                    clienteId = clienteId,
                    token = "Bearer $token",
                    request = log
                )
                onComplete(true)
            } catch (e: Exception) {
                android.util.Log.e("WORKOUT_LOG", "Error al registrar entrenamiento", e)
                saveLogError = e.message ?: "Error de red"
                onComplete(false)
            } finally {
                isSavingLog = false
            }
        }
    }
}
