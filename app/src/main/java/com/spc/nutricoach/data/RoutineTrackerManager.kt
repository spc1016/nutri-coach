package com.spc.nutricoach.data

import android.content.Context
import com.spc.nutricoach.data.local.AppDatabase
import com.spc.nutricoach.data.local.entity.PesoHistorialEntity
import kotlinx.coroutines.flow.Flow

class RoutineTrackerManager(private val context: Context) {
    
    private val pesoHistorialDao = AppDatabase.getDatabase(context).pesoHistorialDao()

    fun getExerciseWeightFlow(clienteId: String, rutinaId: String, exerciseKey: String): Flow<String?> {
        return pesoHistorialDao.obtenerUltimoPeso(clienteId, rutinaId, exerciseKey)
    }

    suspend fun saveExerciseWeight(clienteId: String, rutinaId: String, exerciseKey: String, weight: String) {
        val entity = PesoHistorialEntity(
            clienteId = clienteId,
            rutinaId = rutinaId,
            exerciseKey = exerciseKey,
            peso = weight
        )
        pesoHistorialDao.insertarPeso(entity)
    }
}
