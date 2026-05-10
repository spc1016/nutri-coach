package com.spc.nutricoach.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.ModificarClienteRequest
import com.spc.nutricoach.data.NutriCoachApiClient
import com.spc.nutricoach.data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import com.spc.nutricoach.model.Cliente

class PerfilUsuarioViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var telefono by mutableStateOf("")
    var edad by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")
    var objetivo by mutableStateOf("")

    var seguidoresCount by mutableStateOf(0)
    var seguidosCount by mutableStateOf(0)
    
    var isEditing by mutableStateOf(false)

    var statusMessage by mutableStateOf("")
        private set

    var isLoading by mutableStateOf(false)
        private set
        
    var seguidoresList by mutableStateOf<List<Cliente>>(emptyList())
    var seguidosList by mutableStateOf<List<Cliente>>(emptyList())
    var isLoadingListas by mutableStateOf(false)

    init {
        cargarPerfil()
    }

    private fun cargarPerfil() {
        isLoading = true
        statusMessage = ""
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clienteId = sessionManager.getClienteId()
                val token = sessionManager.getToken()
                
                if (clienteId != null && token != null) {
                    val cliente = NutriCoachApiClient.service.obtenerCliente(clienteId, "Bearer $token")
                    nombre = cliente.nombre
                    email = cliente.email
                    telefono = cliente.telefono ?: ""
                    edad = cliente.edad?.toString() ?: ""
                    peso = cliente.peso?.toString() ?: ""
                    altura = cliente.altura?.toString() ?: ""
                    objetivo = cliente.objetivo ?: ""
                    seguidoresCount = cliente.seguidores_count
                    seguidosCount = cliente.seguidos_count
                } else {
                    statusMessage = "No se pudo obtener el ID del cliente o el token."
                }
            } catch (e: HttpException) {
                Log.e("PERFIL_API", "Error al cargar perfil HTTP ${e.code()}: ${e.message()}")
                statusMessage = "Error del servidor al cargar el perfil"
            } catch (e: IOException) {
                Log.e("PERFIL_API", "Error de red al cargar perfil: ${e.message}", e)
                statusMessage = "Error de conexión. Comprueba tu red."
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error inesperado al cargar perfil: ${e.message}", e)
                statusMessage = "Error inesperado al cargar perfil"
            } finally {
                isLoading = false
            }
        }
    }

    fun guardarCambios() {
        if (nombre.isBlank() || email.isBlank()) {
            statusMessage = "Nombre y email son obligatorios"
            return
        }

        isLoading = true
        statusMessage = "Guardando cambios..."
        
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val clienteId = sessionManager.getClienteId() ?: throw Exception("ID de cliente no encontrado")
                val token = sessionManager.getToken() ?: throw Exception("Token de autenticación no encontrado")

                val request = ModificarClienteRequest(
                    nombre = nombre,
                    email = email,
                    telefono = telefono.ifBlank { null },
                    edad = edad.toIntOrNull(),
                    peso = peso.toDoubleOrNull(),
                    altura = altura.toDoubleOrNull(),
                    objetivo = objetivo.ifBlank { null },
                    genero = null // No editamos el genero en este perfil pero mandamos null
                )

                NutriCoachApiClient.service.modificarCliente(clienteId, "Bearer $token", request)
                
                // Actualizar el correo electrónico en SessionManager si fue cambiado
                val currentRole = sessionManager.getRole() ?: "cliente"
                val currentToken = sessionManager.getToken() ?: ""
                sessionManager.saveSession(currentToken, currentRole, clienteId, email)

                statusMessage = "Perfil actualizado correctamente"
            } catch (e: HttpException) {
                Log.e("PERFIL_API", "Error al guardar perfil HTTP ${e.code()}: ${e.message()}")
                statusMessage = "Error del servidor al guardar"
            } catch (e: IOException) {
                Log.e("PERFIL_API", "Error de red al guardar perfil: ${e.message}", e)
                statusMessage = "Error de conexión. Comprueba tu red."
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error inesperado al guardar perfil: ${e.message}", e)
                statusMessage = "Error inesperado: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun cargarListas() {
        viewModelScope.launch(Dispatchers.IO) {
            isLoadingListas = true
            try {
                val clienteId = sessionManager.getClienteId()
                if (clienteId != null) {
                    val followers = NutriCoachApiClient.service.obtenerSeguidores(clienteId)
                    val following = NutriCoachApiClient.service.obtenerSeguidos(clienteId)
                    seguidoresList = followers
                    seguidosList = following
                }
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error al cargar listas: ${e.message}")
            } finally {
                isLoadingListas = false
            }
        }
    }

    fun dejarDeSeguir(usuarioId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token != null) {
                    NutriCoachApiClient.service.dejarDeSeguirUsuario(usuarioId, "Bearer $token")
                    // Update lists locally
                    seguidosList = seguidosList.filter { it.id != usuarioId }
                    seguidosCount = seguidosList.size
                }
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error al dejar de seguir: ${e.message}")
            }
        }
    }
}
