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
    suspend fun obtenerPosts(limit: Int, cursor: String?): ApiResponse<List<Post>> = safeApiCall {
        comunidadApiService.obtenerPosts(limit, cursor)
    }

    suspend fun obtenerPost(postId: String): ApiResponse<Post> = safeApiCall {
        comunidadApiService.obtenerPost(postId)
    }

    suspend fun crearPost(request: CrearPostRequest): ApiResponse<RegistroApiResponse> = safeApiCall {
        comunidadApiService.crearPost(request)
    }

    suspend fun toggleLikePost(postId: String): ApiResponse<ToggleLikeResponse> = safeApiCall {
        comunidadApiService.toggleLikePost(postId)
    }

    suspend fun comentarPost(postId: String, request: ComentarioRequest): ApiResponse<Unit> = safeApiCall {
        comunidadApiService.comentarPost(postId, request)
    }

    suspend fun eliminarComentario(postId: String, comentarioIndex: Int): ApiResponse<Unit> = safeApiCall {
        comunidadApiService.eliminarComentario(postId, comentarioIndex)
    }

    val session = sessionManager
}
