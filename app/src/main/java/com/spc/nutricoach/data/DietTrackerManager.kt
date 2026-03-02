    package com.spc.nutricoach.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dietTrackerDataStore: DataStore<Preferences> by preferencesDataStore(name = "diet_tracker")

class DietTrackerManager(private val context: Context) {
    companion object {
        private val KEY_COMPLETED_MEALS = stringSetPreferencesKey("completed_meals")
    }

    val completedMealsFlow: Flow<Set<String>> = context.dietTrackerDataStore.data.map { prefs ->
        prefs[KEY_COMPLETED_MEALS] ?: emptySet()
    }

    suspend fun markMealCompleted(mealKey: String) {
        context.dietTrackerDataStore.edit { prefs ->
            val currentMeals = prefs[KEY_COMPLETED_MEALS] ?: emptySet()
            prefs[KEY_COMPLETED_MEALS] = currentMeals + mealKey
        }
    }

    suspend fun unmarkMealCompleted(mealKey: String) {
        context.dietTrackerDataStore.edit { prefs ->
            val currentMeals = prefs[KEY_COMPLETED_MEALS] ?: emptySet()
            prefs[KEY_COMPLETED_MEALS] = currentMeals - mealKey
        }
    }
}
