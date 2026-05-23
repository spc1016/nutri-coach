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
import kotlinx.coroutines.Job
import com.spc.nutricoach.data.SessionManager

class NotasViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: NotasRepository
    private val sessionManager = SessionManager(application)
    
    private val _notas = MutableStateFlow<List<NotasEntity>>(emptyList())
    val notas: StateFlow<List<NotasEntity>> = _notas.asStateFlow()

    private var currentClienteId: String? = null
    private var notesJob: Job? = null

    init {
        val notasDao = AppDatabase.getDatabase(application).notasDao()
        repository = NotasRepository(notasDao)
        cargarNotas()
    }
    
    fun cargarNotas(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteId = sessionManager.getClienteId()
            if (clienteId.isNullOrBlank()) return@launch
            
            if (!force && currentClienteId == clienteId) return@launch
            
            currentClienteId = clienteId
            
            notesJob?.cancel()
            notesJob = launch {
                repository.obtenerNotasPorCliente(clienteId).collect { listaNotas ->
                    _notas.value = listaNotas
                }
            }
        }
    }

    fun agregarNota(titulo: String, contenido: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val clienteId = currentClienteId ?: sessionManager.getClienteId()
            if (clienteId.isNullOrBlank()) return@launch
            val nuevaNota = NotasEntity(clienteId = clienteId, titulo = titulo, contenido = contenido)
            repository.insertarNota(nuevaNota)
        }
    }

    fun actualizarNota(nota: NotasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertarNota(nota) 
        }
    }

    fun obtenerNotaPorId(id: Int): NotasEntity? {
        return _notas.value.find { it.id == id } 
    }

    fun eliminarNota(nota: NotasEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.eliminarNota(nota)
        }
    }
}
