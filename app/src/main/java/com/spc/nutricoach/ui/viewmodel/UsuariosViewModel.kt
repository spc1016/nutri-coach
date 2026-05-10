package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SessionManager
import com.spc.nutricoach.model.Cliente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsuariosViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    
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
                val clientes = NutriCoachApiClient.service.obtenerTodosClientes()
                val miId = sessionManager.getClienteId()
                // Filtrarse a sí mismo
                usuarios = clientes.filter { it.id != miId }
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
                val miId = sessionManager.getClienteId()
                if (miId != null) {
                    val seguidos = NutriCoachApiClient.service.obtenerSeguidos(miId)
                    myFollowingIds = seguidos.mapNotNull { it.id }.toSet()
                }
            } catch (e: Exception) {
                Log.e("USUARIOS", "Error al cargar seguidos", e)
            }
        }
    }

    fun toggleFollow(usuarioId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token != null) {
                    if (myFollowingIds.contains(usuarioId)) {
                        NutriCoachApiClient.service.dejarDeSeguirUsuario(usuarioId, "Bearer $token")
                        myFollowingIds = myFollowingIds - usuarioId
                    } else {
                        NutriCoachApiClient.service.seguirUsuario(usuarioId, "Bearer $token")
                        myFollowingIds = myFollowingIds + usuarioId
                    }
                }
            } catch (e: Exception) {
                Log.e("USUARIOS", "Error al hacer follow/unfollow", e)
            }
        }
    }
}
