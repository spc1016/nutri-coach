package com.spc.nutricoach.ui.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.repository.AuthRepository
import com.spc.nutricoach.data.repository.ComunidadRepository
import com.spc.nutricoach.model.Cliente
import com.spc.nutricoach.model.Post
import com.spc.nutricoach.util.CloudinaryUploader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val comunidadRepository: ComunidadRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    var posts by mutableStateOf<List<Post>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var error by mutableStateOf<String?>(null)
        private set

    var clientes by mutableStateOf<List<Cliente>>(emptyList())
        private set

    var seguidosIds by mutableStateOf<Set<String>>(emptySet())
        private set

    var selectedTab by mutableStateOf(0)
    var searchQuery by mutableStateOf("")

    var estaPublicandoPost by mutableStateOf(false)
        private set
    var progresoPublicacion by mutableStateOf("")
        private set

    fun cargarPosts(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!force && posts.isNotEmpty()) {
                return@launch
            }
            isLoading = true
            error = null
            
            when (val response = comunidadRepository.obtenerPosts()) {
                is ApiResponse.Success -> {
                    posts = response.data
                    error = null
                }
                is ApiResponse.Error -> {
                    error = "Error del servidor (${response.code})"
                    Log.e("FeedViewModel", "HTTP Exception: ${response.message}")
                }
                is ApiResponse.Exception -> {
                    error = "Error de conexión"
                    Log.e("FeedViewModel", "IO Exception", response.throwable)
                }
            }
            isLoading = false
        }
    }

    fun crearPost(texto: String, rutinaId: String?, dietaId: String?, imagenUrl: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = authRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }
                val request = CrearPostRequest(texto = texto, rutina_id = rutinaId) // Wait, CrearPostRequest has texto and optional routine
                when (val response = comunidadRepository.crearPost("Bearer $token", request)) {
                    is ApiResponse.Success -> {
                        cargarPosts(force = true)
                        withContext(Dispatchers.Main) {
                            onResult(true, null)
                        }
                    }
                    is ApiResponse.Error -> {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Error: ${response.message}")
                        }
                    }
                    is ApiResponse.Exception -> {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Error de red")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.message}")
                }
            }
        }
    }

    fun publicarPostConImagenes(
        context: Context,
        texto: String,
        rutinaId: String?,
        dietaId: String?,
        uris: List<Uri>,
        onInicio: () -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            estaPublicandoPost = true
            progresoPublicacion = "Subiendo imágenes..."
            
            withContext(Dispatchers.Main) {
                onInicio() // Cerrar pantalla inmediatamente
            }
            
            try {
                val cloudName = "dsgu3bw8h"
                val uploadPreset = "nutricoach_preset"

                // Subida en paralelo con reintentos
                val deferredUrls = uris.map { uri ->
                    async {
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
                        uploadedUrl
                    }
                }
                
                val uploadedUrls = deferredUrls.awaitAll()
                
                if (uploadedUrls.any { it == null }) {
                    estaPublicandoPost = false
                    progresoPublicacion = ""
                    withContext(Dispatchers.Main) {
                        android.widget.Toast.makeText(context, "Error al subir una o más imágenes. Publicación cancelada.", android.widget.Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                
                val finalUrls = uploadedUrls.filterNotNull()
                val finalImageUrl = if (finalUrls.isEmpty()) null else {
                    "[" + finalUrls.joinToString(",") { "\"$it\"" } + "]"
                }
                
                progresoPublicacion = "Creando publicación..."
                
                val token = authRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    estaPublicandoPost = false
                    progresoPublicacion = ""
                    return@launch
                }
                
                val request = CrearPostRequest(
                    texto = texto,
                    rutina_id = rutinaId
                )
                
                when (val response = comunidadRepository.crearPost("Bearer $token", request)) {
                    is ApiResponse.Success -> {
                        cargarPosts(force = true)
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(context, "¡Publicación creada con éxito!", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                    else -> {
                        withContext(Dispatchers.Main) {
                            android.widget.Toast.makeText(context, "Fallo al registrar la publicación", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error al publicar en segundo plano", e)
                withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Error al crear la publicación: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                }
            } finally {
                estaPublicandoPost = false
                progresoPublicacion = ""
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = authRepository.session.getToken()
                if (token.isNullOrBlank()) return@launch
                
                when (comunidadRepository.toggleLikePost(postId, "Bearer $token")) {
                    is ApiResponse.Success -> {
                        cargarPosts(force = true)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                Log.e("FEED", "Error toggling like", e)
            }
        }
    }

    fun comentarPost(postId: String, texto: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = authRepository.session.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }
                val request = ComentarioRequest(texto = texto)
                when (val response = comunidadRepository.comentarPost(postId, "Bearer $token", request)) {
                    is ApiResponse.Success -> {
                        cargarPosts(force = true)
                        withContext(Dispatchers.Main) {
                            onResult(true, null)
                        }
                    }
                    is ApiResponse.Error -> {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Error: ${response.message}")
                        }
                    }
                    is ApiResponse.Exception -> {
                        withContext(Dispatchers.Main) {
                            onResult(false, "Error de red")
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Error al comentar: ${e.message}")
                }
            }
        }
    }

    fun cargarUsuarios() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUserId = authRepository.session.clienteIdFlow.first()
                if (currentUserId.isNullOrBlank()) return@launch

                val responseTodos = authRepository.obtenerTodosClientes()
                val responseSeguidos = authRepository.obtenerSeguidos(currentUserId)
                
                if (responseTodos is ApiResponse.Success && responseSeguidos is ApiResponse.Success) {
                    clientes = responseTodos.data.filter { it.id != currentUserId }
                    seguidosIds = responseSeguidos.data.mapNotNull { it.id }.toSet()
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error cargando usuarios", e)
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = authRepository.session.getToken()
                if (token.isNullOrBlank()) return@launch

                val isFollowing = seguidosIds.contains(userId)
                if (isFollowing) {
                    when (authRepository.dejarDeSeguirUsuario(userId, "Bearer $token")) {
                        is ApiResponse.Success -> {
                            seguidosIds = seguidosIds - userId
                        }
                        else -> {}
                    }
                } else {
                    when (authRepository.seguirUsuario(userId, "Bearer $token")) {
                        is ApiResponse.Success -> {
                            seguidosIds = seguidosIds + userId
                        }
                        else -> {}
                    }
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error toggling follow", e)
            }
        }
    }
}
