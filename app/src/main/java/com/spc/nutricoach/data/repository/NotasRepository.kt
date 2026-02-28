package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.local.dao.NotasDao
import com.spc.nutricoach.data.local.entity.NotasEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class NotasRepository(private val notasDao: NotasDao) {

    val todasLasNotas: StateFlow<List<NotasEntity>> = notasDao.obtenerTodasLasNotas()
        .stateIn(
            scope = CoroutineScope(Dispatchers.IO),
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    suspend fun insertarNota(nota: NotasEntity) {
        notasDao.insertarNota(nota)
    }

    suspend fun eliminarNota(nota: NotasEntity) {
        notasDao.eliminarNota(nota)
    }
}
