package com.spc.nutricoach.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.spc.nutricoach.data.local.entity.NotasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotasDao {
    @Query("SELECT * FROM notas WHERE clienteId = :clienteId ORDER BY fechaCreacion DESC")
    fun obtenerNotasPorCliente(clienteId: String): Flow<List<NotasEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarNota(nota: NotasEntity)

    @Delete
    suspend fun eliminarNota(nota: NotasEntity)
}
