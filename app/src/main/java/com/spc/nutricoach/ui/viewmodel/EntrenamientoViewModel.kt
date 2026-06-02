package com.spc.nutricoach.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.ApiResponse
import com.spc.nutricoach.data.repository.RutinaRepository
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.workout.WorkoutManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EntrenamientoViewModel @Inject constructor(
    private val rutinaRepository: RutinaRepository
) : ViewModel() {

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
                val clienteId = rutinaRepository.session.getClienteId()
                if (clienteId.isNullOrBlank()) {
                    saveLogError = "No se encontró ID de cliente"
                    onComplete(false)
                    return@launch
                }
                
                val log = WorkoutManager.getEntrenamientoLog(rutinaNombre, diaNombre)
                
                // Cargar el historial de entrenamientos para buscar nombres de ejercicios existentes
                val existingHistory = when (val historyResponse = rutinaRepository.obtenerHistorialEntrenamientos(clienteId)) {
                    is ApiResponse.Success -> historyResponse.data
                    else -> emptyList()
                }
                
                val existingNames = existingHistory.flatMap { it.ejercicios }.map { it.nombre_snapshot }.distinct()
                
                // Normalizar/Canonizar los nombres de los ejercicios del nuevo registro
                val canonicalizedExercises = log.ejercicios.map {
                    val canonicalName = com.spc.nutricoach.util.ExerciseNormalizer.getCanonicalName(it.nombre_snapshot, existingNames)
                    it.copy(nombre_snapshot = canonicalName)
                }
                
                val canonicalizedLog = log.copy(ejercicios = canonicalizedExercises)
                
                when (val response = rutinaRepository.registrarEntrenamiento(clienteId, canonicalizedLog)) {
                    is ApiResponse.Success -> {
                        onComplete(true)
                    }
                    is ApiResponse.Error -> {
                        saveLogError = "Error del servidor (${response.code})"
                        onComplete(false)
                    }
                    is ApiResponse.Exception -> {
                        saveLogError = "Error de conexión"
                        onComplete(false)
                    }
                }
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
