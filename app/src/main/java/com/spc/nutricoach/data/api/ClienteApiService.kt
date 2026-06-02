package com.spc.nutricoach.data.api

import com.spc.nutricoach.model.Cliente
import com.spc.nutricoach.data.ModificarClienteRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ClienteApiService {
    @GET("clientes/{id}")
    suspend fun obtenerCliente(
        @Path("id") clienteId: String
    ): Cliente

    @PUT("clientes/{id}")
    suspend fun modificarCliente(
        @Path("id") clienteId: String,
        @Body request: ModificarClienteRequest
    )

    @GET("clientes")
    suspend fun obtenerTodosClientes(): List<Cliente>

    @POST("clientes/{id}/seguir")
    suspend fun seguirUsuario(
        @Path("id") userId: String
    )

    @DELETE("clientes/{id}/seguir")
    suspend fun dejarDeSeguirUsuario(
        @Path("id") userId: String
    )

    @GET("clientes/{id}/seguidores")
    suspend fun obtenerSeguidores(
        @Path("id") userId: String
    ): List<Cliente>

    @GET("clientes/{id}/seguidos")
    suspend fun obtenerSeguidos(
        @Path("id") userId: String
    ): List<Cliente>
}
