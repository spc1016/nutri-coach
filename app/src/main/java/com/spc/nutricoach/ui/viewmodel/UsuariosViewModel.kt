package com.spc.nutricoach.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.ApiResponse
import com.spc.nutricoach.data.repository.AuthRepository
import com.spc.nutricoach.model.Cliente
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.spc.nutricoach.data.repository.RutinaRepository
import com.spc.nutricoach.model.Rutina

@HiltViewModel
class UsuariosViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val rutinaRepository: RutinaRepository
) : ViewModel() {

    fun cargarDetallesUsuario(
        clienteId: String,
        onResult: (Cliente?, List<Rutina>?) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clientResponse = authRepository.obtenerCliente(clienteId)
                val routinesResponse = rutinaRepository.obtenerRutinasCliente(clienteId)
                
                val cliente = (clientResponse as? ApiResponse.Success)?.data
                val rutinas = (routinesResponse as? ApiResponse.Success)?.data
                
                onResult(cliente, rutinas)
            } catch (e: Exception) {
                Log.e("USUARIOS_VM", "Error al cargar detalles de usuario", e)
                onResult(null, null)
            }
        }
    }
    
    var usuarios by mutableStateOf<List<Cliente>>(emptyList())
        private set

    var searchQuery by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
        private set
        
    var myFollowingIds by mutableStateOf<Set<String>>(emptySet())
        private set

    init {
        cargarUsuarios()
        cargarMisSeguidos()
    }

    fun cargarUsuarios(force: Boolean = false) {
        if (usuarios.isNotEmpty() && !force) return
        isLoading = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val miId = authRepository.session.getClienteId()
                when (val response = authRepository.obtenerTodosClientes()) {
                    is ApiResponse.Success -> {
                        // Filtrarse a sí mismo
                        usuarios = response.data.filter { it.id != miId }
                    }
                    is ApiResponse.Error -> {
                        Log.e("USUARIOS", "Error al cargar usuarios HTTP ${response.code}: ${response.message}")
                    }
                    is ApiResponse.Exception -> {
                        Log.e("USUARIOS", "Error de red al cargar usuarios", response.throwable)
                    }
                }
            } catch (e: Exception) {
                Log.e("USUARIOS", "Error al cargar usuarios", e)
            } finally {
                isLoading = false
            }
        }
    }
    
    private fun cargarMisSeguidos() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val miId = authRepository.session.getClienteId()
                if (miId != null) {
                    when (val response = authRepository.obtenerSeguidos(miId)) {
                        is ApiResponse.Success -> {
                            myFollowingIds = response.data.mapNotNull { it.id }.toSet()
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("USUARIOS", "Error al cargar seguidos", e)
            }
        }
    }

    fun toggleFollow(usuarioId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (myFollowingIds.contains(usuarioId)) {
                    when (authRepository.dejarDeSeguirUsuario(usuarioId)) {
                        is ApiResponse.Success -> {
                            myFollowingIds = myFollowingIds - usuarioId
                        }
                        else -> {}
                    }
                } else {
                    when (authRepository.seguirUsuario(usuarioId)) {
                        is ApiResponse.Success -> {
                            myFollowingIds = myFollowingIds + usuarioId
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("USUARIOS", "Error al hacer follow/unfollow", e)
            }
        }
    }
}
