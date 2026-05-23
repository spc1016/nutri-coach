package com.spc.nutricoach.data

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.spc.nutricoach.model.Dieta
import com.spc.nutricoach.model.LoginApiResponse
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.DELETE
import com.spc.nutricoach.model.Post
import com.spc.nutricoach.model.CrearPostRequest
import com.spc.nutricoach.model.ComentarioRequest
import com.spc.nutricoach.model.ToggleLikeResponse
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

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

interface NutricionApiService {
    @POST("login")
    suspend fun login(@Body request: LoginRequest): LoginApiResponse

    @POST("clientes")
    suspend fun crearCliente(@Body request: RegistroClienteRequest): RegistroApiResponse

    @POST("register/send-code")
    suspend fun sendVerificationCode(@Body request: SendCodeRequest): MessageResponse

    @POST("register/verify-code")
    suspend fun verifyCode(@Body request: VerifyCodeRequest): RegistroApiResponse

    @POST("register/resend-code")
    suspend fun resendCode(@Body request: ResendCodeRequest): MessageResponse

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

    @POST("clientes/{id}/historial-entrenamientos")
    suspend fun registrarEntrenamiento(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String,
        @Body request: EntrenamientoLog
    ): RegistroApiResponse

    @GET("clientes/{id}/historial-entrenamientos")
    suspend fun obtenerHistorialEntrenamientos(
        @Path("id") clienteId: String,
        @Header("Authorization") token: String
    ): List<EntrenamientoLog>


    @GET("rutinas/publicas")
    suspend fun obtenerRutinasPublicas(
        @Header("Authorization") token: String
    ): List<com.spc.nutricoach.model.Rutina>

    @GET("rutinas/{id}")
    suspend fun obtenerRutinaPorId(
        @Path("id") rutinaId: String,
        @Header("Authorization") token: String
    ): com.spc.nutricoach.model.Rutina

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

    @DELETE("rutinas/{id}")
    suspend fun eliminarRutina(
        @Path("id") rutinaId: String,
        @Header("Authorization") token: String
    )

    @DELETE("rutinas/{id}/dias/{dia_index}")
    suspend fun eliminarDia(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int,
        @Header("Authorization") token: String
    )

    @DELETE("rutinas/{id}/dias/{dia_index}/ejercicios/{ejercicio_index}")
    suspend fun eliminarEjercicio(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int,
        @Path("ejercicio_index") ejercicioIndex: Int,
        @Header("Authorization") token: String
    )

    // --- Endpoints Dietas ---
    @GET("dietas/publicas")
    suspend fun obtenerDietasPublicas(
        @Header("Authorization") token: String
    ): List<Dieta>

    @POST("dietas")
    suspend fun crearDieta(
        @Header("Authorization") token: String,
        @Body request: CrearDietaRequest
    ): CrearDietaResponse

    @retrofit2.http.PUT("dietas/{id}")
    suspend fun modificarDieta(
        @Path("id") dietaId: String,
        @Header("Authorization") token: String,
        @Body request: ModificarDietaRequest
    )

    @DELETE("dietas/{id}")
    suspend fun eliminarDieta(
        @Path("id") dietaId: String,
        @Header("Authorization") token: String
    )

    @POST("dietas/{id}/comidas")
    suspend fun agregarComidaADieta(
        @Path("id") dietaId: String,
        @Header("Authorization") token: String,
        @Body request: AgregarComidaRequest
    )

    @POST("dietas/{id}/comidas/{comida_index}/alimentos")
    suspend fun agregarAlimentoAComida(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int,
        @Header("Authorization") token: String,
        @Body request: AgregarAlimentoRequest
    )

    @DELETE("dietas/{id}/comidas/{comida_index}")
    suspend fun eliminarComida(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int,
        @Header("Authorization") token: String
    )

    @DELETE("dietas/{id}/comidas/{comida_index}/alimentos/{alimento_index}")
    suspend fun eliminarAlimento(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int,
        @Path("alimento_index") alimentoIndex: Int,
        @Header("Authorization") token: String
    )

    // --- Endpoints Comunidad / Posts ---
    @GET("posts")
    suspend fun obtenerPosts(): List<Post>

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

    @GET("clientes")
    suspend fun obtenerTodosClientes(): List<com.spc.nutricoach.model.Cliente>

    @POST("clientes/{id}/seguir")
    suspend fun seguirUsuario(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    )

    @retrofit2.http.DELETE("clientes/{id}/seguir")
    suspend fun dejarDeSeguirUsuario(
        @Path("id") userId: String,
        @Header("Authorization") token: String
    )

    @GET("clientes/{id}/seguidores")
    suspend fun obtenerSeguidores(
        @Path("id") userId: String
    ): List<com.spc.nutricoach.model.Cliente>

    @GET("clientes/{id}/seguidos")
    suspend fun obtenerSeguidos(
        @Path("id") userId: String
    ): List<com.spc.nutricoach.model.Cliente>
}

object NutriCoachApiClient {
    private const val BASE_URL = "https://api-nutri-coach.onrender.com/"

    private val json = Json { ignoreUnknownKeys = true }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val service: NutricionApiService by lazy {
        retrofit.create(NutricionApiService::class.java)
    }
}