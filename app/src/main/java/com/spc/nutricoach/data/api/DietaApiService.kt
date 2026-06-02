package com.spc.nutricoach.data.api

import com.spc.nutricoach.model.Dieta
import com.spc.nutricoach.data.CrearDietaRequest
import com.spc.nutricoach.data.CrearDietaResponse
import com.spc.nutricoach.data.ModificarDietaRequest
import com.spc.nutricoach.data.AgregarComidaRequest
import com.spc.nutricoach.data.AgregarAlimentoRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface DietaApiService {
    @GET("clientes/{id}/dietas-activas")
    suspend fun obtenerDietasCliente(
        @Path("id") clienteId: String
    ): List<Dieta>

    @GET("dietas/publicas")
    suspend fun obtenerDietasPublicas(): List<Dieta>

    @GET("dietas/{id}")
    suspend fun obtenerDietaPorId(
        @Path("id") dietaId: String
    ): Dieta

    @POST("dietas")
    suspend fun crearDieta(
        @Body request: CrearDietaRequest
    ): CrearDietaResponse

    @PUT("dietas/{id}")
    suspend fun modificarDieta(
        @Path("id") dietaId: String,
        @Body request: ModificarDietaRequest
    )

    @DELETE("dietas/{id}")
    suspend fun eliminarDieta(
        @Path("id") dietaId: String
    )

    @POST("dietas/{id}/comidas")
    suspend fun agregarComidaADieta(
        @Path("id") dietaId: String,
        @Body request: AgregarComidaRequest
    )

    @POST("dietas/{id}/comidas/{comida_index}/alimentos")
    suspend fun agregarAlimentoAComida(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int,
        @Body request: AgregarAlimentoRequest
    )

    @DELETE("dietas/{id}/comidas/{comida_index}")
    suspend fun eliminarComida(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int
    )

    @DELETE("dietas/{id}/comidas/{comida_index}/alimentos/{alimento_index}")
    suspend fun eliminarAlimento(
        @Path("id") dietaId: String,
        @Path("comida_index") comidaIndex: Int,
        @Path("alimento_index") alimentoIndex: Int
    )
}
