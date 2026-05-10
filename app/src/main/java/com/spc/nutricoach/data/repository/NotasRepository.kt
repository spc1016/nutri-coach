package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.local.dao.NotasDao
import com.spc.nutricoach.data.local.entity.NotasEntity
import kotlinx.coroutines.flow.Flow

class NotasRepository(private val notasDao: NotasDao) {

    fun obtenerNotasPorCliente(clienteId: String): Flow<List<NotasEntity>> {
        return notasDao.obtenerNotasPorCliente(clienteId)
    }

    suspend fun insertarNota(nota: NotasEntity) {
        notasDao.insertarNota(nota)
    }

    suspend fun eliminarNota(nota: NotasEntity) {
        notasDao.eliminarNota(nota)
    }
}
