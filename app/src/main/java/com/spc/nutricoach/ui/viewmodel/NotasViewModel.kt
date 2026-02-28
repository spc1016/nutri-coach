package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.local.AppDatabase
import com.spc.nutricoach.data.local.entity.NotasEntity
import com.spc.nutricoach.data.repository.NotasRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NotasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotasRepository
    private val _notas = MutableStateFlow<List<NotasEntity>>(emptyList())
    val notas: StateFlow<List<NotasEntity>> = _notas.asStateFlow()

    init {
        val notasDao = AppDatabase.getDatabase(application).notasDao()
        repository = NotasRepository(notasDao)
        viewModelScope.launch(Dispatchers.IO) {
            repository.todasLasNotas.collect { listaNotas ->
                _notas.value = listaNotas
            }
        }
    }

    fun agregarNota(titulo: String, contenido: String) {
        val nuevaNota = NotasEntity(titulo = titulo, contenido = contenido)
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarNota(nuevaNota)
        }
    }

    fun actualizarNota(nota: NotasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarNota(nota) 
        }
    }

    fun obtenerNotaPorId(id: Int): NotasEntity? {
        return repository.todasLasNotas.value.find { it.id == id } 
    }

    fun eliminarNota(nota: NotasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarNota(nota)
        }
    }
}
