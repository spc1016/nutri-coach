package com.spc.nutricoach.ui.viewmodel

import android.os.CountDownTimer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.model.Ejercicio
import com.spc.nutricoach.model.Rutina

class EntrenamientoViewModel : ViewModel() {

    var rutinaIdActual by mutableStateOf<String?>(null)
        private set

    var diaActual by mutableStateOf<Dia?>(null)
        private set

    var currentExerciseIndex by mutableIntStateOf(0)
        private set

    var currentSet by mutableIntStateOf(1)
        private set

    var isResting by mutableStateOf(false)
        private set

    var restTimeRemaining by mutableIntStateOf(0)
        private set

    var isFinished by mutableStateOf(false)
        private set

    private var timer: CountDownTimer? = null

    fun iniciarOReanudar(rutinaId: String, dia: Dia) {
        if (this.rutinaIdActual != rutinaId || this.diaActual?.nombre != dia.nombre) {
            this.rutinaIdActual = rutinaId
            this.diaActual = dia
            currentExerciseIndex = 0
            currentSet = 1
            isFinished = false
            if (dia.ejercicios.isEmpty()) {
                isFinished = true
            }
            resetTimer()
        }
    }

    fun reiniciarEntrenamiento() {
        if (diaActual != null) {
            currentExerciseIndex = 0
            currentSet = 1
            isFinished = false
            resetTimer()
        }
    }

    fun finishSet() {
        val dia = diaActual ?: return
        if (currentExerciseIndex >= dia.ejercicios.size) return
        
        val currentEjer = dia.ejercicios[currentExerciseIndex]
        
        if (currentSet < currentEjer.series) {
            // Start rest, prepare for next set
            val desc = currentEjer.descanso?.toIntOrNull() ?: 0
            if (desc > 0) {
                startRest(desc)
            } else {
                currentSet++
            }
        } else {
            // Next exercise
            if (currentExerciseIndex + 1 < dia.ejercicios.size) {
                val desc = currentEjer.descanso?.toIntOrNull() ?: 0
                if (desc > 0) {
                    startRest(desc) {
                        currentExerciseIndex++
                        currentSet = 1
                    }
                } else {
                    currentExerciseIndex++
                    currentSet = 1
                }
            } else {
                isFinished = true
            }
        }
    }
    
    fun skipRest() {
        endRest()
    }
    
    private var onRestFinishAction: (() -> Unit)? = null

    private fun startRest(seconds: Int, onFinish: (() -> Unit)? = null) {
        isResting = true
        restTimeRemaining = seconds
        onRestFinishAction = onFinish
        
        timer?.cancel()
        timer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                restTimeRemaining = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                endRest()
            }
        }.start()
    }
    
    fun endRest() {
        timer?.cancel()
        timer = null
        isResting = false
        restTimeRemaining = 0
        if (onRestFinishAction != null) {
            onRestFinishAction?.invoke()
            onRestFinishAction = null
        } else {
            currentSet++
        }
    }

    private fun resetTimer() {
        timer?.cancel()
        timer = null
        isResting = false
        restTimeRemaining = 0
        onRestFinishAction = null
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}
