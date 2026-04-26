package com.spc.nutricoach.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spc.nutricoach.model.Dieta
import com.spc.nutricoach.model.LoginApiResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegistroClienteRequest(
    val nombre: String,
    val email: String,
    val password_hash: String,
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
data class ModificarClienteRequest(
    val nombre: String? = null,
    val email: String? = null,
    val password_hash: String? = null,
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
    val publica: Boolean? = null
)

interface NutricionApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("clientes")
    suspend fun crearCliente(@Body request: RegistroClienteRequest): RegistroApiResponse

    @GET("clientes/{id}")
    suspend fun obtenerCliente(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String
    ): com.spc.nutricoach.model.Cliente

    @retrofit2.http.PUT("clientes/{id}")
    suspend fun modificarCliente(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String,
        @Body request: ModificarClienteRequest
    )

    @GET("clientes/{id}/dietas-activas")
    suspend fun obtenerDietasCliente(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String
    ): List<Dieta>

    @GET("clientes/{id}/rutinas-activas")
    suspend fun obtenerRutinasCliente(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String
    ): List<com.spc.nutricoach.model.Rutina>

    @GET("rutinas/publicas")
    suspend fun obtenerRutinasPublicas(
        @Header("Authorization") token: String
    ): List<com.spc.nutricoach.model.Rutina>

    @POST("rutinas")
    suspend fun crearRutina(
        @Header("Authorization") token: String,
        @Body request: CrearRutinaRequest
    ): CrearRutinaResponse

    @POST("rutinas/{id}/dias")
    suspend fun agregarDiaARutina(
        @Path("id") rutinaId: String,
        @Header("Authorization") token: String,
        @Body request: AgregarDiaRequest
    )

    @POST("rutinas/{id}/dias/{dia_index}/ejercicios")
    suspend fun agregarEjercicioADia(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int,
        @Header("Authorization") token: String,
        @Body request: AgregarEjercicioRequest
    )

    @retrofit2.http.PUT("rutinas/{id}")
    suspend fun modificarRutina(
        @Path("id") rutinaId: String,
        @Header("Authorization") token: String,
        @Body request: ModificarRutinaRequest
    )
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