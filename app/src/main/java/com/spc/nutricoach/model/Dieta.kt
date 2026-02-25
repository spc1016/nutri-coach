package com.spc.nutricoach.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Alimento(
    @SerialName("alimento_id") val alimentoId: String? = null,
    @SerialName("nombre_snapshot") val nombreSnapshot: String = "",
    val cantidad: Double = 0.0,
    val unidad: String = "",
    val notas: String? = null
)

@Serializable
data class Comida(
    val nombre: String = "",
    @SerialName("hora_sugerida") val horaSugerida: String? = null,
    val orden: Int = 0,
    val alimentos: List<Alimento> = emptyList()
)

@Serializable
data class Dieta(
    @SerialName("_id") val id: String = "",
    @SerialName("cliente_id") val clienteId: String = "",
    val nombre: String = "",
    @SerialName("kcal_objetivo") val kcalObjetivo: Int = 0,
    @SerialName("fecha_asignacion") val fechaAsignacion: String? = null,
    val activa: Boolean = true,
    @SerialName("notas_generales") val notasGenerales: String? = null,
    val comidas: List<Comida> = emptyList()
)
