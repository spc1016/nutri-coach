package com.spc.nutricoach.workout

import android.os.CountDownTimer
import com.spc.nutricoach.model.Dia
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object WorkoutManager {

    private val _rutinaIdActual = MutableStateFlow<String?>(null)
    val rutinaIdActual: StateFlow<String?> = _rutinaIdActual.asStateFlow()

    private val _diaActual = MutableStateFlow<Dia?>(null)
    val diaActual: StateFlow<Dia?> = _diaActual.asStateFlow()

    private val _currentExerciseIndex = MutableStateFlow(0)
    val currentExerciseIndex: StateFlow<Int> = _currentExerciseIndex.asStateFlow()

    private val _currentSet = MutableStateFlow(1)
    val currentSet: StateFlow<Int> = _currentSet.asStateFlow()

    private val _isResting = MutableStateFlow(false)
    val isResting: StateFlow<Boolean> = _isResting.asStateFlow()

    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeRemaining: StateFlow<Int> = _restTimeRemaining.asStateFlow()

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished.asStateFlow()
    
    private val _isStopped = MutableStateFlow(false)
    val isStopped: StateFlow<Boolean> = _isStopped.asStateFlow()

    private val _navigateToWorkoutEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToWorkoutEvent: SharedFlow<Unit> = _navigateToWorkoutEvent.asSharedFlow()

    private var timer: CountDownTimer? = null
    private var onRestFinishAction: (() -> Unit)? = null

    // Registro de progreso en tiempo real
    private val _completedExercises = mutableListOf<com.spc.nutricoach.data.EjercicioCompletado>()
    private val _currentExerciseSets = mutableListOf<com.spc.nutricoach.data.SerieCompletada>()

    fun triggerNavigationToWorkout() {
        _navigateToWorkoutEvent.tryEmit(Unit)
    }

    fun iniciarOReanudar(rutinaId: String, dia: Dia) {
        val esMismoDia = _rutinaIdActual.value == rutinaId && _diaActual.value?.nombre == dia.nombre
        val exercisesChanged = _diaActual.value?.ejercicios != dia.ejercicios
        val isFinishedOrStopped = _isFinished.value || _isStopped.value

        if (!esMismoDia || exercisesChanged || isFinishedOrStopped) {
            _rutinaIdActual.value = rutinaId
            _diaActual.value = dia
            _currentExerciseIndex.value = 0
            _currentSet.value = 1
            _isFinished.value = dia.ejercicios.isEmpty()
            _isStopped.value = false
            _completedExercises.clear()
            _currentExerciseSets.clear()
            resetTimer()
        }
    }

    fun reiniciarEntrenamiento() {
        if (_diaActual.value != null) {
            _currentExerciseIndex.value = 0
            _currentSet.value = 1
            _isFinished.value = false
            _isStopped.value = false
            _completedExercises.clear()
            _currentExerciseSets.clear()
            resetTimer()
        }
    }

    fun finishSet(reps: String = "", peso: Double = 0.0, descanso: Int = -1) {
        val dia = _diaActual.value ?: return
        val exerciseIndex = _currentExerciseIndex.value
        if (exerciseIndex >= dia.ejercicios.size) return
        
        val currentEjer = dia.ejercicios[exerciseIndex]
        val currentSet = _currentSet.value
        
        val finalReps = if (reps.isBlank()) currentEjer.repeticiones else reps
        val finalPeso = peso
        val finalDescanso = if (descanso == -1) (currentEjer.descanso?.toIntOrNull() ?: 0) else descanso
        
        // Registrar la serie actual completada
        _currentExerciseSets.add(com.spc.nutricoach.data.SerieCompletada(
            repeticiones = finalReps,
            peso = finalPeso,
            descanso_segundos = finalDescanso
        ))
        
        if (currentSet < currentEjer.series) {
            val desc = finalDescanso
            if (desc > 0) {
                startRest(desc)
            } else {
                _currentSet.value = currentSet + 1
            }
        } else {
            // Se han terminado todas las series de este ejercicio. Guardar ejercicio completado.
            _completedExercises.add(com.spc.nutricoach.data.EjercicioCompletado(
                ejercicio_id = currentEjer.ejercicioId,
                nombre_snapshot = currentEjer.nombreSnapshot,
                series = _currentExerciseSets.toList()
            ))
            _currentExerciseSets.clear()

            if (exerciseIndex + 1 < dia.ejercicios.size) {
                val desc = finalDescanso
                if (desc > 0) {
                    startRest(desc) {
                        _currentExerciseIndex.value = exerciseIndex + 1
                        _currentSet.value = 1
                    }
                } else {
                    _currentExerciseIndex.value = exerciseIndex + 1
                    _currentSet.value = 1
                }
            } else {
                _isFinished.value = true
                resetTimer()
            }
        }
    }
    
    fun skipRest() {
        endRest()
    }

    private fun startRest(seconds: Int, onFinish: (() -> Unit)? = null) {
        _isResting.value = true
        _restTimeRemaining.value = seconds
        onRestFinishAction = onFinish
        
        timer?.cancel()
        timer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                _restTimeRemaining.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                endRest()
            }
        }.start()
    }
    
    private fun endRest() {
        timer?.cancel()
        timer = null
        _isResting.value = false
        _restTimeRemaining.value = 0
        
        if (onRestFinishAction != null) {
            onRestFinishAction?.invoke()
            onRestFinishAction = null
        } else {
            _currentSet.value += 1
        }
    }

    fun resetTimer() {
        timer?.cancel()
        timer = null
        _isResting.value = false
        _restTimeRemaining.value = 0
        onRestFinishAction = null
    }
    
    fun stop() {
        resetTimer()
        _rutinaIdActual.value = null
        _diaActual.value = null
        _isFinished.value = false
        _isStopped.value = true
        _completedExercises.clear()
        _currentExerciseSets.clear()
    }

    fun getEntrenamientoLog(rutinaNombre: String, diaNombre: String): com.spc.nutricoach.data.EntrenamientoLog {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
        val fechaIso = sdf.format(java.util.Date())
        return com.spc.nutricoach.data.EntrenamientoLog(
            rutina_nombre = rutinaNombre,
            dia_nombre = diaNombre,
            fecha = fechaIso,
            ejercicios = _completedExercises.toList()
        )
    }
}

