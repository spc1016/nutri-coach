@file:Suppress("unused", "RedundantQualifierName", "PropertyName")

package com.spc.nutricoach.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginApiResponse(
    val token: String,
    val role: String
)

@Serializable
data class RegistroClienteRequest(
    val nombre: String,
    val email: String,
    val password: String,
    val telefono: String? = null,
    val edad: Int? = null,
    val peso: Double? = null,
    val altura: Double? = null,
    val genero: String? = null,
    val objetivo: String? = null
)

@Serializable
data class RegistroApiResponse(
    val id: String
)

@Serializable
data class SendCodeRequest(
    val nombre: String,
    val email: String,
    val password: String
)

@Serializable
data class VerifyCodeRequest(
    val email: String,
    val code: String
)

@Serializable
data class ResendCodeRequest(
    val email: String
)

@Serializable
data class MessageResponse(
    val message: String
)

@Serializable
data class ModificarClienteRequest(
    val nombre: String? = null,
    val email: String? = null,
    val password: String? = null,
    val telefono: String? = null,
    val edad: Int? = null,
    val peso: Double? = null,
    val altura: Double? = null,
    val objetivo: String? = null,
    val genero: String? = null
)

@Serializable
data class CrearRutinaRequest(
    val nombre: String,
    val cliente_id: String,
    val dias: List<com.spc.nutricoach.model.Dia> = emptyList(),
    val activa: Boolean = true,
    val publica: Boolean = false
)

@Serializable
data class CrearRutinaResponse(
    val id: String
)

@Serializable
data class AgregarDiaRequest(
    val nombre: String,
    val ejercicios: List<com.spc.nutricoach.model.Ejercicio> = emptyList()
)

@Serializable
data class AgregarEjercicioRequest(
    val ejercicio_id: String,
    val nombre_snapshot: String,
    val series: Int,
    val repeticiones: Int,
    val descanso_segundos: Int
)

@Serializable
data class ModificarRutinaRequest(
    val nombre: String? = null,
    val publica: Boolean? = null
)

@Serializable
data class CrearDietaRequest(
    val nombre: String,
    val cliente_id: String,
    val kcal_objetivo: Int = 0,
    val comidas: List<com.spc.nutricoach.model.Comida> = emptyList(),
    val activa: Boolean = true,
    val publica: Boolean = false
)

@Serializable
data class CrearDietaResponse(
    val id: String
)

@Serializable
data class AgregarComidaRequest(
    val nombre: String,
    val hora_sugerida: String? = null,
    val alimentos: List<com.spc.nutricoach.model.Alimento> = emptyList()
)

@Serializable
data class AgregarAlimentoRequest(
    val alimento_id: String,
    val nombre_snapshot: String,
    val cantidad: Double,
    val unidad: String
)

@Serializable
data class ModificarDietaRequest(
    val nombre: String? = null,
    val publica: Boolean? = null
)

@Serializable
data class SerieCompletada(
    val repeticiones: String,
    val peso: Double,
    val descanso_segundos: Int
)

@Serializable
data class EjercicioCompletado(
    val ejercicio_id: String? = null,
    val nombre_snapshot: String,
    val series: List<SerieCompletada>
)

@Serializable
data class EntrenamientoLog(
    @SerialName("_id") val id: String? = null,
    val rutina_nombre: String,
    val dia_nombre: String,
    val fecha: String,
    val ejercicios: List<EjercicioCompletado>
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
    val liked: Boolean,
    val likes_count: Int
)