package com.spc.nutricoach.model

data class LoginApiResponse(
    val token: String,
    val role: String
)