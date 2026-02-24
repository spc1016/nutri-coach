package com.spc.nutricoach.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spc.nutricoach.model.LoginApiResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

interface NutricionApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse
}

object NutriCoachApiClient {
    private const val BASE_URL = "http://nutricoach.us-east-1.elasticbeanstalk.com/"

    private val json = Json { ignoreUnknownKeys = true }

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val service: NutricionApiService by lazy {
        retrofit.create(NutricionApiService::class.java)
    }
}