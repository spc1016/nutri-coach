package com.spc.nutricoach.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Comentario(
    @SerialName("autor_id") val autorId: String = "",
    @SerialName("autor_nombre") val autorNombre: String = "",
    val texto: String = "",
    val fecha: String = ""
)

@Serializable
data class Post(
    @SerialName("_id") val id: String = "",
    @SerialName("autor_id") val autorId: String = "",
    @SerialName("autor_nombre") val autorNombre: String = "",
    val texto: String = "",
    @SerialName("rutina_id") val rutinaId: String? = null,
    @SerialName("dieta_id") val dietaId: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null,
    val likes: Int = 0,
    @SerialName("liked_by") val likedBy: List<String> = emptyList(),
    val comentarios: List<Comentario> = emptyList(),
    @SerialName("fecha_creacion") val fechaCreacion: String = "",
    @SerialName("autor_foto_perfil") val autorFotoPerfil: String? = null,
    val activa: Boolean = true
)

@Serializable
data class CrearPostRequest(
    val texto: String,
    @SerialName("rutina_id") val rutinaId: String? = null,
    @SerialName("dieta_id") val dietaId: String? = null,
    @SerialName("imagen_url") val imagenUrl: String? = null
)

@Serializable
data class ComentarioRequest(
    val texto: String
)

@Serializable
data class ToggleLikeResponse(
    val mensaje: String,
    val liked: Boolean
)
