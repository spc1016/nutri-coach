package com.spc.nutricoach.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.model.Dia
import com.spc.nutricoach.workout.WorkoutManager
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class EntrenamientoViewModel : ViewModel() {



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

    fun finishSet() {
        WorkoutManager.finishSet()
    }
    
    fun skipRest() {
        WorkoutManager.skipRest()
    }
}
