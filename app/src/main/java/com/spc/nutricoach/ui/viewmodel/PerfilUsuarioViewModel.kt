package com.spc.nutricoach.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.ApiResponse
import com.spc.nutricoach.data.ModificarClienteRequest
import com.spc.nutricoach.data.repository.AuthRepository
import com.spc.nutricoach.model.Cliente
import com.spc.nutricoach.util.CloudinaryUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class PerfilUsuarioViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    var nombre by mutableStateOf("")
    var email by mutableStateOf("")
    var telefono by mutableStateOf("")
    var edad by mutableStateOf("")
    var peso by mutableStateOf("")
    var altura by mutableStateOf("")
    var objetivo by mutableStateOf("")
    var fotoPerfil by mutableStateOf<String?>(null)

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
                val clienteId = authRepository.session.getClienteId()
                
                if (clienteId != null) {
                    when (val response = authRepository.obtenerCliente(clienteId)) {
                        is ApiResponse.Success -> {
                            val cliente = response.data
                            nombre = cliente.nombre
                            email = cliente.email
                            telefono = cliente.telefono ?: ""
                            edad = cliente.edad?.toString() ?: ""
                            peso = cliente.peso?.toString() ?: ""
                            altura = cliente.altura?.toString() ?: ""
                            objetivo = cliente.objetivo ?: ""
                            fotoPerfil = cliente.foto_perfil
                            seguidoresCount = cliente.seguidores_count
                            seguidosCount = cliente.seguidos_count
                            statusMessage = ""
                            authRepository.session.updateFotoPerfil(cliente.foto_perfil)
                        }
                        is ApiResponse.Error -> {
                            Log.e("PERFIL_API", "Error HTTP ${response.code}: ${response.message}")
                            statusMessage = "Error del servidor al cargar el perfil"
                        }
                        is ApiResponse.Exception -> {
                            Log.e("PERFIL_API", "Error de red al cargar perfil", response.throwable)
                            statusMessage = "Error de conexión. Comprueba tu red."
                        }
                    }
                } else {
                    statusMessage = "No se pudo obtener el ID del cliente o el token."
                }
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error inesperado al cargar perfil: ${e.message}", e)
                statusMessage = "Error inesperado al cargar perfil"
            } finally {
                isLoading = false
            }
        }
    }

    fun subirYActualizarFoto(context: Context, uri: Uri) {
        isLoading = true
        statusMessage = "Subiendo foto de perfil..."
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cloudName = "dsgu3bw8h"
                val uploadPreset = "nutricoach_preset"
                var attempt = 0
                var uploadedUrl: String? = null
                while (attempt < 3 && uploadedUrl == null) {
                    attempt++
                    uploadedUrl = CloudinaryUploader.uploadImage(
                        context = context,
                        imageUri = uri,
                        cloudName = cloudName,
                        uploadPreset = uploadPreset
                    )
                    if (uploadedUrl == null && attempt < 3) {
                        kotlinx.coroutines.delay(1000)
                    }
                }

                withContext(Dispatchers.Main) {
                    if (uploadedUrl != null) {
                        fotoPerfil = uploadedUrl
                        statusMessage = "Foto de perfil subida. Guarda los cambios para confirmar."
                        Toast.makeText(context, "Imagen cargada con éxito", Toast.LENGTH_SHORT).show()
                    } else {
                        statusMessage = "Error al subir la imagen"
                        Toast.makeText(context, "Fallo al subir la foto de perfil", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error subiendo foto de perfil", e)
                withContext(Dispatchers.Main) {
                    statusMessage = "Error al subir foto: ${e.message}"
                }
            } finally {
                withContext(Dispatchers.Main) {
                    isLoading = false
                }
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
                val clienteId = authRepository.session.getClienteId() ?: throw Exception("ID de cliente no encontrado")

                val request = ModificarClienteRequest(
                    nombre = nombre,
                    email = email,
                    telefono = telefono.ifBlank { null },
                    edad = edad.toIntOrNull(),
                    peso = peso.toDoubleOrNull(),
                    altura = altura.toDoubleOrNull(),
                    objetivo = objetivo.ifBlank { null },
                    genero = null, // No editamos el genero en este perfil pero mandamos null
                    foto_perfil = fotoPerfil
                )

                when (val response = authRepository.modificarCliente(clienteId, request)) {
                    is ApiResponse.Success -> {
                        // Actualizar el correo electrónico en SessionManager si fue cambiado
                        val currentRole = authRepository.session.getRole() ?: "cliente"
                        val currentToken = authRepository.session.getToken() ?: ""
                        authRepository.session.saveSession(currentToken, currentRole, clienteId, email, fotoPerfil)
                        statusMessage = "Perfil actualizado correctamente"
                    }
                    is ApiResponse.Error -> {
                        Log.e("PERFIL_API", "Error al guardar perfil HTTP ${response.code}: ${response.message}")
                        statusMessage = "Error del servidor al guardar: ${response.message}"
                    }
                    is ApiResponse.Exception -> {
                        Log.e("PERFIL_API", "Error de red al guardar perfil", response.throwable)
                        statusMessage = "Error de conexión. Comprueba tu red."
                    }
                }
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
                val clienteId = authRepository.session.getClienteId()
                if (clienteId != null) {
                    val followersResponse = authRepository.obtenerSeguidores(clienteId)
                    val followingResponse = authRepository.obtenerSeguidos(clienteId)
                    if (followersResponse is ApiResponse.Success && followingResponse is ApiResponse.Success) {
                        seguidoresList = followersResponse.data
                        seguidosList = followingResponse.data
                    }
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
                    when (authRepository.dejarDeSeguirUsuario(usuarioId)) {
                        is ApiResponse.Success -> {
                            // Update lists locally
                            seguidosList = seguidosList.filter { it.id != usuarioId }
                            seguidosCount = seguidosList.size
                        }
                        else -> {}
                    }
                
            } catch (e: Exception) {
                Log.e("PERFIL_API", "Error al dejar de seguir: ${e.message}")
            }
        }
    }
}
