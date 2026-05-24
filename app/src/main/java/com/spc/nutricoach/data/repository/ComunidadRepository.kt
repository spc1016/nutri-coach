package com.spc.nutricoach.data.repository

import com.spc.nutricoach.data.*
import com.spc.nutricoach.data.api.ComunidadApiService
import com.spc.nutricoach.model.Post
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ComunidadRepository @Inject constructor(
    private val comunidadApiService: ComunidadApiService,
    private val sessionManager: SessionManager
) {
    suspend fun obtenerPosts(): ApiResponse<List<Post>> = safeApiCall {
        comunidadApiService.obtenerPosts()
    }

    suspend fun crearPost(token: String, request: CrearPostRequest): ApiResponse<RegistroApiResponse> = safeApiCall {
        comunidadApiService.crearPost(token, request)
    }

    suspend fun toggleLikePost(postId: String, token: String): ApiResponse<ToggleLikeResponse> = safeApiCall {
        comunidadApiService.toggleLikePost(postId, token)
    }

    suspend fun comentarPost(postId: String, token: String, request: ComentarioRequest): ApiResponse<Unit> = safeApiCall {
        comunidadApiService.comentarPost(postId, token, request)
    }

    suspend fun eliminarComentario(postId: String, comentarioIndex: Int, token: String): ApiResponse<Unit> = safeApiCall {
        comunidadApiService.eliminarComentario(postId, comentarioIndex, token)
    }

    val session = sessionManager
}
