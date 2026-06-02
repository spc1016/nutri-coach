package com.spc.nutricoach.data.api

import com.spc.nutricoach.model.Rutina
import com.spc.nutricoach.data.EntrenamientoLog
import com.spc.nutricoach.data.RegistroApiResponse
import com.spc.nutricoach.data.CrearRutinaRequest
import com.spc.nutricoach.data.CrearRutinaResponse
import com.spc.nutricoach.data.AgregarDiaRequest
import com.spc.nutricoach.data.AgregarEjercicioRequest
import com.spc.nutricoach.data.ModificarRutinaRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface RutinaApiService {
    @GET("clientes/{id}/rutinas-activas")
    suspend fun obtenerRutinasCliente(
        @Path("id") clienteId: String
    ): List<Rutina>

    @POST("clientes/{id}/historial-entrenamientos")
    suspend fun registrarEntrenamiento(
        @Path("id") clienteId: String,
        @Body request: EntrenamientoLog
    ): RegistroApiResponse

    @GET("clientes/{id}/historial-entrenamientos")
    suspend fun obtenerHistorialEntrenamientos(
        @Path("id") clienteId: String
    ): List<EntrenamientoLog>

    @GET("rutinas/publicas")
    suspend fun obtenerRutinasPublicas(): List<Rutina>

    @GET("rutinas/{id}")
    suspend fun obtenerRutinaPorId(
        @Path("id") rutinaId: String
    ): Rutina

    @POST("rutinas")
    suspend fun crearRutina(
        @Body request: CrearRutinaRequest
    ): CrearRutinaResponse

    @POST("rutinas/{id}/dias")
    suspend fun agregarDiaARutina(
        @Path("id") rutinaId: String,
        @Body request: AgregarDiaRequest
    )

    @POST("rutinas/{id}/dias/{dia_index}/ejercicios")
    suspend fun agregarEjercicioADia(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int,
        @Body request: AgregarEjercicioRequest
    )

    @PUT("rutinas/{id}")
    suspend fun modificarRutina(
        @Path("id") rutinaId: String,
        @Body request: ModificarRutinaRequest
    )

    @DELETE("rutinas/{id}")
    suspend fun eliminarRutina(
        @Path("id") rutinaId: String
    )

    @DELETE("rutinas/{id}/dias/{dia_index}")
    suspend fun eliminarDia(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int
    )

    @DELETE("rutinas/{id}/dias/{dia_index}/ejercicios/{ejercicio_index}")
    suspend fun eliminarEjercicio(
        @Path("id") rutinaId: String,
        @Path("dia_index") diaIndex: Int,
        @Path("ejercicio_index") ejercicioIndex: Int
    )
}
