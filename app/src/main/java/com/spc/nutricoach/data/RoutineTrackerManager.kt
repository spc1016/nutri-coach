package com.spc.nutricoach.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.routineTrackerDataStore: DataStore<Preferences> by preferencesDataStore(name = "routine_tracker")

class RoutineTrackerManager(private val context: Context) {
    
    fun getExerciseWeightFlow(clienteId: String, rutinaId: String, exerciseKey: String): Flow<String?> {
        val key = stringPreferencesKey("weight_${clienteId}_${rutinaId}_${exerciseKey}")
        return context.routineTrackerDataStore.data.map { prefs ->
            prefs[key]
        }
    }

    suspend fun saveExerciseWeight(clienteId: String, rutinaId: String, exerciseKey: String, weight: String) {
        val key = stringPreferencesKey("weight_${clienteId}_${rutinaId}_${exerciseKey}")
        context.routineTrackerDataStore.edit { prefs ->
            prefs[key] = weight
        }
    }
}
