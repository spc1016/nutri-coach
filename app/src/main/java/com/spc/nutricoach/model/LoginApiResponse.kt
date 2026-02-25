package com.spc.nutricoach.model

import kotlinx.serialization.Serializable

@Serializable
data class LoginApiResponse(
    val token: String,
    val role: String
)