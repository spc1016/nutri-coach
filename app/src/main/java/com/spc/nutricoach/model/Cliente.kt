package com.spc.nutricoach.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Cliente(
    @SerialName("_id") val id: String? = null,
    val nombre: String,
    val email: String,
    val telefono: String? = null,
    val edad: Int? = null,
    val peso: Double? = null,
    val altura: Double? = null,
    val genero: String? = null,
    val objetivo: String? = null,
    val nutricionista_id: String? = null,
    val seguidores_count: Int = 0,
    val seguidos_count: Int = 0
)
