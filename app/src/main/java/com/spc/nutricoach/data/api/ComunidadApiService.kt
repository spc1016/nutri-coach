package com.spc.nutricoach.data.api

import com.spc.nutricoach.model.Post
import com.spc.nutricoach.data.CrearPostRequest
import com.spc.nutricoach.data.RegistroApiResponse
import com.spc.nutricoach.data.ToggleLikeResponse
import com.spc.nutricoach.data.ComentarioRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.DELETE
import retrofit2.http.Path
import retrofit2.http.Header
import retrofit2.http.Query

interface ComunidadApiService {
    @GET("posts")
    suspend fun obtenerPosts(
        @Query("limit") limit: Int,
        @Query("cursor") cursor: String?
    ): List<Post>

    @GET("posts/{id}")
    suspend fun obtenerPost(
        @Path("id") postId: String
    ): Post

    @POST("posts")
    suspend fun crearPost(
        @Header("Authorization") token: String,
        @Body request: CrearPostRequest
    ): RegistroApiResponse

    @POST("posts/{id}/like")
    suspend fun toggleLikePost(
        @Path("id") postId: String,
        @Header("Authorization") token: String
    ): ToggleLikeResponse

    @POST("posts/{id}/comentarios")
    suspend fun comentarPost(
        @Path("id") postId: String,
        @Header("Authorization") token: String,
        @Body request: ComentarioRequest
    )

    @DELETE("posts/{id}/comentarios/{comentario_index}")
    suspend fun eliminarComentario(
        @Path("id") postId: String,
        @Path("comentario_index") comentarioIndex: Int,
        @Header("Authorization") token: String
    )
}
