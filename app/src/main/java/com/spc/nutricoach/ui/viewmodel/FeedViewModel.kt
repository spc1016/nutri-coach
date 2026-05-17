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
import com.spc.nutricoach.model.ComentarioRequest
import com.spc.nutricoach.model.CrearPostRequest
import com.spc.nutricoach.model.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class FeedViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)

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

    fun cargarPosts(force: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            if (!force && posts.isNotEmpty()) {
                return@launch
            }
            isLoading = true
            error = null
            try {
                val resultado = NutriCoachApiClient.service.obtenerPosts()
                posts = resultado
            } catch (e: HttpException) {
                error = "Error del servidor (${e.code()})"
                Log.e("FeedViewModel", "HTTP Exception: ${e.message()}")
            } catch (e: IOException) {
                error = "Error de conexión"
                Log.e("FeedViewModel", "IO Exception", e)
            } catch (e: Exception) {
                error = "Error inesperado: ${e.message}"
                Log.e("FeedViewModel", "Unexpected exception", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun crearPost(texto: String, rutinaId: String?, dietaId: String?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }
                val request = CrearPostRequest(texto = texto, rutinaId = rutinaId, dietaId = dietaId)
                NutriCoachApiClient.service.crearPost("Bearer $token", request)
                cargarPosts(force = true) // Recargar feed
                withContext(Dispatchers.Main) {
                    onResult(true, null)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, "Error: ${e.message}")
                }
            }
        }
    }

    fun toggleLike(postId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch
                
                NutriCoachApiClient.service.toggleLikePost(postId, "Bearer $token")
                cargarPosts(force = true)
            } catch (e: Exception) {
                Log.e("FEED", "Error toggling like", e)
            }
        }
    }

    fun comentarPost(postId: String, texto: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) {
                    onResult(false, "No hay sesión activa")
                    return@launch
                }
                val request = ComentarioRequest(texto = texto)
                NutriCoachApiClient.service.comentarPost(postId, "Bearer $token", request)
                cargarPosts(force = true)
                withContext(Dispatchers.Main) {
                    onResult(true, null)
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
                val currentUserId = sessionManager.clienteIdFlow.first()
                if (currentUserId.isNullOrBlank()) return@launch

                val todosClientes = NutriCoachApiClient.service.obtenerTodosClientes()
                val misSeguidos = NutriCoachApiClient.service.obtenerSeguidos(currentUserId)
                
                clientes = todosClientes.filter { it.id != currentUserId }
                seguidosIds = misSeguidos.mapNotNull { it.id }.toSet()
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error cargando usuarios", e)
            }
        }
    }

    fun toggleFollow(userId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val token = sessionManager.getToken()
                if (token.isNullOrBlank()) return@launch

                val isFollowing = seguidosIds.contains(userId)
                if (isFollowing) {
                    NutriCoachApiClient.service.dejarDeSeguirUsuario(userId, "Bearer $token")
                    seguidosIds = seguidosIds - userId
                } else {
                    NutriCoachApiClient.service.seguirUsuario(userId, "Bearer $token")
                    seguidosIds = seguidosIds + userId
                }
            } catch (e: Exception) {
                Log.e("FeedViewModel", "Error toggling follow", e)
            }
        }
    }
}
