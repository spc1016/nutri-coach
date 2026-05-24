package com.spc.nutricoach.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.spc.nutricoach.data.local.entity.PesoHistorialEntity

@Dao
interface PesoHistorialDao {
    @Query("SELECT peso FROM peso_historial WHERE clienteId = :clienteId AND rutinaId = :rutinaId AND exerciseKey = :exerciseKey ORDER BY fecha DESC LIMIT 1")
    fun obtenerUltimoPeso(clienteId: String, rutinaId: String, exerciseKey: String): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarPeso(pesoHistorial: PesoHistorialEntity)
}
